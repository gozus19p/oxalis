package it.eng.intercenter.oxalis.notier.rest.server.service;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.inject.Inject;

import it.eng.intercenter.oxalis.integration.dto.OxalisLookupEndpoint;
import it.eng.intercenter.oxalis.integration.dto.OxalisLookupMetadata;
import it.eng.intercenter.oxalis.integration.dto.OxalisLookupResponse;
import it.eng.intercenter.oxalis.notier.rest.server.service.api.IOxalisLookupNotierIntegrationService;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.vefa.peppol.common.model.DocumentTypeIdentifier;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.difi.vefa.peppol.common.model.ServiceMetadata;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.api.LookupException;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;

/**
 * @author Manuel Gozzi
 * @date 29 ago 2019
 * @time 16:34:49
 */
@Slf4j
public class OxalisLookupNotierIntegrationService implements IOxalisLookupNotierIntegrationService {

	@Inject
	LookupService lookupService;

	@Inject
	LookupClient lookupClient;

	@Override
	public OxalisLookupResponse executeLookup(String participantIdentifierString) {
		return fetchResults(participantIdentifierString, null);
	}

	@Override
	public OxalisLookupResponse executeLookup(String participantIdentifierString, String documentTypeIdentifierString) {
		return fetchResults(participantIdentifierString, documentTypeIdentifierString);
	}

	/**
	 * @author Manuel Gozzi
	 * @date 30 ago 2019
	 * @time 09:32:04
	 * @param participantIdentifierString  is the given participant identifier to
	 *                                     lookup for
	 * @param documentTypeIdentifierString is the given document type identifier to
	 *                                     match with participant id (optional)
	 * @return the result
	 */
	private OxalisLookupResponse fetchResults(String participantIdentifierString, String documentTypeIdentifierString) {

		// Parse String into PEPPOL object.
		ParticipantIdentifier participantIdentifier = ParticipantIdentifier.of(participantIdentifierString);
		log.debug("Parsed participant identifier String into {}", ParticipantIdentifier.class.getTypeName());

		DocumentTypeIdentifier givenDocumentTypeIdentifier = (documentTypeIdentifierString != null && !documentTypeIdentifierString.trim().isEmpty())
				? DocumentTypeIdentifier.of(documentTypeIdentifierString)
				: null;
		if (givenDocumentTypeIdentifier != null) {
			log.debug("Parsed document type identifier String into {}", DocumentTypeIdentifier.class.getTypeName());
		}

		// Prepare response DTO.
		OxalisLookupResponse lookupResponse = new OxalisLookupResponse();
		lookupResponse.setParticipantIdentifier(participantIdentifierString);
		List<OxalisLookupMetadata> lookupMetadataList = new ArrayList<>();

		try {

			// Retrieve document type identifiers list from lookupClient.
			log.debug("Retrieve document type identifiers list");
			List<DocumentTypeIdentifier> documentTypeIdentifiers = lookupClient.getDocumentIdentifiers(participantIdentifier);

			// If user gives me a document type identifier I have to search for it as single
			// occurrence.
			if (givenDocumentTypeIdentifier != null) {

				try {
					// Try to find a valid document type identifier.
					DocumentTypeIdentifier found = documentTypeIdentifiers.stream()
							.filter(single -> single.toString().equals(givenDocumentTypeIdentifier.toString())).findFirst().get();

					// Retrieve service metadata.
					ServiceMetadata serviceMetadata = lookupClient.getServiceMetadata(participantIdentifier, found);

					// Add single metadata to DTO.
					lookupMetadataList.add(buildMetadata(participantIdentifierString, getEndpointList(serviceMetadata), found.toString()));

					// Set metadata
					if (lookupMetadataList.isEmpty()) {
						// Logging.
						log.warn("Metadata list is empty, no results found for participant identifier \"{}\" and document type identifier \"{}\"",
								participantIdentifierString, documentTypeIdentifierString);

						// Setting outcome.
						lookupResponse.setOutcome(false);
						lookupResponse.setMessage((givenDocumentTypeIdentifier != null)
								? "Participant identifier \"" + participantIdentifierString + "\" and document type identifier \""
										+ documentTypeIdentifierString + "\" not registered on PEPPOL"
								: "No results found for participant identifier \"" + participantIdentifierString + "\"");
					}

				} catch (Exception e) {
					// Logging.
					log.error("Something went wrong during lookup: {}", e.getMessage(), e);

					// Setting outcome.
					lookupResponse.setOutcome(false);
					lookupResponse.setMessage(e.getMessage());
				}

			}

			// If user doesn't give me a single document type identifier I have to retrieve
			// all occurrences.
			else {

				// Build metadata for each document type identifier.
				for (DocumentTypeIdentifier documentTypeIdentifier : documentTypeIdentifiers) {

					// Logging.
					log.debug("Get service metadata of participant identifier \"{}\" and document type identifier \"{}\"", participantIdentifier.toString(),
							documentTypeIdentifier.toString());

					// Retrieve service metadata for given participant identifier and document type
					// identifier.
					ServiceMetadata serviceMetadata = lookupClient.getServiceMetadata(participantIdentifier, documentTypeIdentifier);

					// Add single metadata to DTO.
					lookupMetadataList.add(buildMetadata(participantIdentifierString, getEndpointList(serviceMetadata), documentTypeIdentifier.toString()));
				}

			}

		} catch (LookupException | PeppolSecurityException e) {

			// Set error in response DTO.
			log.error("Something went wrong during lookup process. Cause: {}", e.getMessage(), e);
			lookupResponse.setMessage(e.getMessage());
			lookupResponse.setOutcome(false);

		}

		// Set metadata.
		log.debug("Lookup executed successfully");
		lookupResponse.setMetadata(lookupMetadataList);
		lookupResponse.setMessage("Lookup executed successfully");

		return lookupResponse;
	}

	/**
	 * @author Manuel Gozzi
	 * @date 29 ago 2019
	 * @time 16:39:59
	 * @param participantIdentifier  is the participant identifier of lookup
	 * @param endpointList           is the list of endpoints
	 * @param documentTypeIdentifier is the document type identifier
	 * @return a single metadata that needs to be added to the main DTO
	 */
	private OxalisLookupMetadata buildMetadata(String participantIdentifier, List<OxalisLookupEndpoint> endpointList, String documentTypeIdentifier) {
		OxalisLookupMetadata metadataDTO = new OxalisLookupMetadata();
		metadataDTO.setDocumentTypeIdentifier(documentTypeIdentifier);
		metadataDTO.setEndpoint(endpointList);
		metadataDTO.setParticipantIdentifier(participantIdentifier);
		return metadataDTO;
	}

	private static final List<OxalisLookupEndpoint> getEndpointList(ServiceMetadata serviceMetadata) {
		List<OxalisLookupEndpoint> endpoints = new ArrayList<>();

		// Build a metadata for each endpoint.
		serviceMetadata.getProcesses().stream().forEach(process -> {

			process.getEndpoints().stream().forEach(endpoint -> {

				// Build endpoint.
				log.debug("Build endpoint");
				OxalisLookupEndpoint oxalisEndpoint = new OxalisLookupEndpoint();
				oxalisEndpoint.setAddress(endpoint.getAddress().normalize().toString());

				try {

					log.debug("Encoding X509Certificate into PEM String");
					oxalisEndpoint.setCertificate(parseX509CertificateIntoString(endpoint.getCertificate()));
					log.debug("Encoding process completed successfully");

				} catch (CertificateEncodingException e) {

					log.error("Unable to encode certificate during lookup. Cause: {}", e.getMessage(), e);

				}

				oxalisEndpoint.setServiceActivationDate(endpoint.getPeriod().getFrom());
				oxalisEndpoint.setServiceExpirationDate(endpoint.getPeriod().getTo());
				oxalisEndpoint.setTransportProfile(endpoint.getTransportProfile().toString());

				// TODO: where???
				oxalisEndpoint.setServiceDescription(null);
				// TODO: where???
				oxalisEndpoint.setTechnicalContactUrl(null);
				// TODO: where???
				oxalisEndpoint.setTechnicalInformationUrl(null);

				// Add the built andpoint to the whole list.
				endpoints.add(oxalisEndpoint);

			});

		});

		return endpoints;
	}

	private static final String parseX509CertificateIntoString(X509Certificate certificate) throws CertificateEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append("-----BEGIN CERTIFICATE-----");
		sb.append(Base64.getEncoder().encodeToString(certificate.getEncoded()));
		sb.append("-----END CERTIFICATE-----");
		return sb.toString();
	}

}
