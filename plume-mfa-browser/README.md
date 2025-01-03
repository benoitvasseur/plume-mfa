Plume MFA Browser
=================

Plume MFA is based on [Plume Framework](https://github.com/Coreoz/Plume),
it provides additional authentication factor with browser enrollment.

Installation
------------
1. Maven dependency:
```xml
<dependency>
    <groupId>com.coreoz</groupId>
    <artifactId>plume-mfa-browser</artifactId>
</dependency>
```

1. Set an app name for the QRCode: `mfa.appname = "App name"`
2. [Current user access](#current-user-access)
3. SQL, see [setup files](plume-mfa-authenticator/sql)
4. Implement the webservices
5. Disable previous API that could logged in the user without the MFA token. For exemple, API from SessionWS if you are using Plume-admin dependencies.


Current user access
-------------------
To fetch the current user trying to authenticate, you must provide an implementation of `MfaUserService`.
This interface will return the id of the user trying to authenticate after the first factor has been verified from your end.
```java
bind(MfaUserService.class).to(MfaUserServiceImplementation.class);
```

`MfaUserServiceImplementation` need to implement 3 methods :

```java
@Override
public Long authenticatedUserId(String username, String password) {
    // return the id of the user if the username and password are correct
    return userService.authenticate(username, password)
            .map(authenticatedUser -> authenticatedUser.getUser().getId())
            .orElse(null);
}

@Override
public Long userId(String username) {
    return userDao.findByUserName(username)
            .map(user -> user.getId())
            .orElse(null);
}

@Override
public Long username(Long userId) {
    return userDao.findById(userId)
            .map(user -> user.getUsername())
            .orElse(null);
}
```

Configuration
-------------

To work properly, the module requires the following configuration:
```
# This key is used as the issuer by Google Authenticator to generate QRCode
mfa.appname = "App name"
```

Browser enrollment webservices
------------------------------

### Start registration

To get credentials from the user browser, you'll need to generate the object needed by the webauthn API, such as a `PublicKeyCredentialCreationOptions`

```java
/**
 * This method will return the PublicKeyCredentialCreationOptions needed by the browser
 * and will check that the user is authenticated
 */

mfaService.startRegistration(authenticatedUser)
```

An exemple of implementation could look like this:

```java
@POST
@Operation(description = "Start the registration of a new MFA credential with WebAuthn")
@Path("/webauth/start-registration")
public String getWebAuthentCreationOptions(UserCredentials credentials) {
    // First user needs to be authenticated (an exception will be raised otherwise)
    Validators.checkRequired("Json creadentials", credentials);
    Validators.checkRequired("User name", credentials.getUserName());
    Validators.checkRequired("Password", credentials.getPassword());

    // Generate the PublicKeyCredentialCreationOptions
    PublicKeyCredentialCreationOptions options = mfaService.startRegistration(credentials);
    try {
        return options.toCredentialsCreateJson();
    } catch (JsonProcessingException e) {
        logger.debug("erreur lors de la génération du PublicKeyCredentialCreationOptions", e);
        throw new WsException(WsError.INTERNAL_ERROR);
    }
}
```

### Register the credential generated by the browser

```java
@POST
@Operation(description = "Register public key of a new MFA credential")
@Path("/webauth/register-credential")
public Response registerCredential(BrowserPublicKeyCredentials credentials) {
    Validators.checkRequired("Json credentials", credentials.getCredentials());
    Validators.checkRequired("User name", credentials.getCredentials().getUserName());
    Validators.checkRequired("Password", credentials.getCredentials().getPassword());
    Validators.checkRequired("Public key", credentials.getPublicKeyCredentialJson());

    // Finish the registration of the new MFA credential
    String publicKeyCredentialJson = credentials.getPublicKeyCredentialJson();
    try {
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
        PublicKeyCredential.parseRegistrationResponseJson(publicKeyCredentialJson);
        boolean success = mfaService.finishRegistration(credentials.getCredentials(), pkc);
        if (!success) {
            throw new WsException(WsError.INTERNAL_ERROR);
        }
        return Response.ok().build();
    } catch (IOException e) {
        logger.error("publicKeyCredentialJson parsing error", e);
        return Response.serverError().build();
    }
}
```

### Get Assertion for the user

```java
@GET
@Operation(description = "Get an assertion for the user")
@Path("/webauth/assertion/{username}")
public String getAssertion(@PathParam("username") String username) {
    // Generate the PublicKeyCredentialRequestOptions
    AssertionRequest options = mfaService.getAssertionRequest(username);
    try {
        return options.toCredentialsGetJson();
    } catch (JsonProcessingException e) {
        logger.debug("erreur lors de la génération du credentialGetOptions", e);
        throw new WsException(WsError.INTERNAL_ERROR);
    }
}
```

### Verify the public key

```java
@POST
@Operation(description = "Start the authentication with WebAuthn")
@Path("/webauth/verify")
public Response verifyCredential(BrowserPublicKeyCredentials credentials) {
    Validators.checkRequired("Json credentials", credentials.getCredentials());
    Validators.checkRequired("User name", credentials.getCredentials().getUserName());
    Validators.checkRequired("Public key", credentials.getPublicKeyCredentialJson());
    try {
        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
            PublicKeyCredential.parseAssertionResponseJson(credentials.getPublicKeyCredentialJson());

        // Verify the assertion
        boolean isVerified = mfaService.verifyWebauth(credentials.getCredentials().getUserName(), pkc);
        if (!isVerified) {
            throw new WsException(WsError.INTERNAL_ERROR);
        }
        User user = ... // Get the user
        FingerprintWithHash fingerprintWithHash = sessionUseFingerprintCookie ? generateFingerprint() : NULL_FINGERPRINT;
        return withFingerprintCookie(
            Response.ok(toAdminSession(toWebSession(adminUserService.authenticateWithMfa(user), fingerprintWithHash.getHash()))),
            fingerprintWithHash.getFingerprint()
        )
        .build();

    } catch (IOException e) {
        throw new WsException(AdminWsError.WRONG_LOGIN_OR_PASSWORD);
    }
}
```