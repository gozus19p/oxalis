package it.eng.intercenter.oxalis.rest.http;

import static it.eng.intercenter.oxalis.config.ConfigManagerUtil.MESSAGE_REST_EXECUTED_WITH_STATUS;
import static it.eng.intercenter.oxalis.config.ConfigManagerUtil.MESSAGE_USING_REST_URI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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

import it.eng.intercenter.oxalis.config.impl.CertificateConfigManager;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public abstract class HttpNotierCall<T extends HttpRequestBase> {

	private CertificateConfigManager certConfig;
	private HttpClient client;
	protected T request;
	protected NotierRestCallTypeEnum requestType;

	private boolean clientIsAvailable;
	
	/**
	 * Values of p12 certificate.
	 */
	private static boolean isProductionMode;
	private static String organizationCertificateP12Path;
	private static String distinguishedName;
	private static String organizationCertificateP12Password;
	private static String serialNumber;
	private static KeyStore organizationCertificateP12;
	private static X509Certificate x509Certificate;

	/**
	 * Constructor.
	 * 
	 * @param certConfig is the configuration that holds certificate details
	 */
	public HttpNotierCall(CertificateConfigManager certConfig) {
		this.certConfig = certConfig;
		isProductionMode = detectProductionMode();
		loadCertificates();
		try {
			client = HttpClients.custom().setSSLContext(getSSLContext()).build();
		} catch (KeyManagementException | KeyStoreException e) {
			log.error("Some errors occur on Keystore management");
		} catch (Exception e) {
			log.error("Some errors occur during HttpNotierCall creation");
		}
	}

	/**
	 * @return tue if Oxalis has been set up to run in production, false otherwise
	 */
	private boolean detectProductionMode() {
		return new Boolean(certConfig.readValue(CertificateConfigManager.CONFIG_KEY_PRODUCTION_MODE_ENABLED))
				.booleanValue();
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
		if (!clientIsAvailable) {
			throw new IOException("HttpClient is not available");
		}
		if (!isProductionMode) {
			addDistinguishedNameAndSerialNumberToRequestHeaders();
		}
		log.info(MESSAGE_USING_REST_URI, new Object[] { requestType.name(), request.getURI().normalize().toString() });
		HttpResponse response = client.execute(request);
		log.info(MESSAGE_REST_EXECUTED_WITH_STATUS, response.getStatusLine().getStatusCode());
		return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString());
	}
	
	private KeyStore getTrustStore() throws KeyStoreException, Exception  {
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		String trustStorePath = certConfig.readValue(CertificateConfigManager.CONFIG_KEY_HTTPS_CERT_PATH);
		String trustStorePassword = certConfig.readValue(CertificateConfigManager.CONFIG_KEY_HTTPS_CERT_PASSWORD);
		try (FileInputStream trustInputStream = new FileInputStream(trustStorePath);){
			trustStore.load(trustInputStream, trustStorePassword.toCharArray());
		} catch (Exception e) {
			throw e;
		}
		return trustStore;
	}

	/**
	 * @return SSLContext used to execute HTTP calls.
	 */
	private SSLContext getSSLContext() throws KeyStoreException, KeyManagementException, Exception {
		try {
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); // SunX509
			kmf.init(organizationCertificateP12, organizationCertificateP12Password.toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			tmf.init(getTrustStore());
			
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			clientIsAvailable = true;
			return context;
		} catch (KeyStoreException e) {
			log.error("Found a problem on the KeyStore: {}", e.getMessage(), e);
			clientIsAvailable = false;
			throw e;
		} catch (KeyManagementException e) {
			log.error("Found a problem on the key management: {}", e.getMessage(), e);
			clientIsAvailable = false;
			throw e;
		} catch (Exception e) {
			log.error("Found a problem: {}", e.getMessage(), e);
			clientIsAvailable = false;
			throw e;
		}
	}

	/**
	 * Method that loads details of the PKCS12 certificate.
	 */
	private void loadCertificates() {
		log.info("Preparing certificate for Notier REST communications");
		log.info("Reading password from configuration file");

		organizationCertificateP12Path = certConfig.readValue(CertificateConfigManager.CONFIG_KEY_ORG_CERT_PATH);
		organizationCertificateP12Password = certConfig.readValue(CertificateConfigManager.CONFIG_KEY_ORG_CERT_PASSWORD);

		log.info("Retrieving certificate file from path {}", organizationCertificateP12Path);

		try (FileInputStream certInputStream = new FileInputStream(new File(organizationCertificateP12Path));) {

			log.info("Parsing certificate details from file {}", organizationCertificateP12Path);
			organizationCertificateP12 = KeyStore.getInstance("PKCS12"); // PKCS12
			log.info("Accessing certificate using password");
			organizationCertificateP12.load(certInputStream, organizationCertificateP12Password.toCharArray());

			Enumeration<String> e = organizationCertificateP12.aliases();
			String alias = e.nextElement();
			log.info("Using alias: {}", alias);
			
			if (e.hasMoreElements()) {
				StringBuilder otherAliases = new StringBuilder();
				while (e.hasMoreElements()) {
					otherAliases.append(e.nextElement() + "; ");
				}
				log.info("Other aliases found: {}", otherAliases.toString().trim());
			}

			x509Certificate = (X509Certificate) organizationCertificateP12.getCertificate(alias);

			distinguishedName = x509Certificate.getSubjectDN().getName();
			serialNumber = x509Certificate.getSerialNumber().toString();

			log.info("DN: {}; SN: {};", distinguishedName, serialNumber);
			log.info("Certificate loaded successfully");

		} catch (KeyStoreException e) {
			log.error("An error occurs while accessing KeyStore with root cause: {}", e.getMessage(), e);
			clientIsAvailable = false;
		} catch (NoSuchAlgorithmException e) {
			log.error("An error occurs with root cause: {}", e.getMessage(), e);
			clientIsAvailable = false;
		} catch (CertificateException e) {
			log.error("Caught exception related to X509Certificate with root cause: {}", e.getMessage(), e);
			clientIsAvailable = false;
		} catch (IOException e) {
			log.error("An error occurs during I/O operations with root cause: {}", e.getMessage(), e);
			clientIsAvailable = false;
		}
	}

	/**
	 * This method adds headers to HttpRequest method (this handles both GET and
	 * POST methods).
	 * 
	 * @param request is the HTTP request object
	 */
	private void addDistinguishedNameAndSerialNumberToRequestHeaders() {
		log.info("Adding SN \"{}\" to HTTP request header params with key \"{}\"", serialNumber, CertificateConfigManager.HEADER_SN_KEY);
		request.setHeader(CertificateConfigManager.HEADER_SN_KEY, serialNumber);
		log.info("Adding DN \"{}\" to HTTP request header params with key \"{}\"", distinguishedName, CertificateConfigManager.HEADER_DN_KEY);
		request.setHeader(CertificateConfigManager.HEADER_DN_KEY, distinguishedName);
	}

}
