package it.eng.intercenter.oxalis.integration.dto;

import java.io.ByteArrayInputStream;
import java.util.Date;

/**
 * @author Manuel Gozzi
 * @date 12 feb 2020
 * @time 10:21:00
 */
public class TransactionDetails {

	private String endpointUri;
	private String transmissionIdentifier;
	private Date timestamp;
	private ByteArrayInputStream receipt;
	private String transportProfile;

	public String getEndpointUri() {
		return endpointUri;
	}

	public void setEndpointUri(String endpointUri) {
		this.endpointUri = endpointUri;
	}

	public String getTransmissionIdentifier() {
		return transmissionIdentifier;
	}

	public void setTransmissionIdentifier(String transmissionIdentifier) {
		this.transmissionIdentifier = transmissionIdentifier;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public ByteArrayInputStream getReceipt() {
		return receipt;
	}

	public void setReceipt(ByteArrayInputStream receipt) {
		this.receipt = receipt;
	}

	public String getTransportProfile() {
		return transportProfile;
	}

	public void setTransportProfile(String transportProfile) {
		this.transportProfile = transportProfile;
	}

}
