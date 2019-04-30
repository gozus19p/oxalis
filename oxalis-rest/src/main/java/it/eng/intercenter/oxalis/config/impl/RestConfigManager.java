package it.eng.intercenter.oxalis.config.impl;

import java.io.IOException;
import java.nio.file.Path;

import com.google.inject.Inject;
import com.google.inject.name.Named;

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

	/**
	 * @param oxalisHome holds the Oxalis home path given by Guice context
	 * @throws IOException if something goes wrong with configuration loading
	 */
	@Inject
	public RestConfigManager(@Named("home") Path oxalisHome) throws IOException {
		super(CONFIGURATION_FILE_NAME, oxalisHome);
	}

}
