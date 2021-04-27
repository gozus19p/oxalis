package it.eng.intercenter.oxalis.notier.core.service.util;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

import it.eng.intercenter.oxalis.integration.dto.FullPeppolMessage;
import it.eng.intercenter.oxalis.integration.dto.PeppolMessage;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.outbound.transmission.TransmissionRequestBuilder;
import network.oxalis.sniffer.identifier.InstanceId;
import network.oxalis.vefa.peppol.common.model.DocumentTypeIdentifier;
import network.oxalis.vefa.peppol.common.model.Endpoint;
import network.oxalis.vefa.peppol.common.model.ParticipantIdentifier;
import network.oxalis.vefa.peppol.common.model.ProcessIdentifier;
import network.oxalis.vefa.peppol.common.model.TransportProfile;

/**
 * @author Manuel Gozzi
 */
public class NotierTransmissionRequestBuilder {

	private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
	private static final String END_CERT = "-----END CERTIFICATE-----";

	/**
	 * This method builds a TransmissionRequest starting from a PeppolMessage object
	 * obtained by Notier.
	 *
	 * @param builder                 is the object responsible to create a
	 *                                TrasmissionRequest
	 * @param peppolMessageJsonFormat is the json String representation of
	 *                                PeppolMessage DTO object.
	 * @return the TransmissionRequest built (that contains SBDH also)
	 * @throws OxalisContentException
	 * @throws OxalisTransmissionException
	 */
	public static TransmissionRequest build(TransmissionRequestBuilder builder, String peppolMessageJsonFormat)
			throws OxalisTransmissionException, OxalisContentException {
		PeppolMessage peppolMessage = GsonUtil.getInstance().fromJson(peppolMessageJsonFormat, PeppolMessage.class);
		return prepareForLookup(builder, peppolMessage);
	}

	private static TransmissionRequest prepareForLookup(TransmissionRequestBuilder builder, PeppolMessage peppolMessage)
			throws OxalisTransmissionException, OxalisContentException {

		// Reset builder.
		builder.reset();
		builder.setTransmissionBuilderOverride(true);

		// Prepare metadata.
		ParticipantIdentifier sender = ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdSender());
		ParticipantIdentifier receiver = ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdReceiver());
		ProcessIdentifier processType = ProcessIdentifier.of(peppolMessage.getHeader().getProcessIdentifier());
		DocumentTypeIdentifier documentType = DocumentTypeIdentifier.of(peppolMessage.getHeader().getDocumentTypeIdentifier());
		InstanceId instanceId = new InstanceId();

		// Check for null pointers.
		verifyHeaderMetadataForLookup(sender, receiver, processType, documentType);

		return builder.payLoad(peppolMessage.getPayload()).sender(sender).receiver(receiver).processType(processType).documentType(documentType)
				.instanceId(instanceId).overrideAs2Endpoint(null).build();
	}

	public static TransmissionRequest build(TransmissionRequestBuilder builder, FullPeppolMessage fullPeppolMessage)
			throws OxalisTransmissionException, OxalisContentException, CertificateException {

		// I must do lookup here.
		if (fullPeppolMessage.performLookup()) {
			return prepareForLookup(builder, fullPeppolMessage);
		}

		// Avoid lookup here.
		builder.reset();
		builder.setTransmissionBuilderOverride(true);

		// Prepare metadata.
		ParticipantIdentifier sender = ParticipantIdentifier.of(fullPeppolMessage.getHeader().getParticipantIdSender());
		ParticipantIdentifier receiver = ParticipantIdentifier.of(fullPeppolMessage.getHeader().getParticipantIdReceiver());
		ProcessIdentifier processType = ProcessIdentifier.of(fullPeppolMessage.getHeader().getProcessIdentifier());
		DocumentTypeIdentifier documentType = DocumentTypeIdentifier.of(fullPeppolMessage.getHeader().getDocumentTypeIdentifier());
		InstanceId instanceId = new InstanceId();
		TransportProfile transportProfile = TransportProfile.of(fullPeppolMessage.getTransportProfile());
		URI endpointUri = URI.create(fullPeppolMessage.getEndpointAPUri());
		X509Certificate endpointCertificate = parseCert(fullPeppolMessage.getEndpointAPCertificate());

		// Check for null pointers.
		verifyHeaderMetadataAvoidLookup(sender, receiver, processType, documentType, transportProfile, endpointUri, endpointCertificate);

		return builder.payLoad(fullPeppolMessage.getPayload()).sender(sender).receiver(receiver).processType(processType).documentType(documentType)
				.overrideAs2Endpoint(Endpoint.of(transportProfile, endpointUri, endpointCertificate)).instanceId(instanceId).creationDateAndTime(new Date())
				.build();
	}

	private static X509Certificate parseCert(String pemCertificate) throws CertificateException {
		// before decoding we need to get rod off the prefix and suffix
		byte[] decoded = Base64.getDecoder().decode(pemCertificate.replaceAll(BEGIN_CERT, "").replaceAll(END_CERT, ""));
		return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decoded));
	}

	private static void verifyHeaderMetadataAvoidLookup(ParticipantIdentifier sender, ParticipantIdentifier receiver, ProcessIdentifier processType,
			DocumentTypeIdentifier documentType, TransportProfile transportProfile, URI endpointUri, X509Certificate endpointCertificate)
			throws OxalisContentException {

		if (sender == null)
			throw new OxalisContentException("Sender participant identifier received from NoTI-ER is null");

		if (receiver == null)
			throw new OxalisContentException("Receiver participant identifier received from NoTI-ER is null");

		if (processType == null)
			throw new OxalisContentException("Process type identifier received from NoTI-ER is null");

		if (documentType == null)
			throw new OxalisContentException("Document type identifier received from NoTI-ER is null");

		if (transportProfile == null)
			throw new OxalisContentException("Transport profile identifier received from NoTI-ER is null");

		if (endpointUri == null)
			throw new OxalisContentException("Endpoint URI received from NoTI-ER is null");

		if (endpointCertificate == null)
			throw new OxalisContentException("Endpoint certificate received from NoTI-ER is null");

	}

	private static void verifyHeaderMetadataForLookup(ParticipantIdentifier sender, ParticipantIdentifier receiver, ProcessIdentifier processType,
			DocumentTypeIdentifier documentType) throws OxalisContentException {

		if (sender == null)
			throw new OxalisContentException("Sender participant identifier received from NoTI-ER is null");

		if (receiver == null)
			throw new OxalisContentException("Receiver participant identifier received from NoTI-ER is null");

		if (processType == null)
			throw new OxalisContentException("Process type identifier received from NoTI-ER is null");

		if (documentType == null)
			throw new OxalisContentException("Document type identifier received from NoTI-ER is null");

	}

}
