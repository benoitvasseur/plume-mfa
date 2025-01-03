package com.coreoz.plume.mfa.browser.services;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.guava.Cache;
import org.glassfish.jersey.internal.guava.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.StartAssertionOptions.StartAssertionOptionsBuilder;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;

import com.coreoz.plume.mfa.browser.db.daos.MfaBrowserDao;
import com.coreoz.plume.mfa.core.data.UserCredentials;
import com.coreoz.plume.mfa.core.services.MfaConfigurationService;
import com.coreoz.plume.mfa.core.services.MfaUserService;

@Singleton
public class MfaBrowserService {

    private static final Logger logger = LoggerFactory.getLogger(MfaBrowserService.class);

    private final MfaBrowserDao mfaBrowserDao;
    private final MfaUserService userService;

    private final RelyingParty relyingParty;
    private final Random random = new Random();

    private final Cache<String, PublicKeyCredentialCreationOptions> createOptionCache =
        CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build();
    private final Cache<String, AssertionRequest> verifyOptionCache =
        CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.MINUTES).build();

    @Inject
    private MfaBrowserService(
        MfaBrowserDao mfaBrowserDao,
        MfaUserService userService,
        MfaConfigurationService configurationService
    ) {
        this.mfaBrowserDao = mfaBrowserDao;
        this.userService = userService;
        RelyingPartyIdentity identity = RelyingPartyIdentity.builder()
            .id("localhost") // TODO: Conf ?
            .name(configurationService.appName())
            .build();
        this.relyingParty = RelyingParty.builder()
            .identity(identity)
            .credentialRepository(mfaBrowserDao)
            .build();
    }

    /**
     * Start the registration process for a user
     * and generate an object that will be used by the webauthn API.
     * The method with check the user credentials and throw an exception if they are invalid.
     * @param user
     * @return
     */
    public PublicKeyCredentialCreationOptions startRegistration(UserCredentials user) {
        Long idUser = userService.authenticatedUserId(user.getUserName(), user.getPassword());
        if (idUser == null) {
            // TODO: Invalid credentials
            return null;
        }

        byte[] userHandle = new byte[64];
        random.nextBytes(userHandle);
        StartRegistrationOptions options = StartRegistrationOptions.builder()
            .user(UserIdentity.builder()
                .name(user.getUserName())
                .displayName(user.getUserName())
                .id(new ByteArray(userHandle))
                .build())
            .build();
        PublicKeyCredentialCreationOptions createOptions = relyingParty.startRegistration(options);
        createOptionCache.put(user.getUserName(), createOptions);
        return createOptions;
    }

    /**
     * Finish the registration process for a user.
     * Save the credentials in database.
     * The method with check the user credentials and throw an exception if they are invalid.
     * @param user
     * @param pkc
     * @return
     */
    public boolean finishRegistration(
        UserCredentials user,
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc
    ) {
        Long idUser = userService.authenticatedUserId(user.getUserName(), user.getPassword());
        if (idUser == null) {
            // TODO: Invalid credentials
            return false;
        }
        PublicKeyCredentialCreationOptions request = createOptionCache.getIfPresent(user.getUserName());
        if (request == null) {
            return false;
        }
        try {
            RegistrationResult result = relyingParty.finishRegistration(
                FinishRegistrationOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build()
            );
            mfaBrowserDao.registerCredential(idUser, result, pkc);
            return true;
        } catch (RegistrationFailedException e) {
            logger.error("Error finishing registration", e);
            return false;
        }
    }

    public boolean verifyWebauth(String username, PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc) {
        if (pkc.getResponse().getUserHandle().isEmpty()) {
            return false;
        }
        Optional<String> keyUsername = mfaBrowserDao.getUsernameForUserHandle(pkc.getResponse().getUserHandle().get());
        if (keyUsername.isEmpty() || !keyUsername.get().equals(username)) {
            return false;
        }
        AssertionRequest assertion = verifyOptionCache.getIfPresent(username);

        try {
            AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(assertion)  // The PublicKeyCredentialRequestOptions from startAssertion above
                    .response(pkc)
                    .build());
            if (result.isSuccess()) {
                Long idUser = userService.userId(username);
                mfaBrowserDao.updateCredential(idUser, result);
                return true;
            }
            return false;
        } catch (AssertionFailedException e) {
            return false;
        }
    }

    public AssertionRequest getAssertionRequest(String username) {
        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
            .username(username)
            .build());

        verifyOptionCache.put(username, request);
        return request;
    }
}
