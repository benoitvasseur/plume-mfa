package com.coreoz.plume.mfa.authenticator.services;

public interface MfaUserService {
    // This method return the userId, only if the username and password are correct
    public Long authenticatedUserId(String username, String password);

    // This method return the userId of the user with the given username
    public Long userId(String username);
}
