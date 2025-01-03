package com.coreoz.plume.mfa.browser.webservices.data;

import com.coreoz.plume.mfa.core.data.UserCredentials;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BrowserPublicKeyCredentials {
    private UserCredentials credentials;
	private String publicKeyCredentialJson;
}
