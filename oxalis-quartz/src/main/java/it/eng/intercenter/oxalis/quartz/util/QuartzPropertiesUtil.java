package it.eng.intercenter.oxalis.quartz.util;

import it.eng.intercenter.oxalis.quartz.module.QuartzModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Manuel Gozzi
 * @date 2 ago 2019
 * @time 12:27:40
 */
@Slf4j
public class QuartzPropertiesUtil {

	/**
	 * Constants.
	 */
	public static final String CONFIG_FILE_NAME = "app-config.properties";
	public static final String QUARTZ_PROPRERTIES_FILE_NAME = "quartz.properties";

	/**
	 * Retrieve Quartz properties from file name located in src/main/resources.
	 *
	 * @param resourceStreamFileName is the file name
	 * @return the properties defined in the given file
	 */
	public static Properties getProperties(String resourceStreamFileName) {
		Properties properties = new Properties();
		try {

			properties.load(QuartzModule.class.getClassLoader().getResourceAsStream(resourceStreamFileName));
		} catch (IOException e) {

			log.error("Error: {}", e.getMessage(), e);
		}
		return properties;
	}

}
