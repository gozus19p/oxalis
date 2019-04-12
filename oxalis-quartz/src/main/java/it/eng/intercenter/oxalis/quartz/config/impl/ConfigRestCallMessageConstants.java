package it.eng.intercenter.oxalis.quartz.config.impl;

public class ConfigRestCallMessageConstants {
	
	public static final String MESSAGE_MDN_SEND_FAILED = "MDN has not been sent to Notier for URN: {}";
	public static final String MESSAGE_OUTBOUND_FAILED_FOR_URN = "Outbound process failed for URN: {}";
	public static final String MESSAGE_OUTBOUND_SUCCESS_FOR_URN = "Outbound process completed succesfully for URN: {}";
	public static final String MESSAGE_READING_PROPERTY = "Reading configuration value defined for key: {}";
	public static final String MESSAGE_REST_CALL_FAILED = "Something went wrong during REST call execution, message: {}";
	public static final String MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE = "REST call executed succesfully, got response status: {}";
	public static final String MESSAGE_STARTING_TO_PROCESS_URN = "Starting to process URN: {}";
	public static final String MESSAGE_USING_REST_URI = "Executing REST call to URI: {}";
	public static final String MESSAGE_WRONG_HTTP_PROTOCOL = "Something went wrong with HttpClient protocol, message: {}";
	public static final String MESSAGE_WRONG_INPUT_OUTPUT = "Something went wrong with input/output, message: {}";
	public static final String MESSAGE_WRONG_CONFIGURATION_SETUP = "Rest configuration has not been setup properly. Configuration status: {}";

}
