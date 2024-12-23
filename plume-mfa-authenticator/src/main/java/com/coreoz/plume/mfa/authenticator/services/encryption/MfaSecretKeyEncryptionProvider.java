package com.coreoz.plume.mfa.authenticator.services.encryption;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.coreoz.plume.mfa.authenticator.services.MfaConfigurationService;

@Singleton
public class MfaSecretKeyEncryptionProvider  implements Provider<MfaSecretKeyEncryption> {

    private final MfaSecretKeyEncryption mfaSecretKeyEncryption;

    @Inject
    private MfaSecretKeyEncryptionProvider(MfaConfigurationService conf) {
        this.mfaSecretKeyEncryption = new MfaSecretKeyEncryption(conf.mfaSecret());
    }

    @Override
    public MfaSecretKeyEncryption get() {
        return mfaSecretKeyEncryption;
    }
}
