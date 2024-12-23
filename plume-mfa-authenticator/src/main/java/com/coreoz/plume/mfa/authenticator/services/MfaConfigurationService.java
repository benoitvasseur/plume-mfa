package com.coreoz.plume.mfa.authenticator.services;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Singleton
public class MfaConfigurationService {

	private final Config config;

	@Inject
	public MfaConfigurationService(Config config) {
		// the reference file is not located in src/main/resources/ to ensure
		// that it is not overridden by another config file when a "fat jar" is created.
		this.config = config.withFallback(
			ConfigFactory.parseResources(MfaConfigurationService.class, "reference.conf")
		);
	}

    public String mfaSecret() {
        return config.getString("mfa.secret");
    }

	public String appName() {
		return config.getString("mfa.appname");
	}
}
