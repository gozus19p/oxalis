package it.eng.intercenter.oxalis.onboarding.core;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.lang.OxalisTransmissionException;
import network.oxalis.api.outbound.TransmissionRequest;
import network.oxalis.api.outbound.TransmissionResponse;
import network.oxalis.outbound.OxalisOutboundComponent;
import network.oxalis.outbound.transmission.TransmissionRequestBuilder;
import network.oxalis.vefa.peppol.common.model.Receipt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class OxalisOnboardingService implements IOxalisOnboardingService {

	private final TransmissionRequestBuilder transmissionRequestBuilder;
	private final OxalisOutboundComponent outboundComponent;

	@Inject
	public OxalisOnboardingService(TransmissionRequestBuilder transmissionRequestBuilder, OxalisOutboundComponent outboundComponent) {
		this.transmissionRequestBuilder = transmissionRequestBuilder;
		this.outboundComponent = outboundComponent;
	}

	@Override
	public byte[] sendOutbound(String message) throws IOException, OxalisContentException, OxalisTransmissionException {

		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8))) {

			log.info("Building request...");
			TransmissionRequest request = this.transmissionRequestBuilder.payLoad(byteArrayInputStream).build();
			log.info("Transmitting...");
			TransmissionResponse response = outboundComponent.getTransmitter().transmit(request);

			return response.getReceipts().stream()
					.map(Receipt::getValue)
					.findFirst()
					.orElse(null);
		}
	}
}
