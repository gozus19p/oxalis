package it.eng.intercenter.oxalis.rest.client.config;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import it.eng.intercenter.oxalis.rest.client.api.AbstractConfigManager;

import java.nio.file.Path;

/**
 * @author Manuel Gozzi
 */
public class RestConfigManager extends AbstractConfigManager {

	/**
	 * Configuration file name.
	 */
	private static final String CONFIGURATION_FILE_NAME = "rest-call-configuration.properties";

	/**
	 * Keys used to communicate with Notier.
	 */
	public static final String CONFIG_KEY_PERSIST_MODE = "notier.persist.enabled";
	public static final String CONFIG_KEY_PERSIST_ATTEMPTS = "notier.persist.attempts";
	public static final String CONFIG_KEY_REST_DOCUMENT_INBOUND = "rest.notier.inbound";
	public static final String CONFIG_KEY_REST_GETTER_URNS = "rest.notier.getter.urns";
	public static final String CONFIG_KEY_REST_GETTER_DOCUMENT = "rest.notier.getter.document";
	public static final String CONFIG_KEY_REST_SENDER_STATUS = "rest.notier.sender.status";

	/**
	 * @param oxalisHome holds the Oxalis home path given by Guice context
	 */
	@Inject
	public RestConfigManager(@Named("home") Path oxalisHome) {
		super(CONFIGURATION_FILE_NAME, oxalisHome);
	}

}
