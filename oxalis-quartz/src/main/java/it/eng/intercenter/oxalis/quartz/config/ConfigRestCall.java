package it.eng.intercenter.oxalis.quartz.config;

import java.io.IOException;
import java.util.Properties;

/**
 * Configurazione delle chiamate rest.
 * @author Manuel Gozzi
 */
public class ConfigRestCall implements PropertiesConfigurationManager {

	private Properties fullRestCallConfiguration;
	private static final String CONFIGURATION_FILE_NAME = "rest-call-configuration.properties";
	
	public static final String CONFIG_KEY_REST_GETTER_URNS = "rest.notier.getter.urns";
	public static final String CONFIG_KEY_REST_GETTER_DOCUMENT = "rest.notier.getter.document";
	public static final String CONFIG_KEY_REST_SENDER_STATUS = "rest.notier.sender.status";
	
	/**
	 * Carica la configurazione.
	 */
	@Override
	public void loadConfiguration() {
		try {
			fullRestCallConfiguration.load(ConfigRestCall.class.getClassLoader().getResourceAsStream(CONFIGURATION_FILE_NAME));
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}
	
	/**
	 * Se la configurazione è null la ricarica.
	 */
	@Override
	public void checkIfConfigurationIsLoaded() {
		if (fullRestCallConfiguration == null) loadConfiguration();
	}
	
	/**
	 * Recupera la configurazione dei rest completa.
	 * @return
	 */
	@Override
	public Properties getFullConfiguration() {
		checkIfConfigurationIsLoaded();
		return fullRestCallConfiguration;
	}
	
	/**
	 * Legge una singola proprietà della configurazione.
	 * @param key
	 * @return
	 */
	@Override
	public String readSingleProperty(String key) {
		checkIfConfigurationIsLoaded();
		return fullRestCallConfiguration.getProperty(key);
	}
	
}
