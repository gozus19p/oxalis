package it.eng.intercenter.oxalis.notier.core.service.api;

import java.security.cert.CertificateException;

import it.eng.intercenter.oxalis.integration.dto.FullPeppolMessage;
import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.outbound.TransmissionMessage;
import network.oxalis.api.outbound.TransmissionResponse;

/**
 * @author Manuel Gozzi
 * @date 21 ago 2019
 * @time 14:32:11
 */
public interface IOutboundService {

	void processOutboundFlow() throws Exception;

	OxalisMdn buildTransmissionAndSendOnPeppol(final String urn, final String peppolMessageJsonFormat)
			throws OxalisTransmissionException, OxalisContentException;

	TransmissionResponse send(final TransmissionMessage documento) throws OxalisTransmissionException;

	OxalisMdn sendFullPeppolMessageOnDemand(final FullPeppolMessage fullPeppolMessage)
			throws OxalisTransmissionException, OxalisContentException, CertificateException;

}
