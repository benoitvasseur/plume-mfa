package com.coreoz.plume.mfa.browser.db.daos;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor.PublicKeyCredentialDescriptorBuilder;

import com.coreoz.plume.mfa.browser.db.generated.QMfaBrowser;
import com.coreoz.plume.mfa.browser.db.generated.MfaBrowser;
import com.coreoz.plume.mfa.core.services.MfaUserService;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

@Singleton
public class MfaBrowserDao extends CrudDaoQuerydsl<MfaBrowser> implements CredentialRepository {

	private final MfaUserService userService;

    @Inject
	private MfaBrowserDao(
		TransactionManagerQuerydsl transactionManager,
		MfaUserService userService
	) {
		super(transactionManager, QMfaBrowser.mfaBrowser);
		this.userService = userService;
	}

	public List<MfaBrowser> findByIdUser(Long idUser) {
		return transactionManager.selectQuery()
			.select(QMfaBrowser.mfaBrowser)
			.from(QMfaBrowser.mfaBrowser)
			.where(QMfaBrowser.mfaBrowser.idUser.eq(idUser))
			.fetch();
	}

    public void registerCredential(
        Long idUser,
        RegistrationResult result,
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc
    ) {
        MfaBrowser mfa = new MfaBrowser();
        mfa.setKeyId(result.getKeyId().getId().getBytes());
        mfa.setIdUser(idUser);
        mfa.setCreationDate(Instant.now());
        mfa.setPublicKeyCose(result.getPublicKeyCose().getBytes());
        mfa.setSignatureCount((int)result.getSignatureCount());
        mfa.setIsDiscoverable(result.isDiscoverable().orElse(null));
        mfa.setAttestation(pkc.getResponse().getAttestationObject().getBytes());
        mfa.setClientDataJson(pkc.getResponse().getClientDataJSON().getBytes());
        save(mfa);
    }

    public void updateCredential(
        Long idUser,
        AssertionResult result
    ) {
        MfaBrowser mfa = transactionManager.selectQuery()
            .select(QMfaBrowser.mfaBrowser)
            .from(QMfaBrowser.mfaBrowser)
            .where(QMfaBrowser.mfaBrowser.idUser.eq(idUser)
                .and(QMfaBrowser.mfaBrowser.keyId.eq(result.getCredentialId().getBytes())))
            .fetchOne();
        mfa.setUserHandle(result.getUserHandle().getBytes());
        mfa.setSignatureCount((int)result.getSignatureCount());
        save(mfa);
    }

    // ------------------- CredentialRepository methods -------------------

	@Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
		Long userId = userService.userId(username);
        List<byte[]> results = transactionManager.selectQuery()
            .select(QMfaBrowser.mfaBrowser.publicKeyCose)
            .from(QMfaBrowser.mfaBrowser)
            .where(QMfaBrowser.mfaBrowser.idUser.eq(userId))
            .fetch();
        // Transform the list of byte arrays into a set of PublicKeyCredentialDescriptors
        return results.stream()
            .map(bytes -> {
                PublicKeyCredentialDescriptorBuilder builder = PublicKeyCredentialDescriptor.builder()
                    .id(new ByteArray(bytes))
                    // Todo: everything should come from the database
                    .type(PublicKeyCredentialType.PUBLIC_KEY);
                return builder.build();
            })
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        Long userId = userService.userId(username);
        byte[] bytes = transactionManager.selectQuery()
            .select(QMfaBrowser.mfaBrowser.userHandle)
            .from(QMfaBrowser.mfaBrowser)
            .where(QMfaBrowser.mfaBrowser.idUser.eq(userId))
            .fetchOne();
        return Optional.ofNullable(bytes == null ? null : new ByteArray(bytes));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        Long userId = transactionManager.selectQuery()
            .select(QMfaBrowser.mfaBrowser.idUser)
            .from(QMfaBrowser.mfaBrowser)
            .where(QMfaBrowser.mfaBrowser.userHandle.eq(userHandle.getBytes()))
            .fetchOne();
        return Optional.ofNullable(userService.username(userId));
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        MfaBrowser mfa = transactionManager.selectQuery()
            .select(QMfaBrowser.mfaBrowser)
            .from(QMfaBrowser.mfaBrowser)
            .where(QMfaBrowser.mfaBrowser.userHandle.eq(userHandle.getBytes())
                .and(QMfaBrowser.mfaBrowser.keyId.eq(credentialId.getBytes())))
            .fetchOne();
        if (mfa == null) {
            return Optional.empty();
        }
        return Optional.of(
            RegisteredCredential.builder()
                .credentialId(new ByteArray(mfa.getKeyId()))
                .userHandle(new ByteArray(userHandle.getBytes()))
                .publicKeyCose(new ByteArray(mfa.getPublicKeyCose()))
                .signatureCount(mfa.getSignatureCount())
            .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        List<MfaBrowser> enrollements = transactionManager.selectQuery()
            .select(QMfaBrowser.mfaBrowser)
            .from(QMfaBrowser.mfaBrowser)
            .where(QMfaBrowser.mfaBrowser.keyId.eq(credentialId.getBytes()))
            .fetch();
        // Convert to set
        return enrollements.stream()
            .map(mfa -> RegisteredCredential.builder()
                .credentialId(new ByteArray(mfa.getKeyId()))
                .userHandle(new ByteArray(mfa.getKeyId()))
                .publicKeyCose(new ByteArray(mfa.getPublicKeyCose()))
                .signatureCount(mfa.getSignatureCount())
                .build())
            .collect(Collectors.toSet());
    }
}
