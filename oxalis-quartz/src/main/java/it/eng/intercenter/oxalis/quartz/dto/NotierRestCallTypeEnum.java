package it.eng.intercenter.oxalis.quartz.dto;

/**
 * 
 * @author Manuel Gozzi
 */
public enum NotierRestCallTypeEnum {

	POST,
	GET,
	PUT,
	DELETE;
	
	@Override
	public String toString() {
		return this.name();
	}
	
}
