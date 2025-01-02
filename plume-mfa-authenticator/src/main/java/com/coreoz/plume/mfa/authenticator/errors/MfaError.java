package com.coreoz.plume.mfa.authenticator.errors;

public interface MfaError {

    MfaError NO_USER_FOUND = new MfaErrorInternal("NO_USER_FOUND");
    MfaError ENCRYPTION_ERROR = new MfaErrorInternal("ENCRYPTION_ERROR");
    MfaError GENERATE_KEY_ERROR = new MfaErrorInternal("GENERATE_KEY_ERROR");

    /**
	 * Returns the name of the error
	 */
    String name();

    class MfaErrorInternal implements MfaError {

        private final String name;

        public MfaErrorInternal(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }
    }

}


