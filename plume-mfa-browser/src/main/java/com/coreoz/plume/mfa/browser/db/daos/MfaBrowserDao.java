package com.coreoz.plume.mfa.browser.db.daos;

import java.time.Instant;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.coreoz.plume.mfa.browser.db.generated.QMfaBrowser;
import com.coreoz.plume.mfa.browser.db.generated.MfaBrowser;
import com.coreoz.plume.db.querydsl.crud.CrudDaoQuerydsl;
import com.coreoz.plume.db.querydsl.transaction.TransactionManagerQuerydsl;

@Singleton
public class MfaBrowserDao extends CrudDaoQuerydsl<MfaBrowser>  {

    @Inject
	private MfaBrowserDao(TransactionManagerQuerydsl transactionManager) {
		super(transactionManager, QMfaBrowser.mfaBrowser);
	}

	public List<MfaBrowser> findByIdUser(Long idUser) {
		return transactionManager.selectQuery()
			.select(QMfaBrowser.mfaBrowser)
			.from(QMfaBrowser.mfaBrowser)
			.where(QMfaBrowser.mfaBrowser.idUser.eq(idUser))
			.fetch();
	}

}
