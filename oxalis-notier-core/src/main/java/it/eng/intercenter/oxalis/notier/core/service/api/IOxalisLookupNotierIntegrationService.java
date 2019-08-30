package it.eng.intercenter.oxalis.notier.core.service.api;

import it.eng.intercenter.oxalis.integration.dto.OxalisLookupResponse;

/**
 * @author Manuel Gozzi
 * @date 29 ago 2019
 * @time 16:34:09
 */
public interface IOxalisLookupNotierIntegrationService {

	/**
	 * Execute the whole lookup with a given participant identifier (String).
	 *
	 * @author Manuel Gozzi
	 * @date 29 ago 2019
	 * @time 16:39:07
	 * @param participantIdentifierString is the given participant that user needs
	 *                                    to lookup for
	 * @return the lookup result with PEPPOL details
	 */
	OxalisLookupResponse executeLookup(String participantIdentifierString);

	/**
	 * Execute the lookup with a given participant identifier (String) and a
	 * document type identifier (String).
	 *
	 * @author Manuel Gozzi
	 * @date 29 ago 2019
	 * @time 16:39:07
	 * @param participantIdentifierString  is the given participant that user needs
	 *                                     to lookup for
	 * @param documentTypeIdentifierString is the given document type identifier
	 *                                     that user needs to lookup for
	 * @return the lookup result with PEPPOL details
	 */
	OxalisLookupResponse executeLookup(String participantIdentifierString, String documentTypeIdentifierString);

}
