package it.eng.intercenter.oxalis.notier.core.service.util;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import it.eng.intercenter.oxalis.integration.dto.FullPeppolMessage;
import it.eng.intercenter.oxalis.integration.dto.PeppolMessage;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;
import no.difi.vefa.peppol.common.model.DocumentTypeIdentifier;
import no.difi.vefa.peppol.common.model.Endpoint;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.common.model.ProcessIdentifier;
import no.difi.vefa.peppol.common.model.TransportProfile;

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
		return builder.payLoad(peppolMessage.getPayload()).sender(ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdSender()))
				.receiver(ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdReceiver()))
				.processType(ProcessIdentifier.of(peppolMessage.getHeader().getProcessIdentifier()))
				.documentType(DocumentTypeIdentifier.of(peppolMessage.getHeader().getDocumentTypeIdentifier())).build();
	}

	public static TransmissionRequest build(TransmissionRequestBuilder builder, FullPeppolMessage fullPeppolMessage)
			throws OxalisTransmissionException, OxalisContentException, CertificateException {
		if (fullPeppolMessage.getEndpointAPCertificate() == null && fullPeppolMessage.getEndpointAPUri() == null
				&& fullPeppolMessage.getTransportProfile() == null) {
			// I have to do lookup here.
			return prepareForLookup(builder, fullPeppolMessage);
		}
		// Avoid lookup here.
		return builder.payLoad(fullPeppolMessage.getPayload()).sender(ParticipantIdentifier.of(fullPeppolMessage.getHeader().getParticipantIdSender()))
				.receiver(ParticipantIdentifier.of(fullPeppolMessage.getHeader().getParticipantIdReceiver()))
				.processType(ProcessIdentifier.of(fullPeppolMessage.getHeader().getProcessIdentifier()))
				.documentType(DocumentTypeIdentifier.of(fullPeppolMessage.getHeader().getDocumentTypeIdentifier()))
				.overrideAs2Endpoint(Endpoint.of(TransportProfile.of(fullPeppolMessage.getTransportProfile()), URI.create(fullPeppolMessage.getEndpointAPUri()),
						parseCert(fullPeppolMessage.getEndpointAPCertificate())))
				.build();
	}

	private static X509Certificate parseCert(String pemCertificate) throws CertificateException {
		// before decoding we need to get rod off the prefix and suffix
		byte[] decoded = Base64.getDecoder().decode(pemCertificate.replaceAll(BEGIN_CERT, "").replaceAll(END_CERT, ""));
		return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decoded));
	}

}
