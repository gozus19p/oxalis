package it.eng.intercenter.oxalis.integration.dto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 
 * @author Manuel Gozzi
 * @date 21 feb 2019
 * @time 17:31:30
 */
public class PeppolMessage {
	
	private InputStream payload;
	private PeppolDetails header;

	public PeppolMessage(byte[] payload, PeppolDetails header) {
		this.payload = new ByteArrayInputStream(payload);
		this.header = header;
	}

	public InputStream getPayload() {
		return payload;
	}

	public PeppolDetails getHeader() {
		return header;
	}

}
