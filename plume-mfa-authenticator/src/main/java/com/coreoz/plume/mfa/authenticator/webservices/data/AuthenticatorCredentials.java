package com.coreoz.plume.mfa.authenticator.webservices.data;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthenticatorCredentials {

	private String userName;
	private int code;

}
