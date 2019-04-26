package it.eng.intercenter.oxalis.config.impl;

import java.io.IOException;

import it.eng.intercenter.oxalis.config.ConfigManager;

/**
 * @author Manuel Gozzi
 */
public class EmailSenderConfigManager extends ConfigManager {

	/**
	 * Configuration file name (it needs to be located under /src/main/resources/[filename]).
	 */
	private static final String CONFIGURATION_FILE_NAME = "email-configuration.properties";

	public EmailSenderConfigManager() throws IOException {
		super(CONFIGURATION_FILE_NAME, EmailSenderConfigManager.class);
	}

}
