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
2. Jersey web-services: `packages("com.coreoz.plume.mfa.webservices")`
3. [Generate a hash secret key](#configuration) and register it in your configuration: `mfa.secret = "long_generated_password_to_secure_mfa_tokens"`
4. Set an app name for the QRCode: `mfa.appname = "App name"`
5. [Current user access](#current-user-access)
6. [Secret key hashing](#secret-key-hashing) (optional)
7. SQL, see [setup files](plume-mfa-authenticator/sql)

Current user access
-------------------
To fetch the current user trying to authenticate, you must provider an implemtation of `MfaUserService`.
This interface will return the id of the user trying to authenticate after the first factor has been verified from your end.
```java
bind(MfaUserService.class).to(MfaUserServiceImplemntation.class);
```

Configuration
-------------

To work properly, the module requires the following configuration:
```
# this key is used by the default MfaSecretKeyEncryption to generate users secret keys
mfa.secret = "long_generated_password_to_secure_mfa_tokens"

# This key is used as the issuer by Google Authenticator to generate QRCode
mfa.appname = "App name"
```

Secret key hashing
----------------
To use this module without our secret key encryption, you may want to provide implementations of `SecretKeyEncryption`, and `MfaSecretKeyEncryption`.
As an example, here is what is defined in the Admin Web-services Guice configuration:

```java
bind(SecretKeyEncryption.class).toProvider(MfaSecretKeyEncryptionProvider.class);
bind(MfaSecretKeyEncryption.class).toProvider(MfaSecretKeyEncryptionProvider.class);
```

Note that this service is already bound if you are already using `GuiceMfaAuthenticatorWithDefaultsModule`.
```

Upgrade instructions
--------------------
See the [releases notes](https://github.com/Coreoz/Plume-mfa/releases) to see the upgrade instructions.
