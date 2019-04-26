package it.eng.intercenter.oxalis.config;

import java.io.IOException;
import java.lang.reflect.Field;
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
	private String configurationFileName;
	private boolean configurationIsAvailable;

	/**
	 * Dynamic logger being created using the sub-class "Class" object.
	 */
	private Logger log;

	/**
	 * All the fields that define keys has to start with this prefix.
	 */
	private static final String KEY_PREFIX = "CONFIG_KEY";

	/**
	 * Dynamic constructor.
	 * 
	 * @param configurationFileName is the file name of the subclass
	 * @param clazz                 is the class of the subclass
	 */
	public ConfigManager(String configurationFileName, Class<? extends ConfigManager> clazz) {
		log = LoggerFactory.getLogger(clazz);
		this.configurationFileName = configurationFileName;
		try {
			loadConfiguration(clazz);
			log.info("{} configuration file related to the class {} has been successfully loaded",
					configurationFileName, clazz.getName());
			configurationIsAvailable = true;
			logKeyFields(clazz);
		} catch (IOException e) {
			log.error(
					"Unable to load {} configuration file, the class {} is not available due to a IOException. Root cause: {}",
					configurationFileName, clazz.getName(), e.getMessage(), e);
			configurationIsAvailable = false;
		}
	}

	/**
	 * This method loads the whole configuration.
	 * 
	 * @param clazz is the class of the subclass
	 * @throws IOException if something goes wrong during file parsing
	 */
	private void loadConfiguration(Class<? extends ConfigManager> clazz) throws IOException {
		configuration = new Properties();
		configuration.load(clazz.getClassLoader().getResourceAsStream(configurationFileName));
	}

	/**
	 * This prints keys defined in subclass.
	 * 
	 * @param clazz
	 */
	private void logKeyFields(Class<? extends ConfigManager> clazz) {
		List<Field> fields = getConfigurationKeyFields(clazz);
		log.info("{} configuration file has {} keys", configurationFileName, fields.size());
		fields.forEach(field -> {
			try {
				log.info("Key: \"{}\"; Value: \"{}\";", field.getName(), field.get(clazz));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				log.error("An error occurs during logging, root cause: {}", e.getMessage(), e);
			}
		});
	}

	public List<Field> getConfigurationKeyFields(Class<? extends ConfigManager> clazz) {
		return Arrays.asList(clazz.getDeclaredFields()).stream()
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
	 * @param key is the key that corresponds to the desired value
	 * @return the value
	 */
	public String readValue(String key) {
		if (!configurationIsAvailable) {
			log.warn(ConfigManagerUtil.MESSAGE_READING_PROPERTY_NOT_READY, key);
			return null;
		} else {
			String value = configuration.getProperty(key);
			log.info(ConfigManagerUtil.MESSAGE_READING_PROPERTY, key, value);
			return value;
		}
	}

}
