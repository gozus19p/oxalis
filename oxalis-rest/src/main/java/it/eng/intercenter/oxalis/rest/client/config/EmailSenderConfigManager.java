package it.eng.intercenter.oxalis.rest.client.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import it.eng.intercenter.oxalis.rest.client.api.AbstractConfigManager;

/**
 * @author Manuel Gozzi
 */
public class EmailSenderConfigManager extends AbstractConfigManager {

	/**
	 * Configuration file name.
	 */
	private static final String CONFIGURATION_FILE_NAME = "email-configuration.properties";

	/**
	 * Configuration keys.
	 */
	// Separate multiple e-mail addresses with ",".
	public static final String CONFIG_KEY_EMAIL_SENDER = "email.sender";
	public static final String CONFIG_KEY_EMAIL_RECEIVER = "email.receiver.to";
	public static final String CONFIG_KEY_EMAIL_KNOWN_COPY = "email.receiver.cc";
	public static final String CONFIG_KEY_EMAIL_HIDDEN_COPY = "email.receiver.hcc";
	public static final String CONFIG_KEY_S_USERNAME = "email.username";
	public static final String CONFIG_KEY_S_PASSWORD = "email.password";

	/**
	 * @param oxalisHome holds the Oxalis home path given by Guice context
	 * @throws IOException if something goes wrong with configuration loading
	 */
	@Inject
	public EmailSenderConfigManager(@Named("home") Path oxalisHome) {
		super(CONFIGURATION_FILE_NAME, oxalisHome);
	}

	public static final String printConfigurationFileName() {
		return CONFIGURATION_FILE_NAME;
	}

	public static final Properties getPropertiesForSession() {
		Properties prop = new Properties();
		prop.put("mail.smtp.auth", true);
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", "mailinglist.regione.emilia-romagna.it");
		prop.put("mail.smtp.port", "25");
		prop.put("mail.smtp.ssl.trust", "*");
		return prop;
	}

}
