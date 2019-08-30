package it.eng.intercenter.oxalis.notier.rest.server.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.integration.dto.OxalisLookupResponse;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import it.eng.intercenter.oxalis.notier.rest.server.service.api.IOxalisLookupNotierIntegrationService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 * @date 29 ago 2019
 * @time 15:58:45
 */
@Slf4j
@Singleton
public class LookupServlet extends HttpServlet {

	private static final long serialVersionUID = 5586688022899771024L;

	// Participant identifier HTTP header key.
	private static final String HEADER_PARTICIPANT_ID_KEY = "participant-identifier";

	// Document type identifier HTTP header key.
	private static final String HEADER_DOCUMENT_TYPE_ID_KEY = "document-type-identifier";

	@Inject
	IOxalisLookupNotierIntegrationService lookupService;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		log.info("Received request in order to do perform a lookup");

		// Retrieve participant identifier from HTTP request (mandatory).
		String participantIdentifierString = request.getHeader(HEADER_PARTICIPANT_ID_KEY);

		// Retrieve document type identifier from HTTP request (optional).
		String documentTypeIdentifierString = request.getHeader(HEADER_DOCUMENT_TYPE_ID_KEY);

		// Check if HTTP call is made correctly, give an HTTP 400 otherwise.
		if (participantIdentifierString == null || participantIdentifierString.trim().isEmpty()) {

			log.warn("Missing mandatory header \"{}\" in HTTP GET, giving HTTP status 400", HEADER_PARTICIPANT_ID_KEY);
			response.setStatus(400);

		} else {

			OxalisLookupResponse lookupResponse;

			if (documentTypeIdentifierString == null || documentTypeIdentifierString.isEmpty()) {
				// Logging.
				log.info("Preparing lookup for participant identifier \"{}\"", participantIdentifierString);

				// Execute lookup.
				lookupResponse = lookupService.executeLookup(participantIdentifierString);
				log.info("Lookup outcome is: {}, {}", lookupResponse.getOutcome() ? "OK" : "KO", lookupResponse.getMessage());

			} else {
				// Logging.
				log.info("Preparing lookup for participant identifier \"{}\" and document type identifier \"{}\"",
						new Object[] { participantIdentifierString, documentTypeIdentifierString });

				// Execute lookup.
				lookupResponse = lookupService.executeLookup(participantIdentifierString, documentTypeIdentifierString);
				log.info("Lookup outcome is: {}, {}", lookupResponse.getOutcome() ? "OK" : "KO", lookupResponse.getMessage());

			}

			// Set HTTP content type.
			response.setContentType("application/json");

			// Write result into HTTP response.
			response.getWriter().write(GsonUtil.getPrettyPrintedInstance().toJson(lookupResponse));

			// Set HTTP 200 status.
			response.setStatus(200);

		}

	}

}
