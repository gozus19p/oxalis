package it.eng.intercenter.oxalis.config.impl;

import java.io.IOException;

import it.eng.intercenter.oxalis.config.PropertiesConfigurationManager;

/**
 * @author Manuel Gozzi
 */
public class ConfigEmailNotificationSender extends PropertiesConfigurationManager {

	private static final String CONFIGURATION_FILE_NAME = "email-configuration.properties";

	public ConfigEmailNotificationSender() throws IOException {
		super(CONFIGURATION_FILE_NAME, ConfigEmailNotificationSender.class);
	}

}
