package it.eng.intercenter.oxalis.config;

import java.util.Properties;

/**
 * Definizione dello standard di configurazione per oxalis-quartz.
 * @author Manuel Gozzi
 */
public interface PropertiesConfigurationManager {
	
	public void loadConfiguration();
	public void checkIfConfigurationIsLoaded();
	public Properties getFullConfiguration();
	public String readSingleProperty(String key);
	
}
