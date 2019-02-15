package no.difi.oxalis.outbound.job;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import it.eng.intercenter.oxalis.quartz.config.ConfigRestCall;
import it.eng.intercenter.oxalis.quartz.job.exception.NotierDocumentCastException;
import it.eng.intercenter.oxalis.quartz.job.exception.NotierRestCallException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.commons.quartz.transmission.notier.NotierTransmissionMessageBuilder;
import no.difi.oxalis.outbound.OxalisOutboundComponent;

/**
 * Job che si occupa dell'acquisizione e dell'invio dei documenti da Notier
 * verso rete Peppol.
 * 
 * @author Manuel Gozzi
 */
public class JobNotierGetter implements Job {

	// TODO: Sicurezza e certificati.
	// TODO: Inviare la transmission response a Notier e configurare sulla
	// controparte la creazione dell'MDN.

	private static final Logger log = LoggerFactory.getLogger(JobNotierGetter.class);
	private static final String MESSAGE_OUTBOUND_FAILED_FOR_URN = "Outbound process failed for URN: {}";
	private static final String MESSAGE_OUTBOUND_SUCCESS_FOR_URN = "Outbound process completed succesfully for URN: {}";
	private static final String MESSAGE_READING_PROPERTY = "Reading configuration value defined for key: {}";
	private static final String MESSAGE_REST_CALL_FAILED = "Something went wrong during REST call execution, message: {}";
	private static final String MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE = "REST call executed succesfully, got response status: {}";
	private static final String MESSAGE_STARTING_TO_PROCESS_URN = "Starting to process URN: {}";
	private static final String MESSAGE_USING_REST_URI = "Executing REST call to URI: {}";
	private static final String MESSAGE_WRONG_HTTP_PROTOCOL = "Something went wrong with HttpClient protocol, message: {}";
	private static final String MESSAGE_WRONG_INPUT_OUTPUT = "Something went wrong with input/output, message: {}";
	private static final String MESSAGE_WRONG_URI_SYNTAX = "Something went wrong with URI syntax, message: {}";

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
		 * Recupero la lista di URN corrispondenti ai documenti che devono essere
		 * inviati su rete Peppol.
		 */
		log.info(MESSAGE_READING_PROPERTY, ConfigRestCall.CONFIG_KEY_REST_GETTER_URNS);
		String restUri = configuration.readSingleProperty(ConfigRestCall.CONFIG_KEY_REST_GETTER_URNS);
		String jsonUrnGetterResponse = null;
		try {
			jsonUrnGetterResponse = executeRestCallFromURI(restUri);
		} catch (NotierRestCallException e) {
			log.error(MESSAGE_REST_CALL_FAILED, e.getMessage());
		}

		/**
		 * Se ho ottenuto una response valida converto in arraylist di String il
		 * contenuto della response. Per ogni stringa (URN) eseguo il puntuale recupero
		 * documento.
		 */
		if (!StringUtils.isEmpty(jsonUrnGetterResponse)) {
			List<String> urnList = new Gson().fromJson(jsonUrnGetterResponse, new TypeToken<ArrayList<String>>() {
			}.getType());

			/**
			 * La variabile "restUri" contiene la base dell'URI della chiamata rest, a tale
			 * base deve essere aggiunto il path parameter corrispondente all'URN che si sta
			 * iterando.
			 */
			log.info(MESSAGE_READING_PROPERTY, ConfigRestCall.CONFIG_KEY_REST_GETTER_DOCUMENT);
			restUri = configuration.readSingleProperty(ConfigRestCall.CONFIG_KEY_REST_GETTER_DOCUMENT);

			for (String urn : urnList) {
				log.info(MESSAGE_STARTING_TO_PROCESS_URN, urn);
				try {
					String jsonDocumentGetterResponse = executeRestCallFromURI(restUri + urn);
					try {
						TransmissionMessage messageToSend = NotierTransmissionMessageBuilder
								.buildTransmissionMessageFromDocumento(jsonDocumentGetterResponse);
						send(messageToSend);
						log.info(MESSAGE_OUTBOUND_SUCCESS_FOR_URN, urn);
					} catch (OxalisTransmissionException e) {
						log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, urn);
						log.error(e.getMessage());
					} catch (NotierDocumentCastException e) {
						log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, urn);
						log.error(e.getMessage());
					}

				} catch (NotierRestCallException e) {
					log.error(MESSAGE_REST_CALL_FAILED, e.getMessage());
					log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, urn);
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
		TransmissionResponse response = outboundComponent.getTransmitter().transmit(documento);
		return response;
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
	private String executeRestCallFromURI(String restUri) throws NotierRestCallException {
		try {
			HttpGet getRequest = new HttpGet();
			getRequest.setURI(new URI(restUri));
			HttpClient client = HttpClients.createDefault();
			log.info(MESSAGE_USING_REST_URI, restUri);
			HttpResponse response = client.execute(getRequest);
			log.info(MESSAGE_REST_CALL_SUCCEDED_WITH_RESPONSE, response.getStatusLine().getStatusCode());
			return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString());
		} catch (ClientProtocolException e) {
			log.error(MESSAGE_WRONG_HTTP_PROTOCOL, e.getMessage());
			throw new NotierRestCallException(e.getMessage());
		} catch (URISyntaxException e) {
			log.error(MESSAGE_WRONG_URI_SYNTAX, e.getMessage());
			throw new NotierRestCallException(e.getMessage());
		} catch (IOException e) {
			log.error(MESSAGE_WRONG_INPUT_OUTPUT, e.getMessage());
			throw new NotierRestCallException(e.getMessage());
		}
	}

}
