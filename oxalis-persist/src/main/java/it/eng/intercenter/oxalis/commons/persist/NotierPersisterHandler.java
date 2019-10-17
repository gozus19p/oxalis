package it.eng.intercenter.oxalis.commons.persist;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Singleton;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.http.message.BasicNameValuePair;

import com.google.common.io.Files;
import com.google.inject.Inject;

import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.OxalisMessage;
import it.eng.intercenter.oxalis.integration.dto.PeppolDetails;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.EmailSenderConfigManager;
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

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yyyy");
	private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("hh:mm");

	@Inject
	public NotierPersisterHandler(PayloadPersister payloadPersister, ReceiptPersister receiptPersister, ExceptionPersister exceptionPersister) {
		super(payloadPersister, receiptPersister, exceptionPersister);
	}

	@Inject
	RestConfigManager restConfig;

	@Inject
	CertificateConfigManager certificateConfig;

	@Inject
	EmailSenderConfigManager emailConfig;

	@Override
	public void persist(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
		try {
			// Retrieve HTTP POST URI to execute.
			String uri = restConfig.readValue(RestConfigManager.CONFIG_KEY_REST_DOCUMENT_INBOUND);

			// Build HTTP call.
			HttpNotierPost post = new HttpNotierPost(certificateConfig, uri, getParams(inboundMetadata, inboundMetadata.getHeader(), payloadPath));

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
			// Persist on file system.
			super.persist(inboundMetadata, payloadPath);

			// Logging.
			log.error("An error occurred during persist: {}", e.getMessage(), e);

			// Send e-mail to Support NoTI-ER.
			sendEmailToSupportNotier(e, payloadPath);

			// Throw exception.
			throw e;
		}
	}

	private void sendEmailToSupportNotier(Exception e, Path payloadPath) {
		// Retrieve username and password in order to access e-mail.
		String username = emailConfig.readValue(EmailSenderConfigManager.CONFIG_KEY_S_USERNAME);
		String password = emailConfig.readValue(EmailSenderConfigManager.CONFIG_KEY_S_PASSWORD);

		// Retrieve e-mail receivers (mandatory).
		String config_receiver = emailConfig.readValue(EmailSenderConfigManager.CONFIG_KEY_EMAIL_RECEIVER);

		// Check for valid configuration.
		if (config_receiver == null || config_receiver.trim().isEmpty()) {
			log.error("CRITICAL: no e-mail receiver configured in file \"{}\"; this means that no one will be notified when NoTI-ER persist fail!",
					EmailSenderConfigManager.printConfigurationFileName());
			return;
		}

		// Check for valid username and password.
		if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
			log.error("CRITICAL: no username and password configured in file \"{}\"; this means that it is not possible to send e-mails!",
					EmailSenderConfigManager.printConfigurationFileName());
			return;
		}

		// Prepare Session in order to build MimeMessage.
		Session session = Session.getInstance(EmailSenderConfigManager.getPropertiesForSession(), new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		// Retrieve e-mail known copies (optional).
		String config_knownCopy = emailConfig.readValue(EmailSenderConfigManager.CONFIG_KEY_EMAIL_KNOWN_COPY);
		String config_hiddenCopy = emailConfig.readValue(EmailSenderConfigManager.CONFIG_KEY_EMAIL_HIDDEN_COPY);

		prepareAndSendEmail(e, config_receiver, session, config_knownCopy, config_hiddenCopy);

	}

	private void prepareAndSendEmail(Exception e, String config_receiver, Session session, String config_knownCopy, String config_hiddenCopy) {
		Message email = new MimeMessage(session);

		// Prepare receivers.
		String[] m_receivers = config_receiver.contains(",") ? config_receiver.split(",") : new String[] { config_receiver };
		String[] m_knownCopies = config_knownCopy != null ? (config_knownCopy.contains(",") ? config_knownCopy.split(",") : new String[] { config_knownCopy })
				: null;
		String[] m_hiddenCopies = config_hiddenCopy != null
				? (config_hiddenCopy.contains(",") ? config_hiddenCopy.split(",") : new String[] { config_hiddenCopy })
				: null;

		// Prepare subject and text.
		String m_subject = "Oxalis: failed to persist order (" + DATE_FORMATTER.format(new Date()) + " " + TIME_FORMATTER.format(new Date() + ")");
		String m_text = e.getMessage();

		// Prepare sender.
		String m_sender = emailConfig.readValue(EmailSenderConfigManager.CONFIG_KEY_EMAIL_SENDER);

		try {
			// Set sender.
			email.setFrom(new InternetAddress(m_sender != null ? m_sender : "support.notier@regione.emilia-romagna.it"));

			// Set receivers.
			email.setRecipients(RecipientType.TO, getInternetAddresses(m_receivers));
			if (m_knownCopies != null) {
				email.setRecipients(RecipientType.CC, getInternetAddresses(m_knownCopies));
			}
			if (m_hiddenCopies != null) {
				email.setRecipients(RecipientType.BCC, getInternetAddresses(m_hiddenCopies));
			}

			// Set text and subject.
			email.setText(m_text);
			email.setSubject(m_subject);

			// Send e-mail.
			Transport.send(email);

		} catch (MessagingException me) {
			// Logging.
			log.error("E-mail has not been sent, cause: {}", me.getMessage(), me);
		}
	}

	private InternetAddress[] getInternetAddresses(String[] m_addressesString) throws AddressException {
		InternetAddress[] addresses = new InternetAddress[m_addressesString.length];
		for (int i = 0; i < m_addressesString.length; i++) {
			addresses[i] = new InternetAddress(m_addressesString[i]);
		}
		return addresses;
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
