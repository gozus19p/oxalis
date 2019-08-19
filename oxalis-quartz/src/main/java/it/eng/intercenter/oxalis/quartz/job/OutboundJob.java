package it.eng.intercenter.oxalis.quartz.job;

import static it.eng.intercenter.oxalis.rest.util.ConfigManagerUtil.MESSAGE_MDN_SEND_FAILED;
import static it.eng.intercenter.oxalis.rest.util.ConfigManagerUtil.MESSAGE_OUTBOUND_FAILED_FOR_URN;
import static it.eng.intercenter.oxalis.rest.util.ConfigManagerUtil.MESSAGE_OUTBOUND_SUCCESS_FOR_URN;
import static it.eng.intercenter.oxalis.rest.util.ConfigManagerUtil.MESSAGE_STARTING_TO_PROCESS_URN;
import static it.eng.intercenter.oxalis.rest.util.ConfigManagerUtil.MESSAGE_WRONG_CONFIGURATION_SETUP;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.StringUtils;

import com.google.inject.Inject;

import it.eng.intercenter.oxalis.integration.dto.NotierDocumentIndex;
import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.UrnList;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisStatusEnum;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import it.eng.intercenter.oxalis.quartz.job.transmission.NotierTransmissionRequestBuilder;
import it.eng.intercenter.oxalis.rest.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.rest.config.RestConfigManager;
import it.eng.intercenter.oxalis.rest.http.HttpCaller;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;

/**
 * Job che si occupa dell'acquisizione e dell'invio dei documenti da Notier
 * verso rete Peppol.
 *
 * @author Manuel Gozzi
 */
@Slf4j
public class OutboundJob implements Job {

	/**
	 * Variables useful to process REST calls.
	 */
	private static String restUrnGetterUri;
	private static String restDocumentGetterUri;
	private static String restSendStatusUri;

	@Inject
	TransmissionRequestBuilder requestBuilder;

	@Inject
	CertificateConfigManager certConfig;

	@Inject
	RestConfigManager restConfig;

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
			jsonUrnGetterResponse = HttpCaller.executeGet(certConfig, restUrnGetterUri);
		} catch (Exception e) {
			throw new JobExecutionException("Empty response from URI " + restUrnGetterUri);
		} finally {
			if (StringUtils.isEmpty(jsonUrnGetterResponse)) {
				log.error("Received response is empty");
				throw new JobExecutionException("Received response is empty");
			}
		}

		/**
		 * Phase 1b: check the received response and parse it as UrnList object.
		 */
		log.info("Received reponse: {}{}", new Object[] { System.getProperty("line.separator"), jsonUrnGetterResponse });
		UrnList urnListRetrievedFromNotier = GsonUtil.getInstance().fromJson(jsonUrnGetterResponse, UrnList.class);

		if (urnListRetrievedFromNotier != null) {
			log.info("Found {} documents to send on Peppol", urnListRetrievedFromNotier.getUrnCount());
		} else {
			log.error("Invalid response received from Notier: {}{}", new Object[] { System.getProperty("line.separator"), urnListRetrievedFromNotier });
			throw new JobExecutionException("Invalid response received from Notier (UrnList)");
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
				String peppolMessageJson = HttpCaller.executeGet(certConfig, restDocumentGetterUri + index.getUrn());
				log.info("Received String json response containing {} characters", peppolMessageJson.length());
				/**
				 * Phase 2b: build TransmissionMessage object and send that on Peppol network.
				 * The status of the transaction determines how the Oxalis Mdn needs to be
				 * created.
				 */
				oxalisMdn = buildTransmissionAndSendOnPeppol(index.getUrn(), peppolMessageJson);
				log.info(MESSAGE_OUTBOUND_SUCCESS_FOR_URN, index.getUrn());
			} catch (UnsupportedOperationException | IOException e) {
				oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
				log.error(e.getMessage(), e);
			} catch (OxalisTransmissionException e) {
				oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
				log.error(e.getMessage(), e);
			} catch (OxalisContentException e) {
				oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
				log.error(e.getMessage(), e);
			} catch (Exception e) {
				oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
				log.error(e.getMessage(), e);
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
	 * @throws OxalisContentException
	 */
	private OxalisMdn buildTransmissionAndSendOnPeppol(String urn, String peppolMessageJsonFormat) throws OxalisTransmissionException, OxalisContentException {
		TransmissionRequest messageToSend = NotierTransmissionRequestBuilder.build(requestBuilder, peppolMessageJsonFormat);
		TransmissionResponse response = send(messageToSend);
		String receiptPayloadStringified = new String(response.primaryReceipt().getValue(), StandardCharsets.UTF_8);
		log.info("Received the following receipt: {}{}", new Object[] { System.getProperty("line.separator"), receiptPayloadStringified });

		/**
		 * Fase 3. Creo una notifica MDN Oxalis sulla base dell'esito dell'invio.
		 */
		return buildMdn(urn, OxalisStatusEnum.OK, null);
	}

	/**
	 * Provides an OxalisMdn.
	 *
	 * @param urn          is the document URN
	 * @param status       is the status of the sending
	 * @param errorMessage is the error message thrown by exceptions
	 * @return the mdn
	 */
	private OxalisMdn buildMdn(String urn, OxalisStatusEnum status, String errorMessage) {
		switch (status) {
		case OK:
			log.info(MESSAGE_OUTBOUND_SUCCESS_FOR_URN, urn);
			return new OxalisMdn(urn, status, "Document has been sent successfully");
		case KO:
			log.info(MESSAGE_OUTBOUND_FAILED_FOR_URN, urn);
			return new OxalisMdn(urn, status, errorMessage);
		default:
			return new OxalisMdn(urn, OxalisStatusEnum.KO, "Internal server error");
		}
	}

	/**
	 * Forward the OxalisMdn to Notier.
	 *
	 * @param oxalisMdn is the status of the transaction
	 * @param urn       is the URN of involved document
	 */
	private void sendStatusToNotier(OxalisMdn oxalisMdn, String urn) {
		try {
			String resp = HttpCaller.executePost(certConfig, restSendStatusUri, "oxalisContent", GsonUtil.getPrettyPrintedInstance().toJson(oxalisMdn));
			log.info("Received response contains {} characters", resp.length());
		} catch (UnsupportedOperationException | IOException e) {
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
		restUrnGetterUri = restConfig.readValue(RestConfigManager.CONFIG_KEY_REST_GETTER_URNS);
		restDocumentGetterUri = restConfig.readValue(RestConfigManager.CONFIG_KEY_REST_GETTER_DOCUMENT);
		restSendStatusUri = restConfig.readValue(RestConfigManager.CONFIG_KEY_REST_SENDER_STATUS);

		boolean restUrnConfigIsReady = !StringUtils.isEmpty(restUrnGetterUri);
		boolean restDocumentGetterConfigIsReady = !StringUtils.isEmpty(restDocumentGetterUri);
		boolean restSendStatusConfigIsReady = !StringUtils.isEmpty(restSendStatusUri);
		boolean isAllReadyAndSet = restUrnConfigIsReady && restDocumentGetterConfigIsReady && restSendStatusConfigIsReady;

		if (!isAllReadyAndSet) {
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
		if (StringUtils.isEmpty(restDocumentGetterUri) || StringUtils.isEmpty(restDocumentGetterUri) || StringUtils.isEmpty(restSendStatusUri)) {
			loadRestUriReferences();
		}
	}

}
