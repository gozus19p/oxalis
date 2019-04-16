package it.eng.intercenter.oxalis.rest;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import it.eng.intercenter.oxalis.config.ConfigNotierCertificate;
import it.eng.intercenter.oxalis.rest.http.impl.HttpNotierGet;
import it.eng.intercenter.oxalis.rest.http.impl.HttpNotierPost;

/**
 * This class provides static methods that allows to easily manage REST call.
 * 
 * @author Manuel Gozzi
 */
public class RestManagement {

	public static String executePost(ConfigNotierCertificate certConfig, String uri, String paramKey, String paramValue) throws UnsupportedOperationException, ClientProtocolException, IOException {
		return executePost(certConfig, uri, new BasicNameValuePair(paramKey, paramValue));
	}
	
	public static String executePost(ConfigNotierCertificate certConfig, String uri, BasicNameValuePair... params) throws UnsupportedOperationException, ClientProtocolException, IOException {
		HttpNotierPost request = new HttpNotierPost(certConfig, uri, params);
		return request.execute();
	}

	public static String executeGet(ConfigNotierCertificate certConfig, String uri)
			throws UnsupportedOperationException, ClientProtocolException, IOException {
		HttpNotierGet request = new HttpNotierGet(certConfig, uri);
		return request.execute();
	}

}
