package it.eng.intercenter.oxalis.integration.dto;

import java.util.List;

/**
 * @author Manuel Gozzi
 * @date 29 ago 2019
 * @time 16:11:31
 */
public class OxalisLookupResponse {

	private Boolean outcome;
	private String message;
	private List<OxalisLookupMetadata> metadata;

	public Boolean getOutcome() {
		return outcome;
	}

	public void setOutcome(Boolean outcome) {
		this.outcome = outcome;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<OxalisLookupMetadata> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<OxalisLookupMetadata> metadata) {
		this.metadata = metadata;
	}

}
