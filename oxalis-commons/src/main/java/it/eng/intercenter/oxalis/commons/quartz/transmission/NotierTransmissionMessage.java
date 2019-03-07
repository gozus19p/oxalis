package it.eng.intercenter.oxalis.commons.quartz.transmission;

import java.io.InputStream;
import java.io.Serializable;

import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.tag.Tag;
import no.difi.vefa.peppol.common.model.Header;

/**
 * Implementazione custom del TransmissionMessage, copiata dal
 * DefaultTransmissionMessage.
 * 
 * @author Manuel Gozzi
 */
public class NotierTransmissionMessage implements TransmissionMessage, Serializable {

	private static final long serialVersionUID = -1997L;

	private final Tag tag;

	private final Header header;

	private final InputStream payload;

	public NotierTransmissionMessage(Header header, InputStream payload, Tag tag) {
		this.tag = tag;
		this.header = header;
		this.payload = payload;
	}

	@Override
	public Tag getTag() {
		return tag;
	}

	@Override
	public Header getHeader() {
		return header;
	}

	@Override
	public InputStream getPayload() {
		return payload;
	}

}
