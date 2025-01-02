package com.coreoz.plume.mfa.authenticator.db.generated;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.processing.Generated;
import com.querydsl.sql.Column;

/**
 * MfaAuthenticator is a Querydsl bean type
 */
@Generated("com.coreoz.plume.db.querydsl.generation.IdBeanSerializer")
public class MfaAuthenticator extends com.coreoz.plume.db.querydsl.crud.CrudEntityQuerydsl {

    @Column("creation_date")
    private java.time.Instant creationDate;

    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    @Column("id")
    private Long id;

    @JsonSerialize(using=com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    @Column("id_user")
    private Long idUser;

    @Column("is_enabled")
    private Boolean isEnabled;

    @Column("last_used_date")
    private java.time.Instant lastUsedDate;

    @Column("secret_key")
    private String secretKey;

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

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public java.time.Instant getLastUsedDate() {
        return lastUsedDate;
    }

    public void setLastUsedDate(java.time.Instant lastUsedDate) {
        this.lastUsedDate = lastUsedDate;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String toString() {
        return "MfaAuthenticator#" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (id == null) {
            return super.equals(o);
        }
        if (!(o instanceof MfaAuthenticator)) {
            return false;
        }
        MfaAuthenticator obj = (MfaAuthenticator) o;
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

