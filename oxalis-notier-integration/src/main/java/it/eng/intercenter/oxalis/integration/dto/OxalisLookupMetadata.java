package it.eng.intercenter.oxalis.integration.dto;

import java.util.List;

/**
 * @author Manuel Gozzi
 * @date 29 ago 2019
 * @time 16:12:42
 */
public class OxalisLookupMetadata {

	private List<OxalisLookupEndpoint> endpoint;
	private String participantIdentifier;
	private String documentTypeIdentifier;
	private List<String> processTypeIdentifier;

	public List<OxalisLookupEndpoint> getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(List<OxalisLookupEndpoint> endpoint) {
		this.endpoint = endpoint;
	}

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

	public List<String> getProcessTypeIdentifier() {
		return processTypeIdentifier;
	}

	public void setProcessTypeIdentifier(List<String> processTypeIdentifier) {
		this.processTypeIdentifier = processTypeIdentifier;
	}

}
