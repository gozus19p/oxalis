package it.eng.intercenter.oxalis.integration.dto.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class generate 2 global instances for Gson object.
 * 
 * @author Manuel Gozzi
 */
public class GsonUtil {

	/**
	 * Instance made for parsing object into json String.
	 */
	private static final Gson PRETTY_INSTANCE = new GsonBuilder().setPrettyPrinting().create();
	
	/**
	 * Basic instance.
	 */
	private static final Gson BASIC_INSTANCE = new Gson();

	/**
	 * @return the basic instance (no formatting)
	 */
	public static Gson getInstance() {
		return BASIC_INSTANCE;
	}

	/**
	 * @return the pretty printed instance (format and indent correctly)
	 */
	public static Gson getPrettyPrintedInstance() {
		return PRETTY_INSTANCE;
	}

}
