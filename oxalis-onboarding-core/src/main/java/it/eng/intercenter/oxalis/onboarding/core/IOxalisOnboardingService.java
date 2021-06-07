package it.eng.intercenter.oxalis.onboarding.core;

import network.oxalis.api.lang.OxalisContentException;
import network.oxalis.api.lang.OxalisTransmissionException;

import java.io.IOException;

/**
 * @author Manuel Gozzi
 */
public interface IOxalisOnboardingService {

	byte[] sendOutbound(String message) throws IOException, OxalisContentException, OxalisTransmissionException;
}
