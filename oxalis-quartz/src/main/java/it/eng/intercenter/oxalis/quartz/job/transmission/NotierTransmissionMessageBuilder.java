package it.eng.intercenter.oxalis.quartz.job.transmission;

import it.eng.intercenter.oxalis.integration.dto.PeppolMessage;
import it.eng.intercenter.oxalis.integration.dto.util.GsonUtil;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.api.tag.Tag;
import no.difi.oxalis.outbound.transmission.TransmissionRequestBuilder;
import no.difi.vefa.peppol.common.model.DocumentTypeIdentifier;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.common.model.ProcessIdentifier;

/**
 * This class provides utilities to build Notier TransmissionMessage.
 * 
 * @author Manuel Gozzi
 */
public class NotierTransmissionMessageBuilder {

	/**
	 * Costruisce un TransmissionMessage (NotierTransmissionMessage, creato per
	 * copia da DefaultTransmissionMessage) partendo da un DTO.
	 * 
	 * @param peppolMessageJsonFormat
	 * @return
	 * @throws OxalisContentException 
	 * @throws OxalisTransmissionException 
	 * @throws NotierDocumentCastException
	 */
	public static TransmissionRequest buildTransmissionRequestFromPeppolMessage(TransmissionRequestBuilder builder, String peppolMessageJsonFormat) throws OxalisTransmissionException, OxalisContentException {
		PeppolMessage peppolMessage = GsonUtil.getInstance().fromJson(peppolMessageJsonFormat, PeppolMessage.class);
		TransmissionRequest request = builder.payLoad(peppolMessage.getPayload())
				.sender(ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdSender()))
				.receiver(ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdReceiver()))
				.processType(ProcessIdentifier.of(peppolMessage.getHeader().getProcessIdentifier()))
				.documentType(DocumentTypeIdentifier.of(peppolMessage.getHeader().getDocumentTypeIdentifier()))
				.build();
				
//		TransmissionMessage message = new NotierTransmissionMessage(extractHeaderFromPeppolMessage(peppolMessage),
//				peppolMessage.getPayload(), extractTagFromPeppolMessage(peppolMessage));
//		return message;
		return request;
	}

	/**
	 * Recupera l'Header da un definito DTO che rappresenta il documento.
	 * 
	 * @param obj
	 * @return
	 */
	private static Header extractHeaderFromPeppolMessage(PeppolMessage peppolMessage) {
		return Header.of(ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdSender()),
				ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdReceiver()),
				ProcessIdentifier.of(peppolMessage.getHeader().getProcessIdentifier()),
				DocumentTypeIdentifier.of(peppolMessage.getHeader().getDocumentTypeIdentifier()));
	}

	/**
	 * Recupera il Tag da un definito DTO che rappresenta il documento.
	 * 
	 * @param obj
	 * @return
	 */
	private static Tag extractTagFromPeppolMessage(PeppolMessage peppolMessage) {
		return Tag.of(peppolMessage.getHeader().getParticipantIdSender());
	}

}
