package no.difi.oxalis.commons.quartz.transmission.notier;

import com.google.gson.GsonBuilder;

import it.eng.intercenter.oxalis.quartz.dto.PeppolMessage;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.tag.Tag;
import no.difi.vefa.peppol.common.model.DocumentTypeIdentifier;
import no.difi.vefa.peppol.common.model.Header;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.common.model.ProcessIdentifier;

/**
 * Definisce un elenco di funzionalità atte a costruisce un oggeto
 * TransmissionMessage partendo da un documento ricevuto da Notier.
 * 
 * @author Manuel Gozzi
 */
@Slf4j
public class NotierTransmissionMessageBuilder {

	/**
	 * Costruisce un TransmissionMessage (NotierTransmissionMessage, creato per
	 * copia da DefaultTransmissionMessage) partendo da un DTO.
	 * 
	 * @param peppolMessageJsonFormat
	 * @return
	 * @throws NotierDocumentCastException
	 */
	public static TransmissionMessage buildTransmissionMessageFromPeppolMessage(String peppolMessageJsonFormat) {
		log.info("Building TransmissionMessage from PeppolMessage");
		PeppolMessage peppolMessage = new GsonBuilder().setPrettyPrinting().create().fromJson(peppolMessageJsonFormat,
				PeppolMessage.class);
		TransmissionMessage message = new NotierTransmissionMessage(extractHeaderFromPeppolMessage(peppolMessage),
				peppolMessage.getPayload(), extractTagFromPeppolMessage(peppolMessage));
		return message;
	}

	/**
	 * Recupera l'Header da un definito DTO che rappresenta il documento.
	 * 
	 * @param obj
	 * @return
	 */
	private static Header extractHeaderFromPeppolMessage(PeppolMessage peppolMessage) {
		Header header = new Header();
		header.sender(ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdSender()));
		header.receiver(ParticipantIdentifier.of(peppolMessage.getHeader().getParticipantIdReceiver()));
		header.process(ProcessIdentifier.of(peppolMessage.getHeader().getProcessIdentifier()));
		header.documentType(DocumentTypeIdentifier.of(peppolMessage.getHeader().getDocumentTypeIdentifier()));
		return header;
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
