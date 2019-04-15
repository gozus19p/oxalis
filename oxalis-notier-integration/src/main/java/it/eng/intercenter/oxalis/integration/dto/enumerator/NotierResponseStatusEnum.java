package it.eng.intercenter.oxalis.integration.dto.enumerator;

import java.util.Date;

import it.eng.intercenter.oxalis.integration.dto.NotierDTO;

/**
 * Enumeratore corrispondente allo status della risposta da inviare a Notier.
 * 
 * @author Manuel Gozzi
 */
public enum NotierResponseStatusEnum implements NotierDTO {

	OK("Process completed successfully"), KO("The document has not been sent successfully");

	private String message;
	private Date date;

	private NotierResponseStatusEnum(String message) {
		this.message = message;
		this.date = new Date();
	}

	@Override
	public String toString() {
		return this.message;
	}
	
	public Date getReceiptDate() {
		return this.date;
	}

}
