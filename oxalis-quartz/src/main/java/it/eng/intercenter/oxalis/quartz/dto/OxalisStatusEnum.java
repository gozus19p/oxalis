package it.eng.intercenter.oxalis.quartz.dto;

/**
 * Enumeratore che conserva le possibili risposte da Oxalis.
 * 
 * @author Manuel Gozzi
 * @date 25 feb 2019
 * @time 11:15:27
 */
public enum OxalisStatusEnum {

	OK("Success"), KO("Failed");

	private String message;

	private OxalisStatusEnum(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return message;
	}
	
}
