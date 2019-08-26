package it.eng.intercenter.oxalis.integration.dto;

import java.util.Date;

/**
 * @author Manuel Gozzi
 */
public class OxalisMessage {

	private String transmissionIdentifier;

	private PeppolDetails header;

	private Date date;

	private String receiptType;

	private byte[] receiptValue;

	// "AS2" or "AS4"
	private String transportProfile;

	private String digestMethod;

	private byte[] digestValue;

	private byte[] primaryReceipt;

	private String tag;

	public OxalisMessage(String transmissionIdentifier, PeppolDetails header, Date date, String receiptType, byte[] receiptValue, String transportProfile,
			String digestMethod, byte[] digestValue, byte[] primaryReceipt, String tag) {
		super();
		this.transmissionIdentifier = transmissionIdentifier;
		this.header = header;
		this.date = date;
		this.receiptType = receiptType;
		this.receiptValue = receiptValue;
		this.transportProfile = transportProfile;
		this.digestMethod = digestMethod;
		this.digestValue = digestValue;
		this.primaryReceipt = primaryReceipt;
		this.tag = tag;
	}

	public String getTransmissionIdentifier() {
		return transmissionIdentifier;
	}

	public PeppolDetails getHeader() {
		return header;
	}

	public Date getDate() {
		return date;
	}

	public String getReceiptType() {
		return receiptType;
	}

	public byte[] getReceiptValue() {
		return receiptValue;
	}

	public String getTransportProfile() {
		return transportProfile;
	}

	public String getDigestMethod() {
		return digestMethod;
	}

	public byte[] getDigestValue() {
		return digestValue;
	}

	public byte[] getPrimaryReceipt() {
		return primaryReceipt;
	}

	public String getTag() {
		return tag;
	}

}
