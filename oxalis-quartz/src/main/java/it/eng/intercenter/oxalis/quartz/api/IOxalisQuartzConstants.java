package it.eng.intercenter.oxalis.quartz.api;

import java.text.SimpleDateFormat;

/**
 * @author Manuel Gozzi
 */
public interface IOxalisQuartzConstants {

	SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SSS");
	String HTML_PARAMETER_START = "START";
	String HTML_PARAMETER_STOP = "STOP";
	String KEY_HTML_PARAMETER = "MODE";
	String RESPONSE_CONTENT_TYPE = "text/html;charset=UTF-8";
	String RESOURCE_CSS_FILE_NAME = "presentation.css";

}
