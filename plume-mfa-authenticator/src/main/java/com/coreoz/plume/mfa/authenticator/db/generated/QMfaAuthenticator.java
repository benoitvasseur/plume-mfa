package com.coreoz.plume.mfa.authenticator.db.generated;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMfaAuthenticator is a Querydsl query type for MfaAuthenticator
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMfaAuthenticator extends com.querydsl.sql.RelationalPathBase<MfaAuthenticator> {

    private static final long serialVersionUID = 895027019;

    public static final QMfaAuthenticator mfaAuthenticator = new QMfaAuthenticator("PLM_MFA_AUTHENTICATOR");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> idUser = createNumber("idUser", Long.class);

    public final StringPath secretKey = createString("secretKey");

    public final com.querydsl.sql.PrimaryKey<MfaAuthenticator> primary = createPrimaryKey(id);

    public QMfaAuthenticator(String variable) {
        super(MfaAuthenticator.class, forVariable(variable), "null", "PLM_MFA_AUTHENTICATOR");
        addMetadata();
    }

    public QMfaAuthenticator(String variable, String schema, String table) {
        super(MfaAuthenticator.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMfaAuthenticator(String variable, String schema) {
        super(MfaAuthenticator.class, forVariable(variable), schema, "PLM_MFA_AUTHENTICATOR");
        addMetadata();
    }

    public QMfaAuthenticator(Path<? extends MfaAuthenticator> path) {
        super(path.getType(), path.getMetadata(), "null", "PLM_MFA_AUTHENTICATOR");
        addMetadata();
    }

    public QMfaAuthenticator(PathMetadata metadata) {
        super(MfaAuthenticator.class, metadata, "null", "PLM_MFA_AUTHENTICATOR");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(idUser, ColumnMetadata.named("id_user").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(secretKey, ColumnMetadata.named("secret_key").withIndex(2).ofType(Types.VARCHAR).withSize(255));
    }

}

