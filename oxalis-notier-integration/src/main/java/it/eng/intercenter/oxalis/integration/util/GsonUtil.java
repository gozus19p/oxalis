package it.eng.intercenter.oxalis.integration.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class generate 2 global instances for Gson object.
 * 
 * @author Manuel Gozzi
 */
public class GsonUtil {

	/**
	 * @return the basic instance (no formatting)
	 */
	public static synchronized Gson getInstance() {
		return new Gson();
	}

	/**
	 * @return the pretty printed instance (format and indent correctly)
	 */
	public static synchronized Gson getPrettyPrintedInstance() {
		return new GsonBuilder().setPrettyPrinting().create();
	}

}
