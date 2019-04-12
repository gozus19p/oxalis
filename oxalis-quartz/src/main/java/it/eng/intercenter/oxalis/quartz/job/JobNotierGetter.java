package it.eng.intercenter.oxalis.quartz.job;

import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_MDN_SEND_FAILED;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_OUTBOUND_FAILED_FOR_URN;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_OUTBOUND_SUCCESS_FOR_URN;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_READING_PROPERTY;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_REST_CALL_FAILED;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_STARTING_TO_PROCESS_URN;
import static it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCallMessageConstants.MESSAGE_WRONG_CONFIGURATION_SETUP;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import it.eng.intercenter.oxalis.commons.quartz.transmission.NotierTransmissionMessageBuilder;
import it.eng.intercenter.oxalis.integration.dto.NotierDocumentIndex;
import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.UrnList;
import it.eng.intercenter.oxalis.integration.dto.enumerator.NotierRestCallTypeEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisStatusEnum;
import it.eng.intercenter.oxalis.quartz.config.impl.ConfigRestCall;
import it.eng.intercenter.oxalis.quartz.job.exception.NotierRestCallException;
import it.eng.intercenter.oxalis.quartz.ws.RestManagement;
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

	/**
	 * Variables useful to process REST.
	 */
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
		 * Setup configuration.
		 */
		setupRestConfiguration();

		/**
		 * Fase 1. Recupero URN dei documenti da inviare su rete Peppol.
		 */
		String jsonUrnGetterResponse = null;
		try {
			jsonUrnGetterResponse = RestManagement.executeRestCallFromURI(restUrnGetterUri, NotierRestCallTypeEnum.GET,
					null);
		} catch (NotierRestCallException e) {
			log.error(MESSAGE_REST_CALL_FAILED, e.getMessage());
			return;
		} finally {
			if (StringUtils.isEmpty(jsonUrnGetterResponse)) {
				return;
			}
		}

		/**
		 * Se ho ottenuto una response valida converto in arraylist di String il
		 * contenuto della response. Per ogni stringa (URN) eseguo il puntuale recupero
		 * documento.
		 */
		System.out.println(jsonUrnGetterResponse);
		UrnList urnListRetrievedFromNotier = new Gson().fromJson(jsonUrnGetterResponse, UrnList.class);
		
		if (urnListRetrievedFromNotier != null) {
			log.info("Found {} documents to send on Peppol", urnListRetrievedFromNotier.getUrnCount());
		} else {
			log.warn("Notier answered that has no documents to send on Peppol right now");
			return;
		}

		/**
		 * Fase 2a. Recupero il singolo documento da inviare.
		 */
		OxalisMdn oxalisMdn = null;
		
		for (NotierDocumentIndex index : urnListRetrievedFromNotier.getDocuments()) {
			log.info(MESSAGE_STARTING_TO_PROCESS_URN, index.getUrn());

			try {
				String peppolMessageJson = RestManagement.executeRestCallFromURI(restDocumentGetterUri + index.getUrn(),
						NotierRestCallTypeEnum.GET, null);
				System.out.println(peppolMessageJson);
				try {
					/**
					 * Fase 2b. Converto la response, ottenuta in formato Json, in
					 * TransmissionMessage e processo l'invio del medesimo su rete Peppol.
					 */
					oxalisMdn = buildTransmissionAndSendOnPeppol(oxalisMdn, index.getUrn(), peppolMessageJson);

				} catch (OxalisTransmissionException e) {
					oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());

					log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
					log.error(e.getMessage());
				}
			} catch (NotierRestCallException e) {
				oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());

				log.error(MESSAGE_REST_CALL_FAILED, e.getMessage());
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
			}

			/**
			 * Fase 4. Invio la notifica MDN a Notier.
			 */
			sendStatusToNotier(oxalisMdn, index.getUrn());
		}

	}

	private OxalisMdn buildTransmissionAndSendOnPeppol(OxalisMdn oxalisMdn, String urn, String peppolMessageJsonFormat)
			throws OxalisTransmissionException {
		TransmissionMessage messageToSend = NotierTransmissionMessageBuilder
				.buildTransmissionMessageFromPeppolMessage(peppolMessageJsonFormat);
		// TODO: Determinare esito positivo/negativo.
		TransmissionResponse response = send(messageToSend);
		log.info("Response received: {}", response.toString());

		/**
		 * Fase 3. Creo una notifica MDN Oxalis sulla base dell'esito dell'invio.
		 */
		if (true) {
			oxalisMdn = new OxalisMdn(urn, OxalisStatusEnum.OK, null);
			log.info(MESSAGE_OUTBOUND_SUCCESS_FOR_URN, urn);
		}
		return oxalisMdn;
	}

	private void sendStatusToNotier(OxalisMdn oxalisMdn, String urn) {
		try {
			String resp = RestManagement.executeRestCallFromURI(restSendStatusUri, NotierRestCallTypeEnum.POST,
					oxalisMdn);
			log.info("Response received: {}", resp);
		} catch (NotierRestCallException ex) {
			log.error(MESSAGE_REST_CALL_FAILED, ex.getMessage());
			log.error(MESSAGE_MDN_SEND_FAILED, urn);
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
	 * @throws JobExecutionException if the configuration has not been setup
	 *                               properly.
	 */
	private void loadRestUriReferences() throws JobExecutionException {
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

		boolean restUrnConfigIsReady = !StringUtils.isEmpty(restUrnGetterUri);
		boolean restDocumentGetterConfigIsReady = !StringUtils.isEmpty(restDocumentGetterUri);
		boolean restSendStatusConfigIsReady = !StringUtils.isEmpty(restSendStatusUri);
		boolean isAllReady = restUrnConfigIsReady && restDocumentGetterConfigIsReady && restSendStatusConfigIsReady;

		if (!isAllReady) {
			String configStatus = "";
			configStatus += "[URN getter=" + (restUrnConfigIsReady ? "OK]" : "ERROR]");
			configStatus += "[document getter=" + (restDocumentGetterConfigIsReady ? "OK]" : "ERROR]");
			configStatus += "[send status=" + (restSendStatusConfigIsReady ? "OK]" : "ERROR]");
			log.error(MESSAGE_WRONG_CONFIGURATION_SETUP, configStatus);
			throw new JobExecutionException("REST configuration has not been properly setup. Status: " + configStatus);
		}
	}

	/**
	 * @throws JobExecutionException if the configuration has not been setup
	 *                               properly.
	 */
	private void setupRestConfiguration() throws JobExecutionException {
		if (StringUtils.isEmpty(restDocumentGetterUri) || StringUtils.isEmpty(restDocumentGetterUri)
				|| StringUtils.isEmpty(restSendStatusUri)) {
			loadRestUriReferences();
		}
	}

}
