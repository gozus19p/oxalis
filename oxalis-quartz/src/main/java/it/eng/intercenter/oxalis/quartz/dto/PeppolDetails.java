package it.eng.intercenter.oxalis.quartz.dto;

/**
 * Classe che conserva le informazioni relative ai metadati per inviare un
 * documento su rete Peppol.
 * 
 * @author Manuel Gozzi
 * @date 21 feb 2019
 * @time 16:24:27
 */
public class PeppolDetails {

	private String participantIdSender;
	private String participantIdReceiver;
	private String processIdentifier;
	private String documentTypeIdentifier;

	public PeppolDetails(String participantIdSender, String participantIdReceiver, String processIdentifier,
			String documentTypeIdentifier) {
		this.participantIdSender = participantIdSender;
		this.participantIdReceiver = participantIdReceiver;
		this.processIdentifier = processIdentifier;
		this.documentTypeIdentifier = documentTypeIdentifier;
	}

	public String getParticipantIdSender() {
		return participantIdSender;
	}

	public String getParticipantIdReceiver() {
		return participantIdReceiver;
	}

	public String getProcessIdentifier() {
		return processIdentifier;
	}

	public String getDocumentTypeIdentifier() {
		return documentTypeIdentifier;
	}

}