package com.coreoz.plume.mfa.core.services;

public interface MfaUserService {
    // This method return the userId, only if the username and password are correct
    public Long authenticatedUserId(String username, String password);

    // This method return the userId of the user with the given username
    public Long userId(String username);

    // This method return the username of the user with the given userId
    public String username(Long userId);
}
