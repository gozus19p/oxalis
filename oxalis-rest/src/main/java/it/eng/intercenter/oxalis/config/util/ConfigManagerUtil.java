package it.eng.intercenter.oxalis.config.util;

import java.util.Map.Entry;

import it.eng.intercenter.oxalis.api.AbstractConfigManager;

import java.util.Set;

/**
 * @author Manuel Gozzi
 */
public class ConfigManagerUtil {

	/**
	 * Constants used to write logs.
	 */
	public static final String MESSAGE_MDN_SEND_FAILED = "MDN has not been sent to Notier for URN: {}";
	public static final String MESSAGE_OUTBOUND_FAILED_FOR_URN = "Outbound process failed for URN: {}";
	public static final String MESSAGE_OUTBOUND_SUCCESS_FOR_URN = "Outbound process completed succesfully for URN: {}";
	public static final String MESSAGE_READING_PROPERTY = "Accessing configuration with key \"{}\", extracted value is \"{}\"";
	public static final String MESSAGE_READING_PROPERTY_NOT_READY = "Accessing unavailable configuration with key \"{}\", extracted value is null";
	public static final String MESSAGE_REST_CALL_FAILED = "Something went wrong during REST call execution, message: {}";
	public static final String MESSAGE_REST_EXECUTED_WITH_STATUS = "REST call executed. HTTP status: {}";
	public static final String MESSAGE_STARTING_TO_PROCESS_URN = "Starting to process URN: {}";
	public static final String MESSAGE_USING_REST_URI = "Executing HTTP {} REST call to URI: {}";
	public static final String MESSAGE_PRODUCTION_MODE_DISABLED = "Oxalis is running in TEST mode";
	public static final String MESSAGE_WRONG_HTTP_PROTOCOL = "Something went wrong with HttpClient protocol, message: {}";
	public static final String MESSAGE_WRONG_INPUT_OUTPUT = "Something went wrong with input/output, message: {}";
	public static final String MESSAGE_WRONG_CONFIGURATION_SETUP = "Rest configuration has not been setup properly. Configuration status: {}";

	/**
	 * A custom definition of toString() method.
	 * 
	 * @param config is the configuration that needs to be printed.
	 * @return the String format of configuration
	 */
	public static <T extends AbstractConfigManager> String printConfigToString(T config) {
		StringBuilder sb = new StringBuilder();
		Set<Entry<Object, Object>> entries = config.getFullConfiguration().entrySet();
		entries.stream().forEach((entry) -> sb
				.append(entry.getKey().toString() + " = " + entry.getValue() + System.getProperty("line.separator")));
		return sb.toString();
	}

}
