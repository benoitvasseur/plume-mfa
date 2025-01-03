package com.coreoz.plume.mfa.browser.db.generated;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMfaBrowser is a Querydsl query type for MfaBrowser
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMfaBrowser extends com.querydsl.sql.RelationalPathBase<MfaBrowser> {

    private static final long serialVersionUID = 1962344280;

    public static final QMfaBrowser mfaBrowser = new QMfaBrowser("PLM_MFA_BROWSER");

    public final SimplePath<byte[]> attestation = createSimple("attestation", byte[].class);

    public final SimplePath<byte[]> clientDataJson = createSimple("clientDataJson", byte[].class);

    public final DateTimePath<java.time.Instant> creationDate = createDateTime("creationDate", java.time.Instant.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> idUser = createNumber("idUser", Long.class);

    public final BooleanPath isDiscoverable = createBoolean("isDiscoverable");

    public final SimplePath<byte[]> keyId = createSimple("keyId", byte[].class);

    public final DateTimePath<java.time.Instant> lastUsedDate = createDateTime("lastUsedDate", java.time.Instant.class);

    public final SimplePath<byte[]> publicKeyCose = createSimple("publicKeyCose", byte[].class);

    public final NumberPath<Integer> signatureCount = createNumber("signatureCount", Integer.class);

    public final SimplePath<byte[]> userHandle = createSimple("userHandle", byte[].class);

    public final com.querydsl.sql.PrimaryKey<MfaBrowser> primary = createPrimaryKey(id);

    public QMfaBrowser(String variable) {
        super(MfaBrowser.class, forVariable(variable), "null", "PLM_MFA_BROWSER");
        addMetadata();
    }

    public QMfaBrowser(String variable, String schema, String table) {
        super(MfaBrowser.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMfaBrowser(String variable, String schema) {
        super(MfaBrowser.class, forVariable(variable), schema, "PLM_MFA_BROWSER");
        addMetadata();
    }

    public QMfaBrowser(Path<? extends MfaBrowser> path) {
        super(path.getType(), path.getMetadata(), "null", "PLM_MFA_BROWSER");
        addMetadata();
    }

    public QMfaBrowser(PathMetadata metadata) {
        super(MfaBrowser.class, metadata, "null", "PLM_MFA_BROWSER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(attestation, ColumnMetadata.named("attestation").withIndex(5).ofType(Types.LONGVARBINARY).withSize(65535).notNull());
        addMetadata(clientDataJson, ColumnMetadata.named("client_data_json").withIndex(6).ofType(Types.LONGVARBINARY).withSize(65535).notNull());
        addMetadata(creationDate, ColumnMetadata.named("creation_date").withIndex(9).ofType(Types.TIMESTAMP).withSize(19).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(idUser, ColumnMetadata.named("id_user").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(isDiscoverable, ColumnMetadata.named("is_discoverable").withIndex(8).ofType(Types.BOOLEAN).withSize(3));
        addMetadata(keyId, ColumnMetadata.named("key_id").withIndex(3).ofType(Types.LONGVARBINARY).withSize(65535).notNull());
        addMetadata(lastUsedDate, ColumnMetadata.named("last_used_date").withIndex(10).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(publicKeyCose, ColumnMetadata.named("public_key_cose").withIndex(4).ofType(Types.LONGVARBINARY).withSize(65535).notNull());
        addMetadata(signatureCount, ColumnMetadata.named("signature_count").withIndex(11).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(userHandle, ColumnMetadata.named("user_handle").withIndex(7).ofType(Types.LONGVARBINARY).withSize(65535));
    }

}

