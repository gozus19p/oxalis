package it.eng.intercenter.oxalis.rest.client.config;

import java.io.IOException;
import java.nio.file.Path;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import it.eng.intercenter.oxalis.rest.client.api.AbstractConfigManager;

/**
 * @author Manuel Gozzi
 * @date 28 feb 2020
 * @time 10:23:21
 */
public class QuartzConfigManager extends AbstractConfigManager {

	/**
	 * Configuration file name.
	 */
	private static final String CONFIGURATION_FILE_NAME = "quartz.properties";

	/**
	 * @param oxalisHome holds the Oxalis home path given by Guice context
	 * @throws IOException if something goes wrong with configuration loading
	 */
	@Inject
	public QuartzConfigManager(@Named("home") Path oxalisHome) {
		super(CONFIGURATION_FILE_NAME, oxalisHome);
	}
}
