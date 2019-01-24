package no.difi.oxalis.outbound.ws;

import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.api.lang.OxalisException;
import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.Transmitter;
import no.difi.oxalis.outbound.OxalisOutboundComponent;

/**
 * 
 * @author Manuel Gozzi
 *
 */
@Slf4j
public class OutboundClientWS {
	
	private OxalisOutboundComponent oxalisOutboundComponent;

	/*@RequestMapping(value = "/v1.0/outbound/send", method = RequestMethod.POST, consumes = "multipart/form-data", produces = "application/json")
	public @ResponseBody TransmissionResponse sendOutbound(@RequestPart("transmission-message") TransmissionMessage message) {
		log.info("Sending outbound to: " + message.getHeader().getReceiver().getIdentifier());
		Transmitter transmitter = oxalisOutboundComponent.getTransmitter();
		TransmissionResponse transmissionResponse;
		try {
			transmissionResponse = transmitter.transmit(message);
			log.info("Outbound has successfully been sent to: " + message.getHeader().getReceiver().getIdentifier());
			return transmissionResponse;
		} catch (OxalisException oex) {
			log.error("An error occurred while sending message: " + oex.getMessage());
		} catch (Exception nex) {
			log.error("An unexpected error occurred while sending message: " + nex.getMessage());
		}
		return transmissionResponse;		
	}*/
	
}
