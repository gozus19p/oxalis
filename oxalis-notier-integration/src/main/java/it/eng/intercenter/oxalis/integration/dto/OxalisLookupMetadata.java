package it.eng.intercenter.oxalis.integration.dto;

import java.util.List;

/**
 * @author Manuel Gozzi
 * @date 29 ago 2019
 * @time 16:12:42
 */
public class OxalisLookupMetadata {

	private String participantIdentifier;

	private String documentTypeIdentifier;

	private List<OxalisLookupProcessMetadata> processMetadata;

	public String getParticipantIdentifier() {
		return participantIdentifier;
	}

	public void setParticipantIdentifier(String participantIdentifier) {
		this.participantIdentifier = participantIdentifier;
	}

	public String getDocumentTypeIdentifier() {
		return documentTypeIdentifier;
	}

	public void setDocumentTypeIdentifier(String documentTypeIdentifier) {
		this.documentTypeIdentifier = documentTypeIdentifier;
	}

	public List<OxalisLookupProcessMetadata> getProcessMetadata() {
		return processMetadata;
	}

	public void setProcessMetadata(List<OxalisLookupProcessMetadata> processMetadata) {
		this.processMetadata = processMetadata;
	}

}
