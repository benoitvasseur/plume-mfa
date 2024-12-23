package com.coreoz.plume.mfa.authenticator.services.encryption;

public interface SecretKeyEncryption {
    public String encrypt(String data) throws Exception ;
    public String decrypt(String data) throws Exception ;
}
