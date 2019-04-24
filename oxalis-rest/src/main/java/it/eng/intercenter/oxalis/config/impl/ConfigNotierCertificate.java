package it.eng.intercenter.oxalis.config.impl;

import java.io.IOException;

import it.eng.intercenter.oxalis.config.PropertiesConfigurationManager;

/**
 * @author Manuel Gozzi
 */
public class ConfigNotierCertificate extends PropertiesConfigurationManager {

	// "cert.properties" for production.
	// "cert.local-tst.properties" for production.
	private static final String CONFIGURATION_FILE_NAME = "cert.local-test.properties";
	
	public static final String CONFIG_KEY_CERT_PASSWORD = "cert.password";
	public static final String CONFIG_KEY_CERT_PATH = "cert.path";
	public static final String CONFIG_KEY_PRODUCTION_MODE_ENABLED = "production.mode";

	public ConfigNotierCertificate() throws IOException {
		super(CONFIGURATION_FILE_NAME, ConfigNotierCertificate.class);
	}

}
