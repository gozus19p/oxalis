package it.eng.intercenter.oxalis.config.impl;

import java.io.IOException;

import it.eng.intercenter.oxalis.config.PropertiesConfigurationManager;

/**
 * Configurazione delle chiamate rest.
 * 
 * @author Manuel Gozzi
 */
public class ConfigRestCall extends PropertiesConfigurationManager {

	private static final String CONFIGURATION_FILE_NAME = "rest-call-configuration.properties";
	
	public static final String CONFIG_KEY_REST_DOCUMENT_INBOUND = "rest.notier.inbound";
	public static final String CONFIG_KEY_REST_GETTER_URNS = "rest.notier.getter.urns";
	public static final String CONFIG_KEY_REST_GETTER_DOCUMENT = "rest.notier.getter.document";
	public static final String CONFIG_KEY_REST_SENDER_STATUS = "rest.notier.sender.status";

	public ConfigRestCall() throws IOException {
		super(CONFIGURATION_FILE_NAME, ConfigRestCall.class);
	}

}
