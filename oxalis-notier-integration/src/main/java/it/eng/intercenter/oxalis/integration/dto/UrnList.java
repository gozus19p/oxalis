package it.eng.intercenter.oxalis.integration.dto;

import java.util.ArrayList;
import java.util.List;

import it.eng.intercenter.oxalis.integration.api.NotierDTO;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;

/**
 * @author Manuel Gozzi
 */
public class UrnList implements NotierDTO {

	private final List<NotierDocumentIndex> documents;
	private final int urnCount;

	public UrnList(List<NotierDocumentIndex> urns) {
		if (urns != null) {
			this.documents = urns;
			urnCount = urns.size();
		} else {
			this.documents = new ArrayList<NotierDocumentIndex>();
			urnCount = 0;
		}
	}

	public List<NotierDocumentIndex> getDocuments() {
		return documents;
	}

	public int getUrnCount() {
		return urnCount;
	}

	@Override
	public String toString() {
		return GsonUtil.getPrettyPrintedInstance().toJson(this);
	}
	
}
