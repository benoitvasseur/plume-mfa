package com.coreoz.plume.mfa.authenticator.db.daos;

import java.time.Instant;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.mfa.authenticator.db.generated.MfaAuthenticator;
import com.coreoz.plume.mfa.authenticator.db.generated.QMfaAuthenticator;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

@Singleton
public class MfaAuthenticatorDao extends CrudDaoQuerydsl<MfaAuthenticator> {

    @Inject
	private MfaAuthenticatorDao(TransactionManagerQuerydsl transactionManager) {
		super(transactionManager, QMfaAuthenticator.mfaAuthenticator);
	}

	public List<MfaAuthenticator> findByIdUser(Long idUser) {
		return transactionManager.selectQuery()
			.select(QMfaAuthenticator.mfaAuthenticator)
			.from(QMfaAuthenticator.mfaAuthenticator)
			.where(QMfaAuthenticator.mfaAuthenticator.idUser.eq(idUser))
			.fetch();
	}

	/**
	 * Find all MfaAuthenticator that are not enabled and have a creation date older than the provided date
	 */
	public List<MfaAuthenticator> findDisabledAndOlderThan(Instant date) {
		return transactionManager.selectQuery()
			.select(QMfaAuthenticator.mfaAuthenticator)
			.from(QMfaAuthenticator.mfaAuthenticator)
			.where(
				QMfaAuthenticator.mfaAuthenticator.isEnabled.isFalse()
				.and(QMfaAuthenticator.mfaAuthenticator.creationDate.before(date))
			)
			.fetch();

	}
}
