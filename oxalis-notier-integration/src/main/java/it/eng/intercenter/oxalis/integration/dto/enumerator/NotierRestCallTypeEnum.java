package it.eng.intercenter.oxalis.integration.dto.enumerator;

import it.eng.intercenter.oxalis.integration.dto.NotierDTO;

/**
 * 
 * @author Manuel Gozzi
 */
public enum NotierRestCallTypeEnum implements NotierDTO {

	POST,
	GET,
	PUT,
	DELETE;
	
	@Override
	public String toString() {
		return this.name();
	}
	
}
