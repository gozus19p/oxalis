package it.eng.intercenter.oxalis.integration.dto;

import java.io.ByteArrayInputStream;
import java.util.Date;

import it.eng.intercenter.oxalis.integration.dto.util.GsonUtil;

/**
 * This class represents documents that need to be sent on Notier (inbound
 * flow).
 * 
 * @author Manuel Gozzi
 */
public class ReceivedDocument implements NotierDTO {

	private String fileName;
	private Date receivedAt;
	private ByteArrayInputStream payload;

	public ReceivedDocument(String fileName, Date receivedAt, ByteArrayInputStream payload) {
		this.fileName = fileName;
		this.receivedAt = receivedAt;
		this.payload = payload;
	}

	public String getFileName() {
		return fileName;
	}

	public Date getReceivedAt() {
		return receivedAt;
	}

	public ByteArrayInputStream getPayload() {
		return payload;
	}

	@Override
	public String toString() {
		return GsonUtil.getPrettyPrintedInstance().toJson(this);
	}
	
}
