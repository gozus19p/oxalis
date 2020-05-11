package it.eng.intercenter.oxalis.integration.dto;

import java.util.List;

/**
 * @author Manuel Gozzi
 * @date 29 ago 2019
 * @time 16:12:42
 */
public class OxalisLookupMetadata {

	private PeppolIdentifier participantIdentifier;

	private PeppolIdentifier documentTypeIdentifier;

	private List<OxalisLookupProcessMetadata> processMetadata;

	public PeppolIdentifier getParticipantIdentifier() {
		return participantIdentifier;
	}

	public void setParticipantIdentifier(PeppolIdentifier participantIdentifier) {
		this.participantIdentifier = participantIdentifier;
	}

	public PeppolIdentifier getDocumentTypeIdentifier() {
		return documentTypeIdentifier;
	}

	public void setDocumentTypeIdentifier(PeppolIdentifier documentTypeIdentifier) {
		this.documentTypeIdentifier = documentTypeIdentifier;
	}

	public List<OxalisLookupProcessMetadata> getProcessMetadata() {
		return processMetadata;
	}

	public void setProcessMetadata(List<OxalisLookupProcessMetadata> processMetadata) {
		this.processMetadata = processMetadata;
	}

}
