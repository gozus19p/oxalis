package it.eng.intercenter.oxalis.rest.client.http;

import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_REST_CALL_FAILED;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.rest.client.http.types.HttpNotierGet;
import it.eng.intercenter.oxalis.rest.client.http.types.HttpNotierPost;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provides static methods that allows to easily manage Http call.
 * 
 * @author Manuel Gozzi
 */
@Slf4j
public class HttpCaller {

	/**
	 * This method executes an HTTP POST call to a given URI, building only one
	 * BasicNameValuePair.
	 * 
	 * @param certConfig is the configuration that holds certificates references
	 * @param uri        is the URI of the HTTP call
	 * @param paramKey   is the parameter key that needs to be insert into
	 *                   BasicNameValuePair
	 * @param paramValue is the parameter value corrisponding to key
	 * @return the response of the HTTP call parsed as String
	 * @throws UnsupportedOperationException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String executePost(CertificateConfigManager certConfig, String uri, String paramKey, String paramValue)
			throws UnsupportedOperationException, ClientProtocolException, IOException {
		return executePost(certConfig, uri, new BasicNameValuePair(paramKey, paramValue));
	}

	/**
	 * This method executes an HTTP POST call to a given URI, receiving "n"
	 * BasicNameValuePair.
	 * 
	 * @param certConfig is the configuration that holds certificates references
	 * @param uri        is the URI of the HTTP call
	 * @param params     are the parameters that needs to be sent on HTTP
	 * @return the response of the HTTP call parsed as String
	 * @throws UnsupportedOperationException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String executePost(CertificateConfigManager certConfig, String uri, BasicNameValuePair... params)
			throws UnsupportedOperationException, ClientProtocolException, IOException {
		HttpNotierPost request = new HttpNotierPost(certConfig, uri, params);
		try{
			return request.execute();
		} catch(UnsupportedOperationException | IOException e) {
			log.error(MESSAGE_REST_CALL_FAILED, e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * This method executes an HTTP GET call to a given URI.
	 * 
	 * @param certConfig is the configuration that holds certificates references
	 * @param uri        is the URI of the HTTP call
	 * @return the response of the HTTP call parsed as String
	 * @throws UnsupportedOperationException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String executeGet(CertificateConfigManager certConfig, String uri)
			throws UnsupportedOperationException, ClientProtocolException, IOException {
		HttpNotierGet request = new HttpNotierGet(certConfig, uri);
		try{
			return request.execute();
		} catch(UnsupportedOperationException | IOException e) {
			log.error(MESSAGE_REST_CALL_FAILED, e.getMessage(), e);
			throw e;
		}
	}

}
