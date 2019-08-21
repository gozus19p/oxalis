package it.eng.intercenter.oxalis.quartz.api;

import java.security.cert.CertificateException;

import org.quartz.JobExecutionException;

import it.eng.intercenter.oxalis.integration.dto.FullPeppolMessage;
import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.outbound.TransmissionResponse;

/**
 * @author Manuel Gozzi
 * @date 21 ago 2019
 * @time 14:32:11
 */
public interface IOutboundService {

	void processOutboundFlow() throws JobExecutionException;

	OxalisMdn buildTransmissionAndSendOnPeppol(final String urn, final String peppolMessageJsonFormat)
			throws OxalisTransmissionException, OxalisContentException;

	TransmissionResponse send(final TransmissionMessage documento) throws OxalisTransmissionException;

	OxalisMdn sendFullPeppolMessageOnDemand(final FullPeppolMessage fullPeppolMessage)
			throws OxalisTransmissionException, OxalisContentException, CertificateException;

}
