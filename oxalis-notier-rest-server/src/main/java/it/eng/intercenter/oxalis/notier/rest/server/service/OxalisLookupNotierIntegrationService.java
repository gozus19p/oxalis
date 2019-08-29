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

		// Parse String into PEPPOL object.
		log.debug("Parse participant identifier String into {}", ParticipantIdentifier.class.getTypeName());
		ParticipantIdentifier participantIdentifier = ParticipantIdentifier.of(participantIdentifierString);

		// Prepare response DTO.
		OxalisLookupResponse lookupResponse = new OxalisLookupResponse();
		lookupResponse.setParticipantIdentifier(participantIdentifierString);
		List<OxalisLookupMetadata> lookupMetadataList = new ArrayList<>();

		try {

			// Retrieve document type identifiers list from lookupClient.
			log.debug("Retrieve document type identifiers list");
			List<DocumentTypeIdentifier> documentTypeIdentifiers = lookupClient.getDocumentIdentifiers(participantIdentifier);

			// Build metadata for each document type identifier.
			for (DocumentTypeIdentifier documentTypeIdentifier : documentTypeIdentifiers) {

				log.debug("Get service metadata of participant identifier \"{}\" and document type identifier \"{}\"", participantIdentifier.toString(),
						documentTypeIdentifier.toString());
				ServiceMetadata serviceMetadata = lookupClient.getServiceMetadata(participantIdentifier, documentTypeIdentifier);

				// Add single metadata to DTO.
				lookupMetadataList.add(buildMetadata(participantIdentifierString, getEndpointList(serviceMetadata), documentTypeIdentifier.toString()));

			}

			// Set metadata.
			log.debug("Lookup executed successfully");
			lookupResponse.setMetadata(lookupMetadataList);
			lookupResponse.setOutcome(true);
			lookupResponse.setMessage("Lookup executed successfully");

		} catch (LookupException | PeppolSecurityException e) {

			// Set error in response DTO.
			log.error("Something went wrong during lookup process. Cause: {}", e.getMessage(), e);
			lookupResponse.setMessage(e.getMessage());
			lookupResponse.setOutcome(false);

		}

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
