package it.eng.intercenter.oxalis.integration.dto.enumerator;

import it.eng.intercenter.oxalis.integration.api.NotierDTO;

/**
 * Enumeratore che conserva le possibili risposte da Oxalis.
 *
 * @author Manuel Gozzi
 * @date 25 feb 2019
 * @time 11:15:27
 */
public enum OxalisStatusEnum implements NotierDTO {

	OK("Success"), KO("Failed"), INTERNAL("Internal flow");

	private String message;

	private OxalisStatusEnum(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return message;
	}

}
