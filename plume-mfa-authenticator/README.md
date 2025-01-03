Plume MFA Authenticator
=======================

Plume MFA is based on [Plume Framework](https://github.com/Coreoz/Plume),
it provides additional authentication factor Authenticator (QRCode).

Installation
------------
1. Maven dependency:
```xml
<dependency>
    <groupId>com.coreoz</groupId>
    <artifactId>plume-mfa-authenticator</artifactId>
</dependency>
```

1. Guice module: `install(new GuiceMfaAuthenticatorWithDefaultsModule())`
2. [Generate a hash secret key](#configuration) and register it in your configuration: `mfa.secret = "long_generated_base64_password_to_secure_mfa_tokens"`
3. Set an app name for the QRCode: `mfa.appname = "App name"`
4. [Current user access](#current-user-access)
5. [Secret key hashing](#secret-key-hashing) (optional)
6. SQL, see [setup files](plume-mfa-authenticator/sql)
7. Implement the [generate qrcode and registered webservices](#qrcode-webservice) to generate QrCode and verify the MFA token
8. Implement the [verify webservice](#verify-the-mfa-token)
9. Implement the frontend and call the webservice `/auhenticator/qrcode` to get an image of the QRCode that need to be scanned by the Authenticator app of the user. This app will generate a 6 digits code that need to be sent to the verify webservice (from step `8`) to verify the code.
Alternatively, you can use the webservice `/auhenticator/qrcode-url` to get the URL contained in the QRCode image.
10. Disable previous API that could logged in the user without the MFA token. For exemple, API from SessionWS if you are using Plume-admin dependencies.
11. clean up [unregistered authenticator](#clean-up-unregistered-authenticator) (optional)

Current user access
-------------------
To fetch the current user trying to authenticate, you must provide an implementation of `MfaUserService`.
This interface will return the id of the user trying to authenticate after the first factor has been verified from your end.
```java
bind(MfaUserService.class).to(MfaUserServiceImplementation.class);
```

`MfaUserServiceImplementation` need to implement 2 methods :

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
```

Configuration
-------------

To work properly, the module requires the following configuration:
```
# this key is used by the default MfaSecretKeyEncryption to generate users secret keys, it must be a base64 encoded string of 32 bytes
mfa.secret = "long_generated_base64_password_to_secure_mfa_tokens"

# This key is used as the issuer by Google Authenticator to generate QRCode
mfa.appname = "App name"
```

Qrcode webservices
------------------

To generate the QRCode for your user, you'll generate a secret key.
And generate the QR Code that embeds the secret key and the user's username.

```java
String secretKey = mfaAuthenticatorService.createMfaAuthenticatorSecretKey(credentials);
// To generate an url
String qrCodeUrl = mfaAuthenticatorService.getQRBarcodeURL(credentials, secretKey);
// To generate an image
byte[] qrCode = mfaAuthenticatorService.generateQRCode(credentials, secretKey);
```
An exemple of implementation could look like this:

```java
@POST
@Operation(description = "Generate a qrcode for MFA enrollment")
@Path("/qrcode")
public Response qrCode(UserCredentials credentials) {
    // First user needs to be authenticated (an exception will be raised otherwise)

    Validators.checkRequired("Json creadentials", credentials);
    Validators.checkRequired("User name", credentials.getUserName());
    Validators.checkRequired("Password", credentials.getPassword());
    // Generate MFA secret key and QR code URL
    try {
        String secretKey = mfaAuthenticatorService.createMfaAuthenticatorSecretKey(credentials);
        byte[] qrCode = mfaAuthenticatorService.generateQRCode(credentials, secretKey);

        // Return the QR code image to the client
        ResponseBuilder response = Response.ok(qrCode);
        response.header("Content-Disposition", "attachment; filename=qrcode.png");
        response.header("Content-Type", "image/png");
        return response.build();
    } catch (Exception e) {
        logger.debug("Error during QR code generation", e);
        throw new WsException(WsError.INTERNAL_ERROR);
    }
}
```

Note that by default the authenticator is created with a flag `enabled` set to `false`. This flag will be set to `true` when the user will use the authenticator for the first time.
A good practice is to let him check immediately after the QRCode has been scanned that the authenticator is working properly. For this, you could implement a dedicated webservice that will check the token and set the `enabled` flag to `true` if the token is correct.

```java
@POST
@Operation(description = "Generate a qrcode for MFA enrollment")
@Path("/register")
public Response registerQrcode(AuthenticatorCredentials credentials) {
    // Validate the MFA code
    Validators.checkRequired("Json creadentials", credentials);
    Validators.checkRequired("User name", credentials.getUserName());
    Validators.checkRequired("Code", credentials.getCode());
    try {
        if (mfaAuthenticatorService.isCredentialsValidForUser(credentials.getUserName(), credentials.getCode())) {
            return Response.ok().build();
        } else {
            throw new WsException(WsError.INVALID_CREDENTIALS);
        }
    } catch (Exception e) {
        logger.debug("erreur lors de la validation du code MFA", e);
        throw new WsException(WsError.INTERNAL_ERROR);
    }
}
```

Verify the MFA token
--------------------

To verify the MFA token through a webservice, you'll need to :
1. use the `MfaAuthenticatorService` to verify the token
2. return your websession after the token has been verified

```java
@POST
@Path("/verify")
@Operation(description = "Verify MFA code for authentication")
public Response verifyMfa(AuthenticatorCredentials credentials) {
    // first user needs to be authenticated (an exception will be raised otherwise)
    if (mfaAuthenticatorService.isCredentialsValidForUser(credentials.getUserName(), credentials.getCode())) {
        AuthenticatedUser authenticatedUser = mfaUserService.authenticatedUser(credentials.getUserName());
        // if the client is authenticated, the fingerprint can be generated if needed
        FingerprintWithHash fingerprintWithHash = sessionUseFingerprintCookie ? sessionWs.generateFingerprint() : SessionWs.NULL_FINGERPRINT;
        return sessionWs.withFingerprintCookie(
            Response.ok(sessionWs.toAdminSession(sessionWs.toWebSession(authenticatedUser, fingerprintWithHash.getHash()))),
            fingerprintWithHash.getFingerprint()
        )
        .build();
    }
    return Response.status(Response.Status.UNAUTHORIZED).build();
}
```

Secret key hashing
------------------
To use this module without our secret key encryption, you may want to provide implementations of `SecretKeyEncryption`, and `MfaSecretKeyEncryption`.
As an example, here is what is defined in the Admin Web-services Guice configuration:

```java
bind(SecretKeyEncryption.class).toProvider(MfaSecretKeyEncryptionProvider.class);
bind(MfaSecretKeyEncryption.class).toProvider(MfaSecretKeyEncryptionProvider.class);
```

Note that this service is already bound if you are already using `GuiceMfaAuthenticatorWithDefaultsModule`.

Clean up unregistered authenticator
-----------------------------------

You can delete a batch of unregistered authenticator by calling the following method in a scheduled task:

```java
mfaAuthenticatorService.deleteOldNotEnabledAuthenticators(instant);
```

note that you can also delete a specific authenticator by calling the following method:

```java
mfaAuthenticatorService.deleteAuthenticator(authenticatorId);
```

Upgrade instructions
--------------------
See the [releases notes](https://github.com/Coreoz/Plume-mfa/releases) to see the upgrade instructions.
