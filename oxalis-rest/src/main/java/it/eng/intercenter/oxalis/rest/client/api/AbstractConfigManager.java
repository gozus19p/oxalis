package it.eng.intercenter.oxalis.rest.client.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom configuration abstract class based on Properties technology.
 *
 * @author Manuel Gozzi
 */
@Slf4j
public abstract class AbstractConfigManager {

	/**
	 * Configuration fields.
	 */
	private Properties configuration;
	private Path configurationFullPath;
	private boolean configurationIsAvailable;

	/**
	 * The directory that holds all Properties configuration files is made
	 * with @Named("home"), that is the Oxalis home directory, plus this and the
	 * properties file name. I.e.
	 * "[oxalis_home_path]/[PROPERTIES_CONFIGURATION_DIRECTORY_NAME]/conf.file.properties"
	 * the usage of "System.getProperty("file.separator")" allows interoperability
	 * between Unix systems and Windows systems (base configuration path needs to be
	 * correct).
	 */
	private static final String PROPERTIES_CONFIGURATION_DIRECTORY_NAME = "notier-integration";

	/**
	 * This holds Oxalis home directory path reference.
	 */
	private Path oxalisHomePath;

	/**
	 * All the fields that define keys must start with this prefix.
	 */
	private static final String KEY_PREFIX = "CONFIG_KEY";

	/**
	 * Dynamic constructor.
	 *
	 * @param configurationFileName is the file name of the subclass
	 * @param clazz                 is the class of the subclass
	 */
	public AbstractConfigManager(String configurationFileName, Path oxalisHome) {

		// Saving Oxalis home path reference.
		this.oxalisHomePath = oxalisHome;

		// Init configuration.
		this.configurationFullPath = Paths.get(buildPath(configurationFileName));

		try {
			log.info("Loading process of \"{}\" configuration started", this.getClass().getSimpleName());
			loadConfiguration();
			log.info("{} configuration file related to the class {} has been successfully loaded", configurationFileName, this.getClass().getSimpleName());
			configurationIsAvailable = true;
			logKeyFields();
		} catch (Exception e) {
			log.error("Unable to load {} configuration file, the class {} is not available due to an error. Root cause: {}", configurationFileName,
					this.getClass().getName(), e.getMessage(), e);
			configurationIsAvailable = false;
		}
	}

	/**
	 * @return Oxalis home path
	 */
	protected Path getOxalisHome() {
		return oxalisHomePath;
	}

	/**
	 * This builds the full path of configuration that need to be load.
	 *
	 * @param configurationFileName is the name of the configuration file
	 * @return the full Path as String
	 */
	private String buildPath(String configurationFileName) {
		StringBuilder sb = new StringBuilder();
		sb.append(oxalisHomePath);
		String separator = System.getProperty("file.separator");
		if (!oxalisHomePath.normalize().toString().substring(oxalisHomePath.normalize().toString().length() - 2).equals(separator)) {
			sb.append(separator);
		}
		sb.append(PROPERTIES_CONFIGURATION_DIRECTORY_NAME);
		sb.append(separator);
		sb.append(configurationFileName);
		return sb.toString();
	}

	/**
	 * This method loads the whole configuration.
	 *
	 * @param clazz is the class of the subclass
	 * @throws IOException if something goes wrong during file parsing
	 */
	private void loadConfiguration() throws IOException, Exception {
		try (FileInputStream inputStream = new FileInputStream(new File(configurationFullPath.normalize().toString()));) {
			configuration = new Properties();
			configuration.load(inputStream);
		} catch (IOException e) {
			log.error("An error occurs during {} class configuration loading. Root cause: {}", this.getClass().getSimpleName(), e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			log.error("An unhandled error occurs during {} configuration loading. Root cause: {}", this.getClass().getSimpleName(), e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * It prints keys defined in subclass.
	 */
	private void logKeyFields() {
		List<Field> fields = getConfigurationKeyFields();
		log.debug("{} configuration file has {} keys", configurationFullPath, fields.size());
		fields.forEach(field -> {
			try {
				String key = (String) field.get(this.getClass());
				log.info("\"{}\" --> \"{}\"", key, configuration.getProperty(key));
				log.debug("Key field name (Java): \"{}\"; Key: \"{}\";", field.getName(), field.get(this.getClass()));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				log.debug("An error occurs during logging. Root cause is: \"{}\"", e.getMessage(), e);
			}
		});
	}

	/**
	 * Retrieve configuration key fields of current subclass.
	 *
	 * @param clazz is the subclass
	 * @return the Field's list
	 */
	public List<Field> getConfigurationKeyFields() {
		return Arrays.asList(this.getClass().getDeclaredFields()).stream()
				.filter(field -> field.getType().equals(String.class) && field.getName().toUpperCase().contains(KEY_PREFIX.toUpperCase()))
				.collect(Collectors.toList());
	}

	/**
	 * @return the whole configuration
	 */
	public Properties getFullConfiguration() {
		return configuration;
	}

	/**
	 * This reads a value from the configuration.
	 *
	 * @param key       is the key that corresponds to the desired value
	 * @param hideValue "true" if the value that has been read must not be logged,
	 *                  "false" otherwise
	 * @return the value as String
	 */
	public String readValue(String key, boolean hideValue) {
		if (!configurationIsAvailable) {
			log.warn(ConfigManagerUtil.MESSAGE_READING_PROPERTY_NOT_READY, key);
			return null;
		}
		String value = configuration.getProperty(key);
		log.debug(ConfigManagerUtil.MESSAGE_READING_PROPERTY, key, hideValue ? "*****" : value);
		return value;
	}

	/**
	 * Overload of previous method that reads a configuration value logging its
	 * retrieved value.
	 *
	 * @param key is the key that corresponds to the desired value
	 * @return the value as String
	 */
	public String readValue(String key) {
		return readValue(key, false);
	}

}
