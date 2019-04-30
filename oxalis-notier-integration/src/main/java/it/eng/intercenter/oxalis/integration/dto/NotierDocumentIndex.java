package it.eng.intercenter.oxalis.integration.dto;

import it.eng.intercenter.oxalis.integration.dto.util.GsonUtil;

/**
 * @author Manuel Gozzi
 */
public class NotierDocumentIndex implements NotierDTO {
	
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
	
	@Override
	public String toString() {
		return GsonUtil.getPrettyPrintedInstance().toJson(this);
	}

}
