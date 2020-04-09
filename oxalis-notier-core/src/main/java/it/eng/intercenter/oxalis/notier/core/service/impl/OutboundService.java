package it.eng.intercenter.oxalis.notier.core.service.impl;

import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_MDN_SEND_FAILED;
import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_OUTBOUND_FAILED_FOR_URN;
import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_OUTBOUND_SUCCESS_FOR_URN;
import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_STARTING_TO_PROCESS_URN;
import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.MESSAGE_WRONG_CONFIGURATION_SETUP;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;

import org.apache.http.HttpResponse;

import com.google.inject.Inject;

import it.eng.intercenter.oxalis.integration.dto.FullPeppolMessage;
import it.eng.intercenter.oxalis.integration.dto.NotierDocumentIndex;
import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.TransactionDetails;
import it.eng.intercenter.oxalis.integration.dto.UrnList;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisStatusEnum;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import it.eng.intercenter.oxalis.notier.core.service.api.IOutboundService;
import it.eng.intercenter.oxalis.notier.core.service.util.NotierTransmissionRequestBuilder;
import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.RestConfigManager;
import it.eng.intercenter.oxalis.rest.client.http.HttpCaller;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.outbound.OxalisOutboundComponent;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;

@Slf4j
public class OutboundService implements IOutboundService {

	/**
	 * Variables useful to process REST calls.
	 */
	private static String restUrnGetterUri;
	private static String restDocumentGetterUri;
	private static String restSendStatusUri;

	@Inject
	private TransmissionRequestBuilder requestBuilder;

	@Inject
	private CertificateConfigManager certConfig;

	@Inject
	private RestConfigManager restConfig;

	@Inject
	private OxalisOutboundComponent outboundComponent;

	@Override
	public OxalisMdn sendFullPeppolMessageOnDemand(FullPeppolMessage fullPeppolMessage)
			throws OxalisTransmissionException, OxalisContentException, CertificateException {

		// Prepare Oxalis TransmissionMessage.
		TransmissionMessage transmissionMessage = NotierTransmissionRequestBuilder.build(requestBuilder, fullPeppolMessage);

		// Send Oxalis TransmissionMessage.
		TransmissionResponse response = send(transmissionMessage);

		// Access receipt.
		String receiptPayloadStringified = new String(response.primaryReceipt().getValue(), StandardCharsets.UTF_8);
		log.info("Received the following receipt: {}{}", new Object[] { System.getProperty("line.separator"), receiptPayloadStringified });

		// Build "OK" MDN for NoTI-ER.
		return buildMdn(null, OxalisStatusEnum.OK, "Receipt: " + receiptPayloadStringified, response);
	}

	@Override
	public void processOutboundFlow() throws Exception {

		// Setup REST configuration (if needed).
		setupOutboundRestConfiguration();

		// Get URN of documents that need to be sent on Peppol directly from
		// Notier via REST web service.
		UrnList urnListRetrievedFromNotier = retrieveUrnList();

		// Check the received response.
		checkUrnListContent(urnListRetrievedFromNotier);

		// Iterate over UrnList.NotierDocumentIndex collection in order to
		// send each document one by one.
		sendEachDocument(urnListRetrievedFromNotier);

	}

	private void sendEachDocument(UrnList urnListRetrievedFromNotier) {
		for (NotierDocumentIndex index : urnListRetrievedFromNotier.getDocuments()) {

			// Logging.
			log.info(MESSAGE_STARTING_TO_PROCESS_URN, index.getUrn());

			// Define OxalisMdn object.
			OxalisMdn oxalisMdn = null;

			try {

				// Retrieve PeppolMessage in json String format from NoTI-ER.
				String peppolMessageJson = retrieveSinglePeppolMessageFromNotier(index.getUrn());

				// Build TransmissionMessage object and send it on Peppol network. The status of
				// the transaction determines how the Oxalis MDN needs to be created.
				oxalisMdn = index.isInternal()
						? new OxalisMdn(index.getUrn(), OxalisStatusEnum.INTERNAL,
								"This document has not been sent on PEPPOL Network because it follows internal NoTI-ER process")
						: buildTransmissionAndSendOnPeppol(index.getUrn(), peppolMessageJson);

				log.info(MESSAGE_OUTBOUND_SUCCESS_FOR_URN, index.getUrn());

			} catch (Exception e) {

				// Build negative MDN.
				oxalisMdn = new OxalisMdn(index.getUrn(), OxalisStatusEnum.KO, e.getMessage());
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
				log.error(e.getMessage(), e);
			} finally {

				// Forward the OxalisMdn object to Notier in order to communicate the status of
				// transaction.
				sendJsonMdnToNotier(oxalisMdn);
			}
		}
	}

	private String retrieveSinglePeppolMessageFromNotier(String urn) throws IOException {

		HttpResponse get_response = HttpCaller.executeGet(certConfig, restDocumentGetterUri + urn);
		if (HttpCaller.responseStatusCodeIsValid(get_response))
			return HttpCaller.extractResponseContentAsUTF8String(get_response);

		throw new IOException("Some problem occurs during HTTP GET document getter response handling. Status code is \""
				+ get_response.getStatusLine().getStatusCode() + "\"");

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
	@Override
	public OxalisMdn buildTransmissionAndSendOnPeppol(String urn, String peppolMessageJsonFormat) throws OxalisTransmissionException, OxalisContentException {
		TransmissionRequest messageToSend = NotierTransmissionRequestBuilder.build(requestBuilder, peppolMessageJsonFormat);
		TransmissionResponse response = send(messageToSend);
		String receiptPayloadStringified = new String(response.primaryReceipt().getValue(), StandardCharsets.UTF_8);
		log.info("Received the following receipt: {}{}", new Object[] { System.getProperty("line.separator"), receiptPayloadStringified });
		/**
		 * Fase 3. Creo una notifica MDN Oxalis sulla base dell'esito dell'invio.
		 */
		OxalisMdn mdn = buildMdn(urn, OxalisStatusEnum.OK, null, response);
		try {
			mdn.getTransactionDetails().setReceipt(new ByteArrayInputStream(receiptPayloadStringified.getBytes()));
		} catch (Exception e) {
			log.warn("A problem occurred: {}", e.getMessage(), e);
		}
		return mdn;
	}

	/**
	 * Processa l'invio su rete Peppol del documento recuperato.
	 *
	 * @param documento
	 * @param urn
	 */
	@Override
	public TransmissionResponse send(TransmissionMessage documento) throws OxalisTransmissionException {
		return outboundComponent.getTransmitter().transmit(documento);
	}

	private UrnList retrieveUrnList() throws Exception {
		String jsonUrnGetterResponse = null;
		try {
			HttpResponse get_response = HttpCaller.executeGet(certConfig, restUrnGetterUri);
			if (HttpCaller.responseStatusCodeIsValid(get_response)) {
				jsonUrnGetterResponse = HttpCaller.extractResponseContentAsUTF8String(get_response);
			} else
				throw new IOException("Some problem occurs during HTTP GET URN getter response handling. Status code is \""
						+ get_response.getStatusLine().getStatusCode() + "\"");
			if (isEmptyOrNull(jsonUrnGetterResponse)) {
				log.error("Received response is empty");
				throw new Exception("Received response is empty");
			}
		} catch (Exception e) {
			throw new Exception("Empty response from URI " + restUrnGetterUri);
		}
		return GsonUtil.getInstance().fromJson(jsonUrnGetterResponse, UrnList.class);
	}

	private void checkUrnListContent(UrnList urnListRetrievedFromNotier) throws Exception {
		if (urnListRetrievedFromNotier != null) {
			log.info("Found {} {} to send on Peppol", urnListRetrievedFromNotier.getUrnCount(),
					(urnListRetrievedFromNotier.getUrnCount() == 1 ? "document" : "documents"));
		} else {
			log.error("Invalid response received from Notier: {}{}", new Object[] { System.getProperty("line.separator"), urnListRetrievedFromNotier });
			throw new Exception("Invalid response received from Notier (UrnList)");
		}
	}

	/**
	 * Provides an OxalisMdn.
	 *
	 * @param urn          is the document URN
	 * @param status       is the status of the sending
	 * @param errorMessage is the error message thrown by exceptions
	 * @return the mdn
	 */
	private OxalisMdn buildMdn(String urn, OxalisStatusEnum status, String errorMessage, TransmissionResponse response) {

		OxalisMdn mdn = new OxalisMdn(urn, status, OxalisStatusEnum.OK.equals(status) ? "Document has been sent successfully" : errorMessage);

		try {
			TransactionDetails details = new TransactionDetails();
			details.setEndpointUri(response.getEndpoint().getAddress().normalize().toString());
			details.setTimestamp(response.getTimestamp());
			details.setTransmissionIdentifier(response.getTransmissionIdentifier().toString());
			details.setTransportProfile(response.getTransportProtocol().getIdentifier());
			// details.setReceipt(response.getReceipts());

			mdn.setTransactionDetails(details);
		} catch (Exception e) {
			log.error("Problems during transaction details definition: {}", e.getMessage(), e);
		}

		return mdn;
	}

	/**
	 * Forward the OxalisMdn to Notier.
	 *
	 * @param oxalisMdn   is the status of the transaction
	 * @param urnDocument is the URN of involved document
	 */
	private void sendJsonMdnToNotier(OxalisMdn oxalisMdn) {
		try {
			HttpResponse resp = HttpCaller.executePost(certConfig, restSendStatusUri, "oxalisContent", GsonUtil.getPrettyPrintedInstance().toJson(oxalisMdn));
			String respContent = HttpCaller.extractResponseContentAsUTF8String(resp);
			log.debug("Received response contains {} characters", respContent.length());
		} catch (UnsupportedOperationException | IOException e) {
			log.error(MESSAGE_MDN_SEND_FAILED, oxalisMdn.getDocumentUrn(), e);
		}
	}

	/**
	 * @throws JobExecutionException if the configuration has not been setup
	 *                               properly.
	 */
	private void loadRestUriReferences() {
		/**
		 * Recupero la lista di URN corrispondenti ai documenti che devono essere
		 * inviati su rete Peppol.
		 */
		restUrnGetterUri = restConfig.readValue(RestConfigManager.CONFIG_KEY_REST_GETTER_URNS);
		restDocumentGetterUri = restConfig.readValue(RestConfigManager.CONFIG_KEY_REST_GETTER_DOCUMENT);
		restSendStatusUri = restConfig.readValue(RestConfigManager.CONFIG_KEY_REST_SENDER_STATUS);

		boolean restUrnConfigIsReady = !isEmptyOrNull(restUrnGetterUri);
		boolean restDocumentGetterConfigIsReady = !isEmptyOrNull(restDocumentGetterUri);
		boolean restSendStatusConfigIsReady = !isEmptyOrNull(restSendStatusUri);
		boolean isAllReadyAndSet = restUrnConfigIsReady && restDocumentGetterConfigIsReady && restSendStatusConfigIsReady;

		if (!isAllReadyAndSet) {
			String configStatus = "[URN getter=" + (restUrnConfigIsReady ? "OK]" : "ERROR]" + "; ");
			configStatus += "[document getter=" + (restDocumentGetterConfigIsReady ? "OK]" : "ERROR]; ");
			configStatus += "[send status=" + (restSendStatusConfigIsReady ? "OK]" : "ERROR]");
			log.error(MESSAGE_WRONG_CONFIGURATION_SETUP, configStatus);
			throw new IllegalArgumentException("REST configuration has not been properly setup. Status: \"" + configStatus + "\"");
		}
	}

	/**
	 * @throws JobExecutionException if the configuration has not been setup
	 *                               properly.
	 */
	private void setupOutboundRestConfiguration() {
		if (isEmptyOrNull(restDocumentGetterUri) || isEmptyOrNull(restDocumentGetterUri) || isEmptyOrNull(restSendStatusUri)) {
			loadRestUriReferences();
		}
	}

	private boolean isEmptyOrNull(String str) {
		return str == null || str.isEmpty();
	}
}
