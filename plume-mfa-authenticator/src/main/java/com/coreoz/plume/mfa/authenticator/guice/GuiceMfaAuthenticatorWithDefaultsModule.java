package com.coreoz.plume.mfa.authenticator.guice;

import com.coreoz.plume.mfa.authenticator.services.encryption.MfaSecretKeyEncryption;
import com.coreoz.plume.mfa.authenticator.services.encryption.MfaSecretKeyEncryptionProvider;
import com.coreoz.plume.mfa.authenticator.services.encryption.SecretKeyEncryption;
import com.google.inject.AbstractModule;

public class GuiceMfaAuthenticatorWithDefaultsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SecretKeyEncryption.class).toProvider(MfaSecretKeyEncryptionProvider.class);
        bind(MfaSecretKeyEncryption.class).toProvider(MfaSecretKeyEncryptionProvider.class);
    }

}
