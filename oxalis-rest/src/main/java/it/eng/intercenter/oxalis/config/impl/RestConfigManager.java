package it.eng.intercenter.oxalis.config.impl;

import java.io.IOException;

import it.eng.intercenter.oxalis.config.ConfigManager;

/**
 * @author Manuel Gozzi
 */
public class RestConfigManager extends ConfigManager {

	/**
	 * Configuration file name (it needs to be located under /src/main/resources/[filename]).
	 */
	private static final String CONFIGURATION_FILE_NAME = "rest-call-configuration.properties";
	
	/**
	 * Keys used to communicate with Notier.
	 */
	public static final String CONFIG_KEY_REST_DOCUMENT_INBOUND = "rest.notier.inbound";
	public static final String CONFIG_KEY_REST_GETTER_URNS = "rest.notier.getter.urns";
	public static final String CONFIG_KEY_REST_GETTER_DOCUMENT = "rest.notier.getter.document";
	public static final String CONFIG_KEY_REST_SENDER_STATUS = "rest.notier.sender.status";

	public RestConfigManager() throws IOException {
		super(CONFIGURATION_FILE_NAME, RestConfigManager.class);
	}

}
