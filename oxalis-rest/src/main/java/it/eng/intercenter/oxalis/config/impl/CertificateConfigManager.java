package it.eng.intercenter.oxalis.config.impl;

import java.io.IOException;

import it.eng.intercenter.oxalis.config.ConfigManager;

/**
 * @author Manuel Gozzi
 */
public class CertificateConfigManager extends ConfigManager {

	// "cert.properties" for production.
	// "cert.local-test.properties" for production.
	/**
	 * Configuration file name (it needs to be located under /src/main/resources/[filename]).
	 */
	private static final String CONFIGURATION_FILE_NAME = "cert.properties";
	
	/**
	 * Config keys to access configuration file.
	 */
	public static final String CONFIG_KEY_HTTPS_CERT_PASSWORD = "cert.cacerts.password";
	public static final String CONFIG_KEY_HTTPS_CERT_PATH = "cert.cacerts.path";
	public static final String CONFIG_KEY_ORG_CERT_PASSWORD = "cert.org.password";
	public static final String CONFIG_KEY_ORG_CERT_PATH = "cert.org.path";
	public static final String CONFIG_KEY_PRODUCTION_MODE_ENABLED = "production.mode";
	
	/**
	 * Keys used to communicate with Notier.
	 */
	public static final String HEADER_DN_KEY = "X-FwdCertSubject_0";
	public static final String HEADER_SN_KEY = "X-FwdCertSerialNumber_0";

	public CertificateConfigManager() throws IOException {
		super(CONFIGURATION_FILE_NAME);
	}

}
