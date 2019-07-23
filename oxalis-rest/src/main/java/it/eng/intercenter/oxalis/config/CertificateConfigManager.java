package it.eng.intercenter.oxalis.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import it.eng.intercenter.oxalis.api.AbstractConfigManager;

/**
 * @author Manuel Gozzi
 */
public class CertificateConfigManager extends AbstractConfigManager {

	/**
	 * Configuration file name.
	 *
	 * "cert.properties" for production, "cert.local-test.properties" for local
	 * test.
	 */
	private static final String CONFIGURATION_FILE_NAME = "cert.properties";

	/**
	 * Config keys to access configuration file.
	 */
	public static final String CONFIG_KEY_ORG_CERT_PASSWORD = "cert.org.password";
	public static final String CONFIG_KEY_ORG_CERT_FILE_NAME = "cert.org.filename";
	public static final String CONFIG_KEY_PRODUCTION_MODE_ENABLED = "production.mode";

	/**
	 * Keys used to communicate with Notier.
	 */
	public static final String HEADER_DN_KEY = "X-FwdCertSubject_0";
	public static final String HEADER_SN_KEY = "X-FwdCertSerialNumber_0";

	/**
	 * Certificates directory name located in Oxalis home.
	 */
	private static final String CERTIFICATES_DIRECTORY = "certificates";

	/**
	 * @param oxalisHome holds the Oxalis home path given by Guice context
	 * @throws IOException if something goes wrong with configuration loading
	 */
	@Inject
	public CertificateConfigManager(@Named("home") Path oxalisHome) throws IOException {
		super(CONFIGURATION_FILE_NAME, oxalisHome);
	}

	/**
	 * @return the Path of certificates directory
	 */
	public final Path getCertificatesDirectoryPath() {
		StringBuilder sb = new StringBuilder();
		String separator = System.getProperty("file.separator");
		String oxalisHome = getOxalisHome().normalize().toString();
		sb.append(oxalisHome);
		if (!oxalisHome.substring(oxalisHome.length() - 2).equals(separator)) {
			sb.append(separator);
		}
		sb.append(CERTIFICATES_DIRECTORY);
		return Paths.get(sb.toString());
	}

}
