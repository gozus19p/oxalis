package it.eng.intercenter.oxalis.rest.http;

import static it.eng.intercenter.oxalis.config.ConfigRestCallMessageConstants.MESSAGE_REST_STRING;
import static it.eng.intercenter.oxalis.config.ConfigRestCallMessageConstants.MESSAGE_USING_REST_URI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;

import it.eng.intercenter.oxalis.config.ConfigNotierCertificate;
import it.eng.intercenter.oxalis.config.ConfigRestCallMessageConstants;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public abstract class HttpNotierCall<T extends HttpRequestBase> {

	private ConfigNotierCertificate certConfig;
	private HttpClient client;
	protected T request;
	protected NotierRestCallTypeEnum requestType;

	/**
	 * Values of p12 certificate.
	 */
	private static String distinguishedName;
	private static String password;
	private static String serialNumber;
	private static KeyStore keyStoreP12;
	private Boolean isProductionMode;

	private static final String DN_KEY = "X-FwdCertSubject_0";
	private static final String SN_KEY = "X-FwdCertSerialNumber_0";

	public HttpNotierCall(ConfigNotierCertificate certConfig) {
		this.certConfig = certConfig;
		isProductionMode = detectProductionMode();
		loadCertDetails();
		client = HttpClients.custom().setSSLContext(getSSLContext()).build();
	}

	/**
	 * 
	 */
	private Boolean detectProductionMode() {
		String propertyValue = certConfig.readSingleProperty("production.mode");
		return new Boolean(propertyValue);
	}

	/**
	 * Executes the HTTP call.
	 * 
	 * @return the content of the response in String format
	 * @throws UnsupportedOperationException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String execute() throws UnsupportedOperationException, ClientProtocolException, IOException {
		if (!isProductionMode) {
			addCertHeaders();
		}
		log.info(MESSAGE_USING_REST_URI, new Object[] { requestType.name(), request.getURI().normalize().toString() });
		HttpResponse response = client.execute(request);
		log.info(MESSAGE_REST_STRING, response.getStatusLine().getStatusCode());
		return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString());
	}

	/**
	 * @return SSLContext used to execute HTTP calls.
	 */
	private SSLContext getSSLContext() {
		try {
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(keyStoreP12, password.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init((KeyStore) null);
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			return context;
		} catch (KeyStoreException e) {
			log.error("Found a problem on the KeyStore: {}", e.getMessage());
			log.error("{}", e);
		} catch (KeyManagementException e) {
			log.error("Found a problem on the key management: {}", e.getMessage());
			log.error("{}", e);
		} catch (NoSuchAlgorithmException e) {
			log.error("Found a problem: {}", e.getMessage());
			log.error("{}", e);
		} catch (UnrecoverableKeyException e) {
			log.error("Found a problem: {}", e.getMessage());
			log.error("{}", e);
		}
		return null;
	}

	/**
	 * Method that loads details of the PKCS12 certificate.
	 */
	private void loadCertDetails() {
		log.info("Preparing certificate for Notier REST communications");
		log.info("Reading password from configuration file");

		password = certConfig.readSingleProperty("cert.password");
		// "o40qrkh3td";
		log.info(ConfigRestCallMessageConstants.MESSAGE_READING_PROPERTY, "cert.password");
		String certPath = certConfig.readSingleProperty("cert.path");
		// "C:\\Users\\MGozzi\\Desktop\\ONOTIER.p12";

		log.info(ConfigRestCallMessageConstants.MESSAGE_READING_PROPERTY, "cert.path");
		log.info("Retrieving certificate file from path {}", certPath);
		File cert = new File(certPath);
		FileInputStream certInputStream;
		try {
			certInputStream = new FileInputStream(cert);

			log.info("Parsing certificate details from file {}", certPath);
			keyStoreP12 = KeyStore.getInstance("pkcs12");
			log.info("Accessing certificate using password");
			keyStoreP12.load(certInputStream, password.toCharArray());

			Enumeration<String> e = keyStoreP12.aliases();
			String alias = e.nextElement();
			log.info("Alias found: {}", alias);
			X509Certificate x509Certificate = (X509Certificate) keyStoreP12.getCertificate(alias);
			distinguishedName = x509Certificate.getSubjectDN().getName();
			serialNumber = x509Certificate.getSerialNumber().toString();
			log.info("The DN is: {}", distinguishedName);
			log.info("The SN is: {}", serialNumber);
			log.info("Certificate loaded successfully");
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
	 * This method adds headers to HttpRequest method (this handles both GET and
	 * POST methods).
	 * 
	 * @param request is the HTTP request object
	 */
	private void addCertHeaders() {
		request.setHeader(SN_KEY, serialNumber);
		request.setHeader(DN_KEY, distinguishedName);
	}

}
