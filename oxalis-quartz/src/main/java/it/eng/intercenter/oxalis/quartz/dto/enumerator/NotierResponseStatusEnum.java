package it.eng.intercenter.oxalis.quartz.dto.enumerator;

import java.util.Date;

/**
 * Enumeratore corrispondente allo status della risposta da inviare a Notier.
 * 
 * @author Manuel Gozzi
 */
public enum NotierResponseStatusEnum {

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
