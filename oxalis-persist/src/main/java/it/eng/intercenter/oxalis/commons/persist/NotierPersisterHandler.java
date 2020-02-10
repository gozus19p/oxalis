package it.eng.intercenter.oxalis.commons.persist;

import com.google.common.io.ByteStreams;
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
import org.apache.http.HttpResponse;
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
import it.eng.intercenter.oxalis.rest.client.http.HttpCaller;
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

        // Handle persist on NoTI-ER based on config value.
        if (persistOnNotierIsEnabled()) {

            // Persist on NoTI-ER.
            persistOnNotier(inboundMetadata, payloadPath);
        } else {

            // Persist only in file system.
            log.warn("NoTI-ER persist has been disabled, make sure that this is an intended behaviour!");
            super.persist(inboundMetadata, payloadPath);
        }

    }

    private void persistOnNotier(InboundMetadata inboundMetadata, Path payloadPath) throws IOException {
        try {

            // Retrieve HTTP POST URI to execute.
            String uri = restConfig.readValue(RestConfigManager.CONFIG_KEY_REST_DOCUMENT_INBOUND);

            // Build HTTP call.
            HttpNotierPost post = new HttpNotierPost(certificateConfig, uri, getParams(inboundMetadata, inboundMetadata.getHeader(), payloadPath));

            // Execute HTTP call.
            log.info("Parsing response from NoTI-ER...");
            HttpResponse response = post.execute();
            String responseContent = HttpCaller.extractResponseContentAsUTF8String(response);
            log.info("{}", responseContent);

            // Parse response received from NoTI-ER.
            OxalisMdn mdn = GsonUtil.getInstance().fromJson(responseContent, OxalisMdn.class);

            handleReceivedMdn(payloadPath, mdn, inboundMetadata);

        } catch (Exception e) {

            log.warn("Persisting document on file system due to previous error");
            log.error("Something went wrong during NoTI-ER persist phase. Cause: {}", e.getMessage(), e);

            // Persist on file system.
            super.persist(inboundMetadata, payloadPath);

            notifyTechnicalSupport(payloadPath, e);

        }
    }

    private void notifyTechnicalSupport(Path payloadPath, Exception e) {
        // Logging.
        log.error("An error occurred during persist: {}", e.getMessage(), e);

        // Send e-mail to Support NoTI-ER.
        try {
            sendEmailToSupportNotier(e.getMessage(), payloadPath);
        } catch (Exception e2) {
            log.error("Error while sending e-mail to NoTI-ER support. Cause: {}", e2.getMessage(), e2);
        }
    }

    private void handleReceivedMdn(Path payloadPath, OxalisMdn mdn, InboundMetadata inboundMetadata) {
        // Logging.
        if (mdn.hasPositiveStatus()) {
            log.info("Received document, succesfully sent on NoTI-ER");
            try {
                java.nio.file.Files.delete(payloadPath);
                log.info("Persist on NoTI-ER processed successfully, temp file removed");
            } catch (IOException e) {
                log.error("Persist on NoTI-ER processed successfully, but some error occurred during temp file removing. Cause: {}", e.getMessage(), e);
            }
        } else {
            log.warn("Received document, found some problems during sending process on NoTI-ER");
            
            // Send e-mail to Support NoTI-ER.
            try {
                sendEmailToSupportNotier(mdn.getMessage(), payloadPath);
            } catch (Exception e2) {
                log.error("Error while sending e-mail to NoTI-ER support. Cause: {}", e2.getMessage(), e2);
            }
            
            // Persisting on file system.
            try {
                log.warn("Something went wrong during persist on NoTI-ER, file system persist process starting. Cause: {}", mdn.getMessage());
                super.persist(inboundMetadata, payloadPath);
            } catch (IOException e) {
                log.error("Persist on file system failed: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * If this is evaluated to "true", Oxalis will send the receiving payload to
     * NoTI-ER, otherwise not.
     *
     * @author Manuel Gozzi
     * @date 25 nov 2019
     * @time 14:48:54
     * @return "true" if Oxalis must forward the received document to NoTI-ER,
     * "false" otherwise
     */
    private boolean persistOnNotierIsEnabled() {

        // Reading configuration.
        String persistOnNotierIsEnabled = restConfig.readValue(RestConfigManager.CONFIG_KEY_PERSIST_MODE);

        // The default is set to "true".
        return persistOnNotierIsEnabled != null ? new Boolean(persistOnNotierIsEnabled.trim().toLowerCase().intern()).booleanValue() : true;

    }

    /**
     * It sends an e-mail to NoTI-ER support, informing the technical team about
     * the failure.
     *
     * @author Manuel Gozzi
     * @date 25 nov 2019
     * @time 14:49:55
     * @param e is the Exception related to the outbound failure stack trace
     * @param payloadPath is the path related to the payload
     */
    private void sendEmailToSupportNotier(String errorMessage, Path payloadPath) {

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

        // Retrieve e-mail carbon copy and blind carbon copy.
        String config_knownCopy = emailConfig.readValue(EmailSenderConfigManager.CONFIG_KEY_EMAIL_KNOWN_COPY);
        String config_hiddenCopy = emailConfig.readValue(EmailSenderConfigManager.CONFIG_KEY_EMAIL_HIDDEN_COPY);

        // Build and send e-mail.
        prepareAndSendEmail(errorMessage, config_receiver, session, config_knownCopy, config_hiddenCopy);

    }

    /**
     * Finalize e-mail setup and process e-mail.
     *
     * @author Manuel Gozzi
     * @date 25 nov 2019
     * @time 14:51:02
     * @param errorMessage is the error message related to the issue
     * @param config_receiver is the receiver e-mail address
     * @param session is the session to use for e-mail processing
     * @param config_carbonCopy is the e-mail carbon copy receiver configured
     * @param config_blindCarbonCopy is the e-mail blind carbon copy receiver
     * configured
     */
    private void prepareAndSendEmail(String errorMessage, String config_receiver, Session session, String config_carbonCopy, String config_blindCarbonCopy) {
        Message email = new MimeMessage(session);

        // Prepare e-mail details.
        String[] m_receivers = config_receiver.contains(",") ? config_receiver.split(",") : new String[]{config_receiver};
        String[] m_knownCopies = config_carbonCopy != null
                ? (config_carbonCopy.contains(",") ? config_carbonCopy.split(",") : new String[]{config_carbonCopy})
                : null;
        String[] m_hiddenCopies = config_blindCarbonCopy != null
                ? (config_blindCarbonCopy.contains(",") ? config_blindCarbonCopy.split(",") : new String[]{config_blindCarbonCopy})
                : null;

        // Prepare subject and text.
        String m_subject = "Oxalis: failed to persist order (" + DATE_FORMATTER.format(new Date()) + " " + TIME_FORMATTER.format(new Date() + ")");
        String m_text = errorMessage;

        // Prepare sender.
        String m_sender = emailConfig.readValue(EmailSenderConfigManager.CONFIG_KEY_EMAIL_SENDER);

        try {
            setUpEmailDetails(email, m_receivers, m_knownCopies, m_hiddenCopies, m_subject, m_text, m_sender);

            // Send e-mail.
            Transport.send(email);

        } catch (MessagingException me) {
            // Logging.
            log.error("E-mail has not been sent, cause: {}", me.getMessage(), me);
        }
    }

    private void setUpEmailDetails(Message email, String[] m_receivers, String[] m_knownCopies, String[] m_hiddenCopies, String m_subject, String m_text,
            String m_sender) throws MessagingException, AddressException {
        // Set sender.
        email.setFrom(new InternetAddress(m_sender != null ? m_sender : "support.notier@regione.emilia-romagna.it"));

        // Set receivers.
        email.setRecipients(RecipientType.TO, getInternetAddresses(m_receivers));

        // Set carbon copy.
        if (m_knownCopies != null) {
            email.setRecipients(RecipientType.CC, getInternetAddresses(m_knownCopies));
        }

        // Set blind carbon copy.
        if (m_hiddenCopies != null) {
            email.setRecipients(RecipientType.BCC, getInternetAddresses(m_hiddenCopies));
        }

        // Set text and subject.
        email.setText(m_text);
        email.setSubject(m_subject);
    }

    /**
     * It converts a String[] into InternetAddress[].
     *
     * @author Manuel Gozzi
     * @date 25 nov 2019
     * @time 14:53:13
     * @param m_addressesString
     * @return
     * @throws AddressException
     */
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
     * @param payloadPath is the payload reference
     * @return the array of params to attach on POST request
     * @throws IOException if something goes wrong during payload management
     */
    private BasicNameValuePair[] getParams(InboundMetadata inboundMetadata, Header header, Path payloadPath) throws IOException {
        BasicNameValuePair[] arr = new BasicNameValuePair[3];

        byte[] payload = getPayloadFromPath(payloadPath);
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
    private final byte[] getPayloadFromPath(Path payloadPath) throws IOException {
        return Files.toByteArray(new File(payloadPath.normalize().toString()));
    }

}
