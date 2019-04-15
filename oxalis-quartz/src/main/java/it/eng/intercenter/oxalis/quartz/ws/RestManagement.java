package it.eng.intercenter.oxalis.quartz.ws;

import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_USING_REST_URI;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_WRONG_HTTP_PROTOCOL;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_WRONG_INPUT_OUTPUT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import it.eng.intercenter.oxalis.quartz.config.impl.ConfigNotierCertificate;
import it.eng.intercenter.oxalis.quartz.job.exception.NotierRestCallException;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provides static methods that allows to easily manage REST call.
 * 
 * @author Manuel Gozzi
 */
@Slf4j
public class RestManagement {

	/**
	 * Unchangeable constants.
	 */
	private static final String DN_KEY = "X-FwdCertSubject_0";
	private static final String SN_KEY = "X-FwdCertSerialNumber_0";

	@Inject
	private static ConfigNotierCertificate certConfig;

	/**
	 * Values of p12 certificate.
	 */
	private static String distinguishedName;
	private static String password;
	private static String serialNumber;

	/**
	 * Flag to evaluate for configuration evaluation.
	 */
	private static boolean restConfigurationIsReady = false;
	private static boolean certificateHasBeenLoaded = false;

	/**
	 * Esegue una chiamata REST prendendo in input un URI e restituisce la risposta
	 * in formato stringa.
	 * 
	 * @param restUri is the URI of the destination
	 * @return the response in String format (json)
	 * @throws URISyntaxException      if URI is not set properly
	 * @throws ClientProtocolException if something goes wrong while accessing HTTPS
	 *                                 or HTTP
	 * @throws IOException             if something goes wrong during response or
	 *                                 file management
	 */
	public static String executeRestCallFromURI(String restUri, NotierRestCallTypeEnum restCallType,
			OxalisMdn oxalisMdn) throws NotierRestCallException {
		if (!certificateHasBeenLoaded) {
			loadCertDetails();
		}
		if (!restConfigurationIsReady) {
			throw new NotierRestCallException("Unable to execute REST due to configuration issues");
		}
		try {
			HttpClient client = HttpClients.createDefault();
			log.info(MESSAGE_USING_REST_URI, restUri);

			if (NotierRestCallTypeEnum.GET.equals(restCallType)) {
				HttpGet request = new HttpGet(restUri);
				addCertHeaders(request);

				HttpResponse response = client.execute(request);

				if (response.getStatusLine().getStatusCode() != 200) {
					throw new NotierRestCallException("HTTP " + response.getStatusLine().getStatusCode());
				} else {
					log.info(MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE, response.getStatusLine().getStatusCode());
				}

				return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString());

			} else if (NotierRestCallTypeEnum.POST.equals(restCallType)) {
				HttpPost request = new HttpPost(restUri);
				addCertHeaders(request);

				List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
				postParameters.add(new BasicNameValuePair("oxalisMdnJson",
						new GsonBuilder().setPrettyPrinting().create().toJson(oxalisMdn)));
				request.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
				HttpResponse response = client.execute(request);

				log.info(MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE, response.getStatusLine().getStatusCode());
				return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString());

			} else {
				throw new NotierRestCallException("Rest call type undefined (" + restCallType.toString() + ")!");
			}
		} catch (ClientProtocolException e) {
			log.error(MESSAGE_WRONG_HTTP_PROTOCOL, e.getMessage());
			throw new NotierRestCallException(e.getMessage());
		} catch (IOException e) {
			log.error(MESSAGE_WRONG_INPUT_OUTPUT, e.getMessage());
			throw new NotierRestCallException(e.getMessage());
		}
	}

	/**
	 * This method adds headers to HttpRequest method (this handles both GET and
	 * POST methods).
	 * 
	 * @param request is the HTTP request object
	 */
	private static void addCertHeaders(HttpRequestBase request) {
		request.setHeader(SN_KEY, serialNumber);
		request.setHeader(DN_KEY, distinguishedName);
	}

	/**
	 * This method parse and load certificate.
	 * 
	 * @author Manuel Gozzi
	 */
	private static void loadCertDetails() {
		certificateHasBeenLoaded = true;
		log.info("Preparing certificate for Notier REST communications");
		log.info("Reading password from configuration file");

		// TODO: Correggere e sostituire i valori fissi con il recupero dinamico da file
		// .properties

		password = "o40qrkh3td";
		// certConfig.readSingleProperty("cert.password");
		log.info("Reading certificate path from configuration file");
		String certPath = "C:\\Users\\MGozzi\\Desktop\\ONOTIER.p12";
		// certConfig.readSingleProperty("cert.path");

		log.info("Retrieving certificate file from path {}", certPath);
		File cert = new File(certPath);
		FileInputStream certInputStream;
		try {
			certInputStream = new FileInputStream(cert);

			log.info("Parsing certificate details from file {}", certPath);
			KeyStore p12 = KeyStore.getInstance("pkcs12");
			log.info("Accessing certificate using password");
			p12.load(certInputStream, password.toCharArray());

			Enumeration<String> e = p12.aliases();
			String alias = e.nextElement();
			log.info("Alias found: {}", alias);
			X509Certificate x509Certificate = (X509Certificate) p12.getCertificate(alias);
			distinguishedName = x509Certificate.getSubjectDN().getName();
			serialNumber = x509Certificate.getSerialNumber().toString();
			log.info("The DN is: {}", distinguishedName);
			log.info("The SN is: {}", serialNumber);
			log.info("Certificate loading completed successfully");
			restConfigurationIsReady = true;
		} catch (KeyStoreException e) {
			log.error("An error occurs while accessing KeyStore with root cause: {}", e.getMessage());
			log.error("{}", e);
			return;
		} catch (NoSuchAlgorithmException e) {
			log.error("An error occurs with root cause: {}", e.getMessage());
			log.error("{}", e);
			return;
		} catch (CertificateException e) {
			log.error("Caught exception related to X509Certificate with root cause: {}", e.getMessage());
			log.error("{}", e);
			return;
		} catch (IOException e) {
			log.error("An error occurs during I/O operations with root cause: {}", e.getMessage());
			log.error("{}", e);
			return;
		}
	}

	/**
	 * @return true if REST configuration is ready to process HTTP requests, false
	 *         otherwise (possible problems related to certificate or configuration.
	 */
	public boolean restIsConfigured() {
		return restConfigurationIsReady;
	}

}
