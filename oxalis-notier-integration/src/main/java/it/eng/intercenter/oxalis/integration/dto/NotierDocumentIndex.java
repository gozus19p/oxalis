package it.eng.intercenter.oxalis.integration.dto;

/**
 * @author Manuel Gozzi
 */
public class NotierDocumentIndex {
	
	private final String urn;
	private final boolean isInternal;
	
	public NotierDocumentIndex(String urn, boolean isInternal) {
		this.urn = urn;
		this.isInternal = isInternal;
	}
	
	public String getUrn() {
		return urn;
	}
	public boolean isInternal() {
		return isInternal;
	}

}
