package it.eng.intercenter.oxalis.integration.dto;

import java.util.List;

/**
 * @author Manuel Gozzi
 */
public class UrnList {

	private final List<NotierDocumentIndex> documents;
	private final int urnCount;

	public UrnList(List<NotierDocumentIndex> urns) {
		this.documents = urns;
		if (urns != null) {
			urnCount = urns.size();
		} else {
			urnCount = 0;
		}
	}

	public List<NotierDocumentIndex> getDocuments() {
		return documents;
	}

	public int getUrnCount() {
		return urnCount;
	}

}
