package it.eng.intercenter.oxalis.integration.dto;

import java.io.ByteArrayInputStream;

import it.eng.intercenter.oxalis.integration.api.NotierDTO;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;

/**
 * 
 * @author Manuel Gozzi
 * @date 21 feb 2019
 * @time 17:31:30
 */
public class PeppolMessage implements NotierDTO {
	
	private ByteArrayInputStream payload;
	private PeppolDetails header;

	public PeppolMessage(byte[] payload, PeppolDetails header) {
		this.payload = new ByteArrayInputStream(payload);
		this.header = header;
	}

	public ByteArrayInputStream getPayload() {
		return payload;
	}

	public PeppolDetails getHeader() {
		return header;
	}
	
	@Override
	public String toString() {
		return GsonUtil.getPrettyPrintedInstance().toJson(this);
	}

}
