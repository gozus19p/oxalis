package it.eng.intercenter.oxalis.rest.client.api;

import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
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

import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_REST_EXECUTED_WITH_STATUS;
import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_USING_REST_URI;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public abstract class AbstractHttpNotierCall<T extends HttpRequestBase> {

	private final CertificateConfigManager certificateConfiguration;
	private CloseableHttpClient httpClient;
	protected T httpRequest;

	/**
	 * As of 04.30.2019 only POST and GET are supported.
	 */
	protected NotierRestCallTypeEnum httpRequestType;

	/**
	 * "true" if the HttpClient has been set up correctly, "false" otherwise.
	 */
	private boolean httpClientIsAvailable;

	/**
	 * Values of p12 certificate.
	 */
	private final boolean isProductionMode;
	private String distinguishedName;
	private String organizationCertificateP12Password;
	private String serialNumber;
	private KeyStore organizationCertificateP12;

	private static final String ORGANIZATION_CERTIFICATE_ALGORITHM = "PKCS12";

	/**
	 * Constructor.
	 *
	 * @param certConfig is the configuration that holds certificate details
	 */
	public AbstractHttpNotierCall(CertificateConfigManager certConfig) {
		this.certificateConfiguration = certConfig;
		isProductionMode = detectProductionMode();
		loadCertificate();
		try {
			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(
							RequestConfig.custom()
									.setConnectTimeout(20000)
									.setSocketTimeout(20000)
									.setConnectionRequestTimeout(20000)
									.build()
					)
					.setRetryHandler(
							new DefaultHttpRequestRetryHandler(
									3,
									true
							)
					)
					.setSSLContext(
							getSSLContext()
					)
					.build();
		} catch (KeyManagementException | KeyStoreException e) {
			log.error("Some errors occur on Keystore management, root cause: {}", e.getMessage(), e);
		} catch (Exception e) {
			log.error("Some errors occur during HttpNotierCall creation, root cause: {}", e.getMessage(), e);
		}
	}

	/**
	 * @return tue if Oxalis has been set up to run in production, false otherwise
	 */
	private boolean detectProductionMode() {
		return Boolean.parseBoolean(certificateConfiguration.readValue(CertificateConfigManager.CONFIG_KEY_PRODUCTION_MODE_ENABLED));
	}

	/**
	 * Executes the HTTP call.
	 *
	 * @return the content of the response in String format
	 * @throws UnsupportedOperationException see {@link UnsupportedOperationException}
	 * @throws ClientProtocolException       see {@link ClientProtocolException}
	 * @throws IOException                   if {@link CloseableHttpClient} is not available
	 */
	public String execute() throws UnsupportedOperationException, ClientProtocolException, IOException {

		log.info("Executing: {}", httpRequest);
		if (!httpClientIsAvailable) {
			throw new IOException("HttpClient is not available");
		}
		if (!isProductionMode) {
			addDistinguishedNameAndSerialNumberToRequestHeaders();
			// log.warn(MESSAGE_PRODUCTION_MODE_DISABLED);
		}
		log.info(MESSAGE_USING_REST_URI, httpRequestType.name(), httpRequest.getURI().normalize());
		HttpResponse response = httpClient.execute(httpRequest);
		log.info(MESSAGE_REST_EXECUTED_WITH_STATUS, response.getStatusLine().getStatusCode());

		try {

			if (statusCodeIsValid(response)) {

				return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.name());
			} else {

				throw new IOException("Status code is \"" + response.getStatusLine().getStatusCode() + "\"");
			}
		} finally {

			closeHttpClient();
		}
	}

	private boolean statusCodeIsValid(HttpResponse httpResponse) {

		return httpResponse != null
				&& httpResponse.getStatusLine() != null
				&& httpResponse.getStatusLine().getStatusCode() >= 200
				&& httpResponse.getStatusLine().getStatusCode() <= 299;
	}

	/**
	 * @return SSLContext used to execute HTTP calls.
	 */
	private SSLContext getSSLContext() throws Exception {
		try {
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(organizationCertificateP12, organizationCertificateP12Password.toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			tmf.init((KeyStore) null);

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			httpClientIsAvailable = true;
			return context;
		} catch (KeyStoreException e) {
			log.error("Found a problem on the KeyStore: {}", e.getMessage(), e);
			httpClientIsAvailable = false;
			throw e;
		} catch (KeyManagementException e) {
			log.error("Found a problem on the key management: {}", e.getMessage(), e);
			httpClientIsAvailable = false;
			throw e;
		} catch (Exception e) {
			log.error("Found a problem: {}", e.getMessage(), e);
			httpClientIsAvailable = false;
			throw e;
		}
	}

	/**
	 * Method that loads details of the PKCS12 certificate.
	 */
	private void loadCertificate() {
		log.debug("Preparing certificate for Notier HTTP communications");

		String organizationCertificateP12FileName = certificateConfiguration.readValue(CertificateConfigManager.CONFIG_KEY_ORG_CERT_FILE_NAME);
		organizationCertificateP12Password = certificateConfiguration.readValue(CertificateConfigManager.CONFIG_KEY_ORG_CERT_PASSWORD, true);

		String certificatePath = buildCertificatePath(organizationCertificateP12FileName);

		try (FileInputStream certificateInputStream = new FileInputStream(certificatePath)) {

			log.debug("Parsing certificate details from file {}", organizationCertificateP12FileName);
			organizationCertificateP12 = KeyStore.getInstance(ORGANIZATION_CERTIFICATE_ALGORITHM);

			log.debug("Accessing certificate using password");
			organizationCertificateP12.load(certificateInputStream, organizationCertificateP12Password.toCharArray());

			Enumeration<String> aliasesEnumeration = organizationCertificateP12.aliases();
			String alias = aliasesEnumeration.nextElement();
			log.debug("Using alias: {}", alias);

			X509Certificate x509Certificate = (X509Certificate) organizationCertificateP12.getCertificate(alias);

			distinguishedName = x509Certificate.getSubjectDN().getName();
			serialNumber = x509Certificate.getSerialNumber().toString();

			log.debug("DN: {}; SN: {};", distinguishedName, serialNumber);

		} catch (KeyStoreException e) {

			log.error("An error occurs while accessing KeyStore with root cause: {}", e.getMessage(), e);
			httpClientIsAvailable = false;
		} catch (NoSuchAlgorithmException e) {

			log.error("An error occurs with root cause: {}", e.getMessage(), e);
			httpClientIsAvailable = false;
		} catch (CertificateException e) {

			log.error("Caught exception related to X509Certificate with root cause: {}", e.getMessage(), e);
			httpClientIsAvailable = false;
		} catch (IOException e) {

			log.error("An error occurs during I/O operations with root cause: {}", e.getMessage(), e);
			httpClientIsAvailable = false;
		}
	}

	/**
	 * @param fileName is the certificate file name (without path)
	 * @return full certificate path
	 */
	private String buildCertificatePath(String fileName) {
		StringBuilder sb = new StringBuilder();
		String configuredDirectoryPath = certificateConfiguration.getCertificatesDirectoryPath().normalize().toString();
		sb.append(configuredDirectoryPath);
		if (!configuredDirectoryPath.substring(configuredDirectoryPath.length() - 2).equals(System.getProperty("file.separator"))) {
			sb.append(System.getProperty("file.separator"));
		}
		sb.append(fileName);
		return sb.toString();
	}

	/**
	 * This method adds headers to HttpRequest method (this handles both GET and
	 * POST methods).
	 */
	private void addDistinguishedNameAndSerialNumberToRequestHeaders() {
		log.warn("Adding SN \"{}\" to HTTP request header params with key \"{}\"", serialNumber, CertificateConfigManager.HEADER_SN_KEY);
		httpRequest.setHeader(CertificateConfigManager.HEADER_SN_KEY, serialNumber);
		log.warn("Adding DN \"{}\" to HTTP request header params with key \"{}\"", distinguishedName, CertificateConfigManager.HEADER_DN_KEY);
		httpRequest.setHeader(CertificateConfigManager.HEADER_DN_KEY, distinguishedName);
	}

	public void closeHttpClient() {
		if (httpClient != null && httpClientIsAvailable) {
			try {

				httpClient.close();
			} catch (Exception e) {

				log.error("An error occurred: {}", e.getMessage(), e);
			}
		}
	}

	@Override
	public String toString() {
		return "HttpNotierGet{" +
				"httpRequest=" + httpRequest +
				", httpRequestType=" + httpRequestType +
				'}';
	}
}
