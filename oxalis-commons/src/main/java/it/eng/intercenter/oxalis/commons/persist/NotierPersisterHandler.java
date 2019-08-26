package it.eng.intercenter.oxalis.commons.persist;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.http.message.BasicNameValuePair;

import com.google.common.io.Files;
import com.google.inject.Inject;

import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.OxalisMessage;
import it.eng.intercenter.oxalis.integration.dto.PeppolDetails;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.RestConfigManager;
import it.eng.intercenter.oxalis.rest.client.http.types.HttpNotierPost;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.persist.ExceptionPersister;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.api.util.Type;
import no.difi.oxalis.commons.persist.DefaultPersisterHandler;
import no.difi.vefa.peppol.common.model.Header;

/**
 * @author Manuel Gozzi
 */
@Singleton
@Type("default")
@Slf4j
public class NotierPersisterHandler extends DefaultPersisterHandler {

	@Inject
	public NotierPersisterHandler(PayloadPersister payloadPersister, ReceiptPersister receiptPersister, ExceptionPersister exceptionPersister) {
		super(payloadPersister, receiptPersister, exceptionPersister);
	}

	@Inject
	RestConfigManager config;

	@Inject
	CertificateConfigManager certConfig;

	@Override
	public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
		try {
			// Retrieve HTTP POST URI to execute.
			String uri = config.readValue(RestConfigManager.CONFIG_KEY_REST_DOCUMENT_INBOUND);

			// Build HTTP call.
			HttpNotierPost post = new HttpNotierPost(certConfig, uri, getParams(inboundMetadata, inboundMetadata.getHeader(), payloadPath));

			// Execute HTTP call.
			String response = post.execute();
			log.info("Parsing response from NoTI-ER");
			log.info("{}", response);

			// Parse response received from NoTI-ER.
			OxalisMdn mdn = GsonUtil.getInstance().fromJson(response, OxalisMdn.class);

			// Logging.
			if (mdn.hasPositiveStatus()) {
				log.info("Received document, succesfully sent on Notier");
			} else {
				log.warn("Received document, found some problems during sending process on Notier");
			}

		} catch (Exception e) {
			super.persist(inboundMetadata, payloadPath);
			log.error("An error occurred during persist: {}", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Prepares params to attach on HTTP POST request.
	 *
	 * @param inboundMetadata is the metadata related to the document
	 * @param payloadPath     is the payload reference
	 * @return the array of params to attach on POST request
	 * @throws IOException if something goes wrong during payload management
	 */
	private BasicNameValuePair[] getParams(InboundMetadata inboundMetadata, Header header, Path payloadPath) throws IOException {
		BasicNameValuePair[] arr = new BasicNameValuePair[3];

		byte[] payload = getPayload(payloadPath);
		ByteArrayInputStream bais = new ByteArrayInputStream(payload);

		OxalisMessage oxalisMessage = new OxalisMessage(inboundMetadata.getTransmissionIdentifier().getIdentifier(),
				new PeppolDetails(header.getSender().getIdentifier(), header.getReceiver().getIdentifier(), header.getProcess().getIdentifier(),
						header.getDocumentType().getIdentifier()),
				null, // timestamp
				null, //
				null, inboundMetadata.getTransportProtocol().getIdentifier(), inboundMetadata.getDigest().getMethod().name(),
				inboundMetadata.getDigest().getValue(), inboundMetadata.getReceipts().get(0).getValue(), inboundMetadata.getTag().toString());

		arr[0] = new BasicNameValuePair("document", GsonUtil.getPrettyPrintedInstance().toJson(oxalisMessage));
		arr[1] = new BasicNameValuePair("peppolPayload", GsonUtil.getPrettyPrintedInstance().toJson(new ByteArrayInputStream(IOUtils.toByteArray(bais))));
		arr[2] = new BasicNameValuePair("isInternal", "false");

		return arr;
	}

	/**
	 * Retrieves byte[] payload from a given Path.
	 *
	 * @param payloadPath is the Path related to the Payload
	 * @return the payload in byte[] format
	 * @throws IOException if something goes wrong during I/O access
	 */
	private final byte[] getPayload(Path payloadPath) throws IOException {
		return Files.toByteArray(new File(payloadPath.normalize().toString()));
	}

}
