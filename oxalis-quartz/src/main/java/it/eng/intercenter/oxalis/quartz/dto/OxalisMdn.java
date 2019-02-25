package it.eng.intercenter.oxalis.quartz.dto;

/**
 * Classe atta a incapsulare l'esito dell'invio su rete Peppol di un documento
 * da parte di Oxalis.
 * 
 * @author Manuel Gozzi
 * @date 25 feb 2019
 * @time 11:11:00
 */
public class OxalisMdn {

	private String documentUrn;
	private String errorMessage;
	private OxalisStatusEnum status;

	public OxalisMdn(String documentUrn, OxalisStatusEnum status, String errorMessage) {
		this.documentUrn = documentUrn;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	public String getDocumentUrn() {
		return documentUrn;
	}

	public OxalisStatusEnum getStatus() {
		return status;
	}

	public String getMessage() {
		return errorMessage;
	}

}
