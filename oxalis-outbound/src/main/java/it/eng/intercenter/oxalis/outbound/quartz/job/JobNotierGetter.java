package it.eng.intercenter.oxalis.outbound.quartz.job;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

import it.eng.intercenter.oxalis.commons.quartz.transmission.NotierTransmissionMessageBuilder;
import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisStatusEnum;
import it.eng.intercenter.oxalis.quartz.config.ConfigRestCall;
import it.eng.intercenter.oxalis.quartz.job.exception.NotierRestCallException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.outbound.OxalisOutboundComponent;

/**
 * Job che si occupa dell'acquisizione e dell'invio dei documenti da Notier
 * verso rete Peppol.
 * 
 * @author Manuel Gozzi
 */
public class JobNotierGetter implements Job {

	// TODO: Sicurezza e certificati.

	private static final Logger log = LoggerFactory.getLogger(JobNotierGetter.class);
	private static final String MESSAGE_MDN_SEND_FAILED = "MDN has not been sent to Notier for URN: {}";
	private static final String MESSAGE_OUTBOUND_FAILED_FOR_URN = "Outbound process failed for URN: {}";
	private static final String MESSAGE_OUTBOUND_SUCCESS_FOR_URN = "Outbound process completed succesfully for URN: {}";
	private static final String MESSAGE_READING_PROPERTY = "Reading configuration value defined for key: {}";
	private static final String MESSAGE_REST_CALL_FAILED = "Something went wrong during REST call execution, message: {}";
	private static final String MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE = "REST call executed succesfully, got response status: {}";
	private static final String MESSAGE_STARTING_TO_PROCESS_URN = "Starting to process URN: {}";
	private static final String MESSAGE_USING_REST_URI = "Executing REST call to URI: {}";
	private static final String MESSAGE_WRONG_HTTP_PROTOCOL = "Something went wrong with HttpClient protocol, message: {}";
	private static final String MESSAGE_WRONG_INPUT_OUTPUT = "Something went wrong with input/output, message: {}";

	private static String restUrnGetterUri = null;
	private static String restDocumentGetterUri = null;
	private static String restSendStatusUri = null;

	@Inject
	ConfigRestCall configuration;

	@Inject
	OxalisOutboundComponent outboundComponent;

	/**
	 * Esegue una chiamata a Notier per recuperare i documenti dal WS relativo.
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		/**
		 * Fase 0. Controlla le configurazioni REST, se occorre le valorizza.
		 */
		if (StringUtils.isEmpty(restDocumentGetterUri) || StringUtils.isEmpty(restDocumentGetterUri)
				|| StringUtils.isEmpty(restSendStatusUri)) {
			loadRestUriReferences();
		}

		/**
		 * Fase 1. Recupero URN dei documenti da inviare su rete Peppol.
		 */
		String jsonUrnGetterResponse = null;
		try {
			jsonUrnGetterResponse = executeRestCallFromURI(restUrnGetterUri, NotierRestCallTypeEnum.GET, null);
		} catch (NotierRestCallException e) {
			log.error(MESSAGE_REST_CALL_FAILED, e.getMessage());
		}

		/**
		 * Se ho ottenuto una response valida converto in arraylist di String il
		 * contenuto della response. Per ogni stringa (URN) eseguo il puntuale recupero
		 * documento.
		 */
		if (!StringUtils.isEmpty(jsonUrnGetterResponse)) {
			String[] urnList = new Gson().fromJson(jsonUrnGetterResponse, String[].class);

			/**
			 * Fase 2a. Recupero il singolo documento da inviare.
			 */

			OxalisMdn oxalisMdn;
			for (String urn : urnList) {
				log.info(MESSAGE_STARTING_TO_PROCESS_URN, urn);

				try {
					String peppolMessageJsonFormat = executeRestCallFromURI(restDocumentGetterUri + urn,
							NotierRestCallTypeEnum.GET, null);
					try {
						/**
						 * Fase 2b. Converto la response, ottenuta in formato Json, in
						 * TransmissionMessage e processo l'invio del medesimo su rete Peppol.
						 */
						TransmissionMessage messageToSend = NotierTransmissionMessageBuilder
								.buildTransmissionMessageFromPeppolMessage(peppolMessageJsonFormat);
						//TODO: Determinare esito positivo/negativo.
						TransmissionResponse response = send(messageToSend);
						log.info("Response received: {}", response.toString());

						/**
						 * Fase 3. Creo una notifica MDN Oxalis sulla base dell'esito dell'invio.
						 */
						if (true) {
							oxalisMdn = new OxalisMdn(urn, OxalisStatusEnum.OK, null);
							// TODO: Trasmettere a Notier
						}

						log.info(MESSAGE_OUTBOUND_SUCCESS_FOR_URN, urn);
					} catch (OxalisTransmissionException e) {
						oxalisMdn = new OxalisMdn(urn, OxalisStatusEnum.KO, e.getMessage());

						log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, urn);
						log.error(e.getMessage());
					}
				} catch (NotierRestCallException e) {
					oxalisMdn = new OxalisMdn(urn, OxalisStatusEnum.KO, e.getMessage());

					log.error(MESSAGE_REST_CALL_FAILED, e.getMessage());
					log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, urn);
				}

				/**
				 * Fase 4. Invio la notifica MDN a Notier.
				 */
				try {
					String resp = executeRestCallFromURI(restSendStatusUri, NotierRestCallTypeEnum.POST, oxalisMdn);
					log.info("Response received: {}", resp);
				} catch (NotierRestCallException ex) {
					log.error(MESSAGE_REST_CALL_FAILED, ex.getMessage());
					log.error(MESSAGE_MDN_SEND_FAILED, urn);
				}

			}
		}

	}

	/**
	 * Processa l'invio su rete Peppol del documento recuperato.
	 * 
	 * @param documento
	 * @param urn
	 */
	private TransmissionResponse send(TransmissionMessage documento) throws OxalisTransmissionException {
		return outboundComponent.getTransmitter().transmit(documento);
	}

	/**
	 * Esegue una chiamata REST prendendo in input un URI e restituisce la risposta
	 * in formato stringa.
	 * 
	 * @param restUri
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private String executeRestCallFromURI(String restUri, NotierRestCallTypeEnum restCallType, OxalisMdn oxalisMdn)
			throws NotierRestCallException {
		try {
			HttpClient client = HttpClients.createDefault();
			log.info(MESSAGE_USING_REST_URI, restUri);

			if (restCallType.equals(NotierRestCallTypeEnum.GET)) {
				HttpGet request = new HttpGet(restUri);
				HttpResponse response = client.execute(request);
				log.info(MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE, response.getStatusLine().getStatusCode());

				return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString());

			} else if (restCallType.equals(NotierRestCallTypeEnum.POST)) {
				HttpPost request = new HttpPost(restUri);

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
	 * Carica le configurazioni REST interessate dal job.
	 */
	private void loadRestUriReferences() {
		/**
		 * Recupero la lista di URN corrispondenti ai documenti che devono essere
		 * inviati su rete Peppol.
		 */
		log.info(MESSAGE_READING_PROPERTY, ConfigRestCall.CONFIG_KEY_REST_GETTER_URNS);
		restUrnGetterUri = configuration.readSingleProperty(ConfigRestCall.CONFIG_KEY_REST_GETTER_URNS);
		log.info(MESSAGE_READING_PROPERTY, ConfigRestCall.CONFIG_KEY_REST_GETTER_DOCUMENT);
		restDocumentGetterUri = configuration.readSingleProperty(ConfigRestCall.CONFIG_KEY_REST_GETTER_DOCUMENT);
		log.info(MESSAGE_READING_PROPERTY, ConfigRestCall.CONFIG_KEY_REST_SENDER_STATUS);
		restSendStatusUri = configuration.readSingleProperty(ConfigRestCall.CONFIG_KEY_REST_SENDER_STATUS);
	}

}
