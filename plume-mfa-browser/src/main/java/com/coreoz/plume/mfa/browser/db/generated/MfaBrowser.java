package com.coreoz.plume.mfa.browser.db.generated;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.processing.Generated;
import com.querydsl.sql.Column;

/**
 * MfaBrowser is a Querydsl bean type
 */
@Generated("com.coreoz.plume.db.querydsl.generation.IdBeanSerializer")
public class MfaBrowser extends com.coreoz.plume.db.querydsl.crud.CrudEntityQuerydsl {

    @Column("attestation")
    private byte[] attestation;

    @Column("client_data_json")
    private byte[] clientDataJson;

    @Column("creation_date")
    private java.time.Instant creationDate;

    @Column("id")
    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long id;

    @Column("id_user")
    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long idUser;

    @Column("is_discoverable")
    private Boolean isDiscoverable;

    @Column("key_id")
    private byte[] keyId;

    @Column("last_used_date")
    private java.time.Instant lastUsedDate;

    @Column("public_key_cose")
    private byte[] publicKeyCose;

    @Column("signature_count")
    private Integer signatureCount;

    @Column("user_handle")
    private byte[] userHandle;

    public byte[] getAttestation() {
        return attestation;
    }

    public void setAttestation(byte[] attestation) {
        this.attestation = attestation;
    }

    public byte[] getClientDataJson() {
        return clientDataJson;
    }

    public void setClientDataJson(byte[] clientDataJson) {
        this.clientDataJson = clientDataJson;
    }

    public java.time.Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(java.time.Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Boolean getIsDiscoverable() {
        return isDiscoverable;
    }

    public void setIsDiscoverable(Boolean isDiscoverable) {
        this.isDiscoverable = isDiscoverable;
    }

    public byte[] getKeyId() {
        return keyId;
    }

    public void setKeyId(byte[] keyId) {
        this.keyId = keyId;
    }

    public java.time.Instant getLastUsedDate() {
        return lastUsedDate;
    }

    public void setLastUsedDate(java.time.Instant lastUsedDate) {
        this.lastUsedDate = lastUsedDate;
    }

    public byte[] getPublicKeyCose() {
        return publicKeyCose;
    }

    public void setPublicKeyCose(byte[] publicKeyCose) {
        this.publicKeyCose = publicKeyCose;
    }

    public Integer getSignatureCount() {
        return signatureCount;
    }

    public void setSignatureCount(Integer signatureCount) {
        this.signatureCount = signatureCount;
    }

    public byte[] getUserHandle() {
        return userHandle;
    }

    public void setUserHandle(byte[] userHandle) {
        this.userHandle = userHandle;
    }

    @Override
    public String toString() {
        return "MfaBrowser#" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (id == null) {
            return super.equals(o);
        }
        if (!(o instanceof MfaBrowser)) {
            return false;
        }
        MfaBrowser obj = (MfaBrowser) o;
        return id.equals(obj.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        return result;
    }

}

