package it.eng.intercenter.oxalis.config;

import java.io.IOException;
import java.nio.file.Path;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import it.eng.intercenter.oxalis.api.AbstractConfigManager;

/**
 * @author Manuel Gozzi
 */
public class EmailSenderConfigManager extends AbstractConfigManager {

	/**
	 * Configuration file name.
	 */
	private static final String CONFIGURATION_FILE_NAME = "email-configuration.properties";

	/**
	 * @param oxalisHome holds the Oxalis home path given by Guice context
	 * @throws IOException if something goes wrong with configuration loading
	 */
	@Inject
	public EmailSenderConfigManager(@Named("home") Path oxalisHome) throws IOException {
		super(CONFIGURATION_FILE_NAME, oxalisHome);
	}

}
