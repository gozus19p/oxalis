package it.eng.intercenter.oxalis.notier.core.service.impl;

import com.google.inject.Inject;
import it.eng.intercenter.oxalis.integration.dto.*;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisStatusEnum;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import it.eng.intercenter.oxalis.notier.core.service.api.IOutboundService;
import it.eng.intercenter.oxalis.notier.core.service.util.NotierTransmissionRequestBuilder;
import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.RestConfigManager;
import it.eng.intercenter.oxalis.rest.client.http.HttpCaller;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.outbound.TransmissionMessage;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.outbound.OxalisOutboundComponent;
import network.oxalis.outbound.transmission.TransmissionRequestBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;

import static it.eng.intercenter.oxalis.rest.client.util.ConfigManagerUtil.*;

/**
 * @author Manuel Gozzi
 */
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
		log.info("Received the following receipt: {}{}", System.getProperty("line.separator"), receiptPayloadStringified);

		// Build "OK" MDN for NoTI-ER.
		return buildOkMdn(null, response);
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
				oxalisMdn = new OxalisMdn(
						index.getUrn(),
						OxalisStatusEnum.KO,
						e.getMessage()
						// formatThrowableMessage(e)
				);
				log.error(MESSAGE_OUTBOUND_FAILED_FOR_URN, index.getUrn());
				log.error(e.getMessage(), e);
			} finally {

				// Forward the OxalisMdn object to Notier in order to communicate the status of
				// transaction.
				sendJsonMdnToNotier(oxalisMdn);
			}
		}
	}

	/**
	 * Method that formats properly the whole messages of causes related to the given Throwable instance.
	 *
	 * @param throwable is the Throwable instance
	 * @return the full representation of cause message
	 */
	@SuppressWarnings("unused")
	private String formatThrowableMessage(Throwable throwable) {
		if (throwable == null) {
			return "unable to detect error message";
		}
		return getFullExceptionCauseMessage(throwable, 1);
	}

	/**
	 * Recursive method that formats properly the whole messages of causes related to the given Throwable instance.
	 *
	 * @param e     is the Throwable instance
	 * @param index is the index that needs to be printed on message
	 * @return the full representation of message
	 */
	private String getFullExceptionCauseMessage(Throwable e, int index) {
		return e != null && e.getCause() != null ?
				// If the given Throwable instance has a deeper cause, I keep going deeply
				getSingleExceptionMessage(e, index) + getFullExceptionCauseMessage(e.getCause(), index + 1)
				: (
				e != null ?
						getSingleExceptionMessage(e, index) : ""
		);
	}

	/**
	 * This one properly formats the exception message.
	 *
	 * @param e     is the <code>Throwable</code> that occurred
	 * @param index is the index to print on message
	 * @return the message properly formatted
	 */
	private String getSingleExceptionMessage(Throwable e, int index) {
		return String.format(
				"[%s] \"%s\": %s%s",
				index,
				e.getClass().getName(),
				e.getMessage(),
				System.getProperty("line.separator")
		);
	}

	/**
	 * This one asks NoTI-ER for a message to send on PEPPOL.
	 *
	 * @param urn represents the document index that needs to be sent on PEPPOL network
	 * @return the .json String representation of the given document
	 * @throws IOException if something goes wrong during HTTP call processing
	 */
	private String retrieveSinglePeppolMessageFromNotier(String urn) throws IOException {

		return HttpCaller.executeGet(certConfig, restDocumentGetterUri + urn);
	}

	/**
	 * Builds a TransmissionMessage object and send it on Peppol.
	 *
	 * @param urn                     is the URN of the involved document
	 * @param peppolMessageJsonFormat is the json format of PeppolMessage instance
	 *                                related to involved document
	 * @return the final OxalisMdn object
	 * @throws OxalisTransmissionException if some problems occur while sending
	 *                                     document on Peppol network
	 * @throws OxalisContentException      if something goes wrong with content
	 */
	@Override
	public OxalisMdn buildTransmissionAndSendOnPeppol(String urn, String peppolMessageJsonFormat) throws OxalisTransmissionException, OxalisContentException {

		// Building transmission request
		TransmissionRequest messageToSend = NotierTransmissionRequestBuilder.build(requestBuilder, peppolMessageJsonFormat);
		TransmissionResponse response = send(messageToSend);
		String receiptPayloadString = new String(response.primaryReceipt().getValue(), StandardCharsets.UTF_8);
		log.info("Received the following receipt: {}{}", System.getProperty("line.separator"), receiptPayloadString);

		// Third phase, creating an MDN notification based on sending outcome
		OxalisMdn mdn = buildOkMdn(urn, response);
		try {
			mdn.getTransactionDetails().setReceipt(
					new ByteArrayInputStream(receiptPayloadString.getBytes())
			);
		} catch (Exception e) {
			log.warn("A problem occurred: {}", e.getMessage(), e);
		}
		return mdn;
	}

	@Override
	public TransmissionResponse send(TransmissionMessage transmissionMessage) throws OxalisTransmissionException {
		log.info("Transmitting...");
		return outboundComponent.getTransmitter().transmit(transmissionMessage);
	}

	/**
	 * This one retrieves URN list related to documents that need to be sent on PEPPOL network.
	 *
	 * @return an UrnList instance that contains all the URN occurrences
	 * @throws Exception in case of error
	 */
	private UrnList retrieveUrnList() throws Exception {

		String jsonUrnGetterResponse;
		try {

			jsonUrnGetterResponse = HttpCaller.executeGet(certConfig, restUrnGetterUri);
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
			log.info("Found {} {} to send on PEPPOL", urnListRetrievedFromNotier.getUrnCount(),
					(urnListRetrievedFromNotier.getUrnCount() == 1 ? "document" : "documents"));
		} else {
			log.error("Invalid response received from Notier: {}{}", System.getProperty("line.separator"), urnListRetrievedFromNotier);
			throw new Exception("Invalid response received from Notier (UrnList)");
		}
	}

	/**
	 * Provides an OxalisMdn with status OK.
	 *
	 * @param urn is the document URN
	 * @return the mdn
	 */
	private OxalisMdn buildOkMdn(String urn, TransmissionResponse response) {

		OxalisMdn mdn = new OxalisMdn(urn, OxalisStatusEnum.OK, "Document has been sent successfully");

		try {
			TransactionDetails details = new TransactionDetails();
			details.setEndpointUri(response.getEndpoint().getAddress().normalize().toString());
			details.setTimestamp(response.getTimestamp());
			details.setTransmissionIdentifier(response.getTransmissionIdentifier().toString());
			details.setTransportProfile(response.getTransportProtocol().getIdentifier());
			// details.setReceipt(response.getReceipts());

			mdn.setTransactionDetails(details);
		} catch (Exception e) {
			log.warn("Problems during transaction details definition: {}", e.getMessage(), e);
		}

		return mdn;
	}

	/**
	 * Forward the OxalisMdn to Notier.
	 *
	 * @param oxalisMdn is the status of the transaction
	 */
	private void sendJsonMdnToNotier(OxalisMdn oxalisMdn) {
		try {
			String respContent = HttpCaller.executePost(certConfig, restSendStatusUri, "oxalisContent", GsonUtil.getPrettyPrintedInstance().toJson(oxalisMdn));
			log.info("Received response contains {} characters", respContent.length());
		} catch (UnsupportedOperationException | IOException e) {
			log.error(MESSAGE_MDN_SEND_FAILED, oxalisMdn.getDocumentUrn(), e);
		}
	}

	/**
	 * This loads URI references reading them from configuration.
	 */
	private void loadRestUriReferences() {

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
	 * This initializes REST URI configuration, if needed.
	 */
	private void setupOutboundRestConfiguration() {
		if (isEmptyOrNull(restUrnGetterUri) || isEmptyOrNull(restDocumentGetterUri) || isEmptyOrNull(restSendStatusUri)) {
			loadRestUriReferences();
		}
	}

	/**
	 * Basic utility method that checks for String empty or null.
	 *
	 * @param str is the string to check
	 * @return <code>true</code> if the given string is empty or null, <code>false</code> otherwise
	 */
	private boolean isEmptyOrNull(String str) {
		return str == null || str.isEmpty();
	}
}
