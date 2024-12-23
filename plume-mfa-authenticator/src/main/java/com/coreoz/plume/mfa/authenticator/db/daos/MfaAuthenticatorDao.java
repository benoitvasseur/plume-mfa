package com.coreoz.plume.mfa.authenticator.db.daos;

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
}
