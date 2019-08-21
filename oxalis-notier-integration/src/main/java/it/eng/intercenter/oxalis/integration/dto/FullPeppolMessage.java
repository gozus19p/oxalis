package it.eng.intercenter.oxalis.integration.dto;

import it.eng.intercenter.oxalis.integration.util.GsonUtil;

/**
 * @author Manuel Gozzi
 * @date 21 ago 2019
 * @time 14:41:22
 */
public class FullPeppolMessage extends PeppolMessage {

	private final String endpointAPUri;
	private final String endpointAPCertificate;
	private final String transportProfile;

	public FullPeppolMessage(final String endpointAPUri, final String endpointAPCertificate, final String transportProfile, final byte[] payload,
			final PeppolDetails header) {
		super(payload, header);
		this.endpointAPUri = endpointAPUri;
		this.endpointAPCertificate = endpointAPCertificate;
		this.transportProfile = transportProfile;
	}

	public String getEndpointAPUri() {
		return endpointAPUri;
	}

	public String getEndpointAPCertificate() {
		return endpointAPCertificate;
	}

	public String getTransportProfile() {
		return transportProfile;
	}

	@Override
	public String toString() {
		return GsonUtil.getPrettyPrintedInstance().toJson(this);
	}

}
