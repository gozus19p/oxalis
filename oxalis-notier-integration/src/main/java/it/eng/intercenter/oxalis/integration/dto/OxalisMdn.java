package it.eng.intercenter.oxalis.integration.dto;

import it.eng.intercenter.oxalis.integration.api.NotierDTO;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisStatusEnum;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;

/**
 * Classe atta a incapsulare l'esito dell'invio su rete Peppol di un documento
 * da parte di Oxalis.
 *
 * @author Manuel Gozzi
 * @date 25 feb 2019
 * @time 11:11:00
 */
public class OxalisMdn implements NotierDTO {

	private String documentUrn;
	private String errorMessage;
	private OxalisStatusEnum status;
	private TransactionDetails transactionDetails;

	public OxalisMdn(String documentUrn, OxalisStatusEnum status, String errorMessage) {
		this.documentUrn = documentUrn;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	public OxalisMdn(String documentUrn, OxalisStatusEnum status, String errorMessage, TransactionDetails transactionDetails) {
		this.documentUrn = documentUrn;
		this.status = status;
		this.errorMessage = errorMessage;
		this.setTransactionDetails(transactionDetails);
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

	public boolean hasPositiveStatus() {
		return OxalisStatusEnum.OK.equals(status);
	}

	public TransactionDetails getTransactionDetails() {
		return transactionDetails;
	}

	public void setTransactionDetails(TransactionDetails transactionDetails) {
		this.transactionDetails = transactionDetails;
	}

	@Override
	public String toString() {
		return GsonUtil.getPrettyPrintedInstance().toJson(this);
	}

}
