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
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class JobNotierOutbound implements Job {

	// TODO: Sicurezza e certificati.

	/**
	 * Variables useful to process REST calls.
	 */
	private static String restUrnGetterUri;
	private static String restDocumentGetterUri;
	private static String restSendStatusUri;

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
		 * Phase 0: setup REST configuration if needed.
		 */
		setupOutboundRestConfiguration();

		/**
		 * Phase 1: get URN of documents that need to be sent on Peppol directly from
		 * Notier via REST web service.
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
		 * Phase 1b: check the received response and parse it as UrnList object.
		 */
		System.out.println(jsonUrnGetterResponse);
		UrnList urnListRetrievedFromNotier = new Gson().fromJson(jsonUrnGetterResponse, UrnList.class);

		if (urnListRetrievedFromNotier != null) {
			log.info("Found {} documents to send on Peppol", urnListRetrievedFromNotier.getUrnCount());
		} else {
			log.error("Invalid response received from Notier: {}{}",
					new Object[] { System.getProperty("line.separator"), urnListRetrievedFromNotier });
			return;
		}

		/**
		 * Phase 2: iterate over UrnList.NotierDocumentIndex' collection in order to
		 * send each document one by one.
		 */
		OxalisMdn oxalisMdn = null;

		for (NotierDocumentIndex index : urnListRetrievedFromNotier.getDocuments()) {
			log.info(MESSAGE_STARTING_TO_PROCESS_URN, index.getUrn());

			try {
				/**
				 * Phase 2a: get document payload by REST web service from Notier.
				 */
				String peppolMessageJson = RestManagement.executeRestCallFromURI(restDocumentGetterUri + index.getUrn(),
						NotierRestCallTypeEnum.GET, null);
				System.out.println(peppolMessageJson);
				try {
					/**
					 * Phase 2b: build TransmissionMessage object and send that on Peppol network.
					 * The status of the transaction determines how the Oxalis Mdn needs to be
					 * created.
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
			 * Phase 3: forward the OxalisMdn object to Notier in order to communicate the
			 * status of transaction.
			 */
			sendStatusToNotier(oxalisMdn, index.getUrn());
		}

	}

	/**
	 * Builds a TransmissionMessage object and send it on Peppol.
	 * 
	 * @param oxalisMdn               is the object that holds the URN and the
	 *                                status
	 * @param urn                     is the URN of the involved document
	 * @param peppolMessageJsonFormat is the json format of PeppolMessage instance
	 *                                related to involved document
	 * @return the final OxalisMdn object
	 * @throws OxalisTransmissionException if some problems occur while sending
	 *                                     document on Peppol network
	 */
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

	/**
	 * Forward the OxalisMdn to Notier.
	 * 
	 * @param oxalisMdn is the status of the transaction
	 * @param urn       is the URN of involved document
	 */
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
	private void setupOutboundRestConfiguration() throws JobExecutionException {
		if (StringUtils.isEmpty(restDocumentGetterUri) || StringUtils.isEmpty(restDocumentGetterUri)
				|| StringUtils.isEmpty(restSendStatusUri)) {
			loadRestUriReferences();
		}
	}

}
