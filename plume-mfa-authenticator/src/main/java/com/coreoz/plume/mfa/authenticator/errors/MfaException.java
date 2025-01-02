package com.coreoz.plume.mfa.authenticator.errors;

public class MfaException extends RuntimeException {

    private final MfaError error;

    public MfaException(MfaError error) {
        this.error = error;
    }

    public MfaError getError() {
        return error;
    }
}
