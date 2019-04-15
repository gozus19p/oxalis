package it.eng.intercenter.oxalis.quartz.config.impl;

import java.io.IOException;
import java.util.Properties;

import it.eng.intercenter.oxalis.quartz.config.PropertiesConfigurationManager;

public class ConfigNotierCertificate implements PropertiesConfigurationManager {

	private Properties fullCertNotierConfiguration;
	private static final String CONFIGURATION_FILE_NAME = "cert.properties";
	
	/**
	 * Carica la configurazione.
	 */
	@Override
	public void loadConfiguration() {
		try {
			fullCertNotierConfiguration = new Properties();
			fullCertNotierConfiguration.load(ConfigNotierCertificate.class.getClassLoader().getResourceAsStream(CONFIGURATION_FILE_NAME));
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}
	
	/**
	 * Se la configurazione è null la ricarica.
	 */
	@Override
	public void checkIfConfigurationIsLoaded() {
		if (fullCertNotierConfiguration == null) loadConfiguration();
	}
	
	/**
	 * Recupera la configurazione dei rest completa.
	 * @return
	 */
	@Override
	public Properties getFullConfiguration() {
		checkIfConfigurationIsLoaded();
		return fullCertNotierConfiguration;
	}
	
	/**
	 * Legge una singola proprietà della configurazione.
	 * @param key
	 * @return
	 */
	@Override
	public String readSingleProperty(String key) {
		checkIfConfigurationIsLoaded();
		return fullCertNotierConfiguration.getProperty(key);
	}

}
