package it.eng.intercenter.oxalis.quartz.job.transmission;

import it.eng.intercenter.oxalis.integration.dto.PeppolMessage;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;
import no.difi.vefa.peppol.common.model.DocumentTypeIdentifier;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.common.model.ProcessIdentifier;

/**
 * @author Manuel Gozzi
 */
public class NotierTransmissionRequestBuilder {

	/**
	 * This method builds a TransmissionRequest starting from a PeppolMessage object
	 * obtained by Notier.
	 * 
	 * @param builder is the object responsible to create a TrasmissionRequest
	 * @param peppolMessageJsonFormat is the json String representation of PeppolMessage DTO object.
	 * @return the TransmissionRequest built (that contains SBDH also)
	 * @throws OxalisContentException
	 * @throws OxalisTransmissionException
	 */
	public static TransmissionRequest build(TransmissionRequestBuilder builder,
			String peppolMessageJsonFormat) throws OxalisTransmissionException, OxalisContentException {
		PeppolMessage peppolMessage = GsonUtil.getInstance().fromJson(peppolMessageJsonFormat, PeppolMessage.class);
		return builder.payLoad(peppolMessage.getPayload())
				.sender(ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdSender()))
				.receiver(ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdReceiver()))
				.processType(ProcessIdentifier.of(peppolMessage.getHeader().getProcessIdentifier()))
				.documentType(DocumentTypeIdentifier.of(peppolMessage.getHeader().getDocumentTypeIdentifier())).build();
	}

}
