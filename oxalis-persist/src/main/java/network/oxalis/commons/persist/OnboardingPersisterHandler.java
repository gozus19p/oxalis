package network.oxalis.commons.persist;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.inbound.InboundMetadata;
import network.oxalis.api.persist.ExceptionPersister;
import network.oxalis.api.persist.PayloadPersister;
import network.oxalis.api.persist.ReceiptPersister;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.inject.Singleton;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Manuel Gozzi
 */
@Singleton
@Slf4j
public class OnboardingPersisterHandler extends DefaultPersisterHandler {

	/**
	 * Configuration keys
	 */
	private static final String CONFIG_KEY_URI = "uri";
	private static final String CONFIG_KEY_CERTIFICATE_PATH = "certificate-path";
	private static final String CONFIG_KEY_CERTIFICATE_PASSWORD = "certificate-password";
	private static final String CONFIG_KEY_CERTIFICATE_ALGORITHM = "certificate-algorithm";
	private static final String CONFIG_KEY_TIMEOUT_SOCKET = "client-timeout-socket";
	private static final String CONFIG_KEY_TIMEOUT_CONNECTION = "client-timeout-connection";
	private static final String CONFIG_KEY_TIMEOUT_REQUEST = "client-timeout-request";

	private static final String PERSISTER_PROPERTIES = "persister.properties";

	private static final String NAME_VALUE_PAIR_KEY = "message";

	/**
	 * Instance variables.
	 */
	private HttpClient httpClient;
	private final Map<String, String> config = new HashMap<>();

	@Inject
	public OnboardingPersisterHandler(PayloadPersister payloadPersister, ReceiptPersister receiptPersister, ExceptionPersister exceptionPersister)
			throws Exception {
		super(payloadPersister, receiptPersister, exceptionPersister);
		initClient();

		Properties persisterProperties = new Properties();
		persisterProperties.load(
				this.getClass().getClassLoader().getResourceAsStream(PERSISTER_PROPERTIES)
		);
		String env = persisterProperties.getProperty("oxalis.onboarding.env", "test");
		config.put(
				CONFIG_KEY_CERTIFICATE_PATH, persisterProperties.getProperty(
						String.format("oxalis.onboarding.%s.certificate.path", env)
				)
		);
		config.put(
				CONFIG_KEY_CERTIFICATE_PASSWORD, persisterProperties.getProperty(
						String.format("oxalis.onboarding.%s.certificate.password", env)
				)
		);
		config.put(
				CONFIG_KEY_CERTIFICATE_ALGORITHM, persisterProperties.getProperty(
						String.format("oxalis.onboarding.%s.certificate.algorithm", env)
				)
		);
		config.put(
				CONFIG_KEY_URI, persisterProperties.getProperty(
						String.format("oxalis.onboarding.%s.uri", env)
				)
		);
		config.put(
				CONFIG_KEY_TIMEOUT_SOCKET, (String) persisterProperties.getOrDefault("timeout.socket", 10000 + "")
		);
		config.put(
				CONFIG_KEY_TIMEOUT_CONNECTION, (String) persisterProperties.getOrDefault("timeout.connection", 10000 + "")
		);
		config.put(
				CONFIG_KEY_TIMEOUT_REQUEST, (String) persisterProperties.getOrDefault("timeout.request", 10000 + "")
		);
	}

	@Override
	public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {

		try {

			// Try to persist using logic provided by this service
			this.persist(payloadPath);
		} catch (Exception e) {

			// Persist file on filesystem
			super.persist(inboundMetadata, payloadPath);
		}
	}

	/**
	 * It forwards the received document to Onboarding server.
	 *
	 * @param payloadPath it corresponds to payload temporary path
	 * @throws IOException if anything goes wrong
	 */
	private void persist(Path payloadPath) throws IOException {

		try (FileReader fileReader = new FileReader(payloadPath.toFile())) {

			byte[] bytes = IOUtils.toByteArray(fileReader, StandardCharsets.UTF_8.name());

			HttpPost httpPost = new HttpPost(config.get(CONFIG_KEY_URI));

			NameValuePair nameValuePair = new BasicNameValuePair(NAME_VALUE_PAIR_KEY, new String(bytes));
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(Collections.singletonList(nameValuePair));
			httpPost.setEntity(urlEncodedFormEntity);
			HttpResponse response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			System.out.println(statusCode);
		}
	}

	/**
	 * It initializes HTTP Client used to send received documents to Onboarding.
	 */
	private void initClient() throws Exception {

		httpClient = HttpClients.custom()
				.setSSLContext(getSSLContext())
				.setDefaultRequestConfig(getRequestConfig())
				.build();
	}

	/**
	 * It provides request configuration used by HTTP Client.
	 *
	 * @return {@link RequestConfig} configurazione del client HTTP
	 */
	private RequestConfig getRequestConfig() {

		return RequestConfig.custom()
				.setConnectTimeout(Integer.parseInt(config.get(CONFIG_KEY_TIMEOUT_CONNECTION)))
				.setConnectionRequestTimeout(Integer.parseInt(config.get(CONFIG_KEY_TIMEOUT_REQUEST)))
				.setSocketTimeout(Integer.parseInt(config.get(CONFIG_KEY_TIMEOUT_SOCKET)))
				.build();
	}

	/**
	 * It provides SSL context used for transmission.
	 *
	 * @return {@link SSLContext} instance
	 */
	private SSLContext getSSLContext() throws Exception {

		try (FileInputStream inputStream = new FileInputStream(config.get(CONFIG_KEY_CERTIFICATE_PATH))) {

			KeyStore keyStore = KeyStore.getInstance(config.get(CONFIG_KEY_CERTIFICATE_ALGORITHM));
			keyStore.load(inputStream, config.get(CONFIG_KEY_CERTIFICATE_PASSWORD).toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, config.get(CONFIG_KEY_CERTIFICATE_PASSWORD).toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			tmf.init((KeyStore) null);

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			return context;
		} catch (KeyStoreException e) {
			log.error("Found a problem on the KeyStore: {}", e.getMessage(), e);
			throw e;
		} catch (KeyManagementException e) {
			log.error("Found a problem on the key management: {}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			log.error("Found a problem: {}", e.getMessage(), e);
			throw e;
		}
	}
}
