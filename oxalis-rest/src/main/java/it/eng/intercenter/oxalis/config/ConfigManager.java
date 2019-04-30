package it.eng.intercenter.oxalis.config;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom configuration abstract class based on Properties technology.
 * 
 * @author Manuel Gozzi
 */
public abstract class ConfigManager {

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
	 * Dynamic logger being created using the sub-class "Class" object.
	 */
	private Logger log;

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
	public ConfigManager(String configurationFileName, Path oxalisHome) {

		this.oxalisHomePath = oxalisHome;
		
		/**
		 * Init logger.
		 */
		log = LoggerFactory.getLogger(this.getClass());

		/**
		 * Init configuration.
		 */
		this.configurationFullPath = Paths.get(buildPath(configurationFileName));

		try {
			loadConfiguration();
			log.info("{} configuration file related to the class {} has been successfully loaded",
					configurationFileName, this.getClass().getName());
			configurationIsAvailable = true;
			logKeyFields();
		} catch (Exception e) {
			log.error(
					"Unable to load {} configuration file, the class {} is not available due to an error. Root cause: {}",
					configurationFileName, this.getClass().getName(), e.getMessage(), e);
			configurationIsAvailable = false;
		}
	}
	
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
		try (FileInputStream inputStream = new FileInputStream(
				new File(configurationFullPath.normalize().toString()));) {
			configuration = new Properties();
			configuration.load(inputStream);
		} catch (IOException e) {
			log.error("An error occurs during {} configuration loading, root cause: {}",
					this.getClass().getSimpleName(), e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			log.error("An unhandled error occurs during {} configuration loading, root cause: {}",
					this.getClass().getSimpleName(), e.getMessage(), e);
			throw e;
		}
		// configuration.load(this.getClass().getClassLoader().getResourceAsStream(configurationFullPath));
	}

	/**
	 * This prints keys defined in subclass.
	 * 
	 * @param clazz
	 */
	private void logKeyFields() {
		List<Field> fields = getConfigurationKeyFields();
		log.info("{} configuration file has {} keys", configurationFullPath, fields.size());
		fields.forEach(field -> {
			try {
				log.info("Key field: \"{}\"; Value: \"{}\";", field.getName(), field.get(this.getClass()));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				log.error("An error occurs during logging, root cause: {}", e.getMessage(), e);
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
				.filter(field -> field.getType().equals(String.class)
						&& field.getName().toUpperCase().contains(KEY_PREFIX.toUpperCase()))
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
	 * @return the value
	 */
	public String readValue(String key, boolean hideValue) {
		if (!configurationIsAvailable) {
			log.warn(ConfigManagerUtil.MESSAGE_READING_PROPERTY_NOT_READY, key);
			return null;
		} else {
			String value = configuration.getProperty(key);
			log.info(ConfigManagerUtil.MESSAGE_READING_PROPERTY, key, hideValue ? "*****" : value);
			return value;
		}
	}

	/**
	 * Overload of previous method that reads a configuration value logging its
	 * retrieved value.
	 * 
	 * @param key is the key that corresponds to the desired value
	 * @return the value
	 */
	public String readValue(String key) {
		return readValue(key, false);
	}

}
