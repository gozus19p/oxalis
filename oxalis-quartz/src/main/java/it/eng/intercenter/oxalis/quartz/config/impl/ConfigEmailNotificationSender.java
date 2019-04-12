package it.eng.intercenter.oxalis.quartz.config.impl;

import java.io.IOException;
import java.util.Properties;

import it.eng.intercenter.oxalis.quartz.config.PropertiesConfigurationManager;

/**
 * 
 * @author Manuel Gozzi
 */
public class ConfigEmailNotificationSender implements PropertiesConfigurationManager {

	private Properties fullEmailNotificationSenderConfiguration;
	private static final String CONFIGURATION_FILE_NAME = "email-configuration.properties";
	
	@Override
	public void loadConfiguration() {
		try {
			fullEmailNotificationSenderConfiguration.load(ConfigRestCall.class.getClassLoader().getResourceAsStream(CONFIGURATION_FILE_NAME));
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}

	@Override
	public void checkIfConfigurationIsLoaded() {
		if (fullEmailNotificationSenderConfiguration == null) loadConfiguration();
	}

	@Override
	public Properties getFullConfiguration() {
		checkIfConfigurationIsLoaded();
		return fullEmailNotificationSenderConfiguration;
	}

	@Override
	public String readSingleProperty(String key) {
		checkIfConfigurationIsLoaded();
		return fullEmailNotificationSenderConfiguration.getProperty(key);
	}

}
