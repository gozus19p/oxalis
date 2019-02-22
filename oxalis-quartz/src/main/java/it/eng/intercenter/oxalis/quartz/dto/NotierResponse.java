package it.eng.intercenter.oxalis.quartz.dto;

import it.eng.intercenter.oxalis.quartz.dto.enumerator.NotierResponseStatusEnum;

/**
 * Classe atta ad incapsulare il risultato dell'invio del documento da Oxalis
 * verso rete Peppol.
 * 
 * @author Manuel Gozzi
 */
public class NotierResponse {

	private String urn;
	private NotierResponseStatusEnum status;

	public NotierResponse(String urn, boolean documentHasBeenSentSuccessfully) {
		this.urn = urn;
		this.status = documentHasBeenSentSuccessfully ? NotierResponseStatusEnum.OK
				: NotierResponseStatusEnum.KO;
	}

	public String getUrn() {
		return urn;
	}

	public NotierResponseStatusEnum getStatusEnum() {
		return status;
	}

	public String getStatusDescription() {
		return status.toString();
	}

	public String getStatusCode() {
		return status.name();
	}

}
