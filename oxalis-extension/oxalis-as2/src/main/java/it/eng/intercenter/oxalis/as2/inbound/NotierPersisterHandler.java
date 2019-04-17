package it.eng.intercenter.oxalis.as2.inbound;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.http.message.BasicNameValuePair;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import it.eng.intercenter.oxalis.commons.quartz.transmission.NotierTransmissionMessage;
import it.eng.intercenter.oxalis.config.ConfigNotierCertificate;
import it.eng.intercenter.oxalis.config.ConfigRestCall;
import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.rest.http.impl.HttpNotierPost;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.vefa.peppol.common.model.Header;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class NotierPersisterHandler implements PersisterHandler {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String SENT_PATH = "/sent/";
	private static final String LOCKED_PATH = "/locked/";

	@Inject
	ConfigRestCall config;

	@Inject
	ConfigNotierCertificate certConfig;

	@Named("inbound")
	private String inboundPath;

	@Override
	public Path persist(TransmissionIdentifier transmissionIdentifier, Header header, InputStream inputStream)
			throws IOException {
		throw new IOException("Unsupported operation");
	}

	@Override
	public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
		File payloadFile = new File(payloadPath.normalize().toString());
		
		String uri = config.readSingleProperty(ConfigRestCall.CONFIG_KEY_REST_DOCUMENT_INBOUND);
		HttpNotierPost post = new HttpNotierPost(certConfig, uri, getParams(inboundMetadata, payloadPath));
		
		try {
			String response = post.execute();
			
			log.info("Parsing response from Notier");
			OxalisMdn mdn = GSON.fromJson(response, OxalisMdn.class);
			log.info("{}", response);
			
			File destinationPath = new File(buildDestinationPath(mdn.hasPositiveStatus()));
			payloadFile.renameTo(destinationPath);
			log.warn("File {} has been moved to {}",
					new Object[] { payloadFile.getName(), destinationPath.getAbsolutePath() });
			
		} catch (IOException e) {
			log.error("I/O error: {}", e.getMessage(), e);
		} catch (Exception e) {
			log.error("An error occurs: {}", e.getMessage(), e);
		}

	}

	/**
	 * Builds destination path as String in which the file of received document will
	 * be moved to.
	 * 
	 * @param documentHasBeenSentSuccessfully determines if it has to be moved on
	 *                                        "sent" cathegory or "locked" cathegory
	 * @return the path as String
	 */
	private String buildDestinationPath(boolean documentHasBeenSentSuccessfully) {
		if (documentHasBeenSentSuccessfully) {
			log.info("Received document, succesfully sent on Notier");
		} else {
			log.error("Received document, found some problems during sending process on Notier");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(inboundPath);
		sb.append(documentHasBeenSentSuccessfully ? SENT_PATH : LOCKED_PATH);
		return sb.toString();
	}

	/**
	 * Prepares params to attach on HTTP POST request.
	 * 
	 * @param inboundMetadata is the metadata related to the document
	 * @param payloadPath     is the payload reference
	 * @return the array of params to attach on POST request
	 * @throws IOException if something goes wrong during payload management
	 */
	private BasicNameValuePair[] getParams(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
		BasicNameValuePair[] arr = new BasicNameValuePair[2];
		byte[] payload = getPayload(payloadPath);
		NotierTransmissionMessage tm = new NotierTransmissionMessage(inboundMetadata.getHeader(),
				new ByteArrayInputStream(payload), inboundMetadata.getTag());
		arr[0] = new BasicNameValuePair("peppolMessage", GSON.toJson(tm));
		arr[1] = new BasicNameValuePair("inboundMetadata", GSON.toJson(inboundMetadata));
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