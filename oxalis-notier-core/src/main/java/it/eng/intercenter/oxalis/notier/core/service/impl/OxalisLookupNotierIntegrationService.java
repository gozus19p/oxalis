package it.eng.intercenter.oxalis.notier.core.service.impl;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.inject.Inject;

import it.eng.intercenter.oxalis.integration.dto.OxalisLookupEndpoint;
import it.eng.intercenter.oxalis.integration.dto.OxalisLookupMetadata;
import it.eng.intercenter.oxalis.integration.dto.OxalisLookupResponse;
import it.eng.intercenter.oxalis.notier.core.service.api.IOxalisLookupNotierIntegrationService;
import lombok.extern.slf4j.Slf4j;
import no.difi.vefa.peppol.common.model.*;
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
    private LookupClient lookupClient;

    @Override
    public OxalisLookupResponse executeLookup(String participantIdentifierString) {
        return fetchResults(participantIdentifierString, null);
    }

    @Override
    public OxalisLookupResponse executeLookup(String participantIdentifierString, String documentTypeIdentifierString) {
        return fetchResults(participantIdentifierString, documentTypeIdentifierString);
    }

    /**
     * @param participantIdentifierString  is the given participant identifier to
     *                                     lookup for
     * @param documentTypeIdentifierString is the given document type identifier to
     *                                     match with participant id (optional)
     * @return the result
     * @author Manuel Gozzi
     * @date 30 ago 2019
     * @time 09:32:04
     */
    private OxalisLookupResponse fetchResults(String participantIdentifierString, String documentTypeIdentifierString) {

        // Prepare response DTO.
        OxalisLookupResponse lookupResponse = new OxalisLookupResponse();
        lookupResponse.setParticipantIdentifier(participantIdentifierString);
        List<OxalisLookupMetadata> lookupMetadataList = new ArrayList<>();

        try {

            // Parse String into PEPPOL object.
            ParticipantIdentifier participantIdentifier = ParticipantIdentifier.of(participantIdentifierString);
            log.debug("Parsed participant identifier String into {}", ParticipantIdentifier.class.getTypeName());

            DocumentTypeIdentifier givenDocumentTypeIdentifier = (documentTypeIdentifierString != null && !documentTypeIdentifierString.trim().isEmpty())
                    ? DocumentTypeIdentifier.of(documentTypeIdentifierString)
                    : null;
            if (givenDocumentTypeIdentifier != null) {
                log.debug("Parsed document type identifier String into {}", DocumentTypeIdentifier.class.getTypeName());
            }

            try {

                // Retrieve document type identifiers list from lookupClient.
                log.debug("Retrieve document type identifiers list");
                List<DocumentTypeIdentifier> documentTypeIdentifiers = lookupClient.getDocumentIdentifiers(participantIdentifier);

                // If user gives me a document type identifier I have to search for it as single
                // occurrence.
                if (givenDocumentTypeIdentifier != null) {

                    partialLookup(participantIdentifierString, documentTypeIdentifierString, lookupResponse,
                            lookupMetadataList, participantIdentifier, givenDocumentTypeIdentifier, documentTypeIdentifiers);

                } else {
                    // If user doesn't give me a single document type identifier I have to retrieve
                    // all occurrences.

                    // Build metadata for each document type identifier.
                    for (DocumentTypeIdentifier documentTypeIdentifier : documentTypeIdentifiers) {

                        // Logging.
                        log.debug("Get service metadata of participant identifier \"{}\" and document type identifier \"{}\"", participantIdentifier.toString(),
                                documentTypeIdentifier.toString());

                        // Retrieve service metadata for given participant identifier and document type
                        // identifier.
                        ServiceMetadata serviceMetadata = lookupClient.getServiceMetadata(participantIdentifier, documentTypeIdentifier);

                        // Add single metadata to DTO.
                        lookupMetadataList.addAll(buildMetadata(serviceMetadata));
                    }

                }

                // Set metadata.
                log.info("Lookup executed successfully");
                lookupResponse.setMetadata(lookupMetadataList);
                lookupResponse.setMessage("Lookup executed successfully");
                lookupResponse.setOutcome(true);

            } catch (LookupException | PeppolSecurityException e) {

                // Set error in response DTO.
                log.error("Something went wrong during lookup process. Cause: {}", e.getMessage(), e);
                lookupResponse.setMessage(e.getMessage());
                lookupResponse.setOutcome(false);

            }

        } catch (Exception e) {

            // Set error in response DTO.
            log.error("Something went wrong during lookup: {}", e.getMessage(), e);
            lookupResponse.setMessage(e.getMessage());
            lookupResponse.setOutcome(false);

        }

        return lookupResponse;
    }

    private void partialLookup(String participantIdentifierString, String documentTypeIdentifierString,
                               OxalisLookupResponse lookupResponse, List<OxalisLookupMetadata> lookupMetadataList,
                               ParticipantIdentifier participantIdentifier,
                               DocumentTypeIdentifier givenDocumentTypeIdentifier,
                               List<DocumentTypeIdentifier> documentTypeIdentifiers) {
        try {
            // Try to find a valid document type identifier.
            DocumentTypeIdentifier found = documentTypeIdentifiers.stream()
                    .filter(single -> single.toString().equals(givenDocumentTypeIdentifier.toString()))
                    .findFirst()
                    .orElse(null);

            // Retrieve service metadata.
            ServiceMetadata serviceMetadata = lookupClient.getServiceMetadata(participantIdentifier, found);

            // Add single metadata to DTO.
            lookupMetadataList.addAll(buildMetadata(serviceMetadata));

            // Set metadata
            if (lookupMetadataList.isEmpty()) {
                // Logging.
                log.warn("Metadata list is empty, no results found for participant identifier \"{}\" and document type identifier \"{}\"",
                        participantIdentifierString, documentTypeIdentifierString);

                // Setting outcome.
                lookupResponse.setOutcome(false);
                lookupResponse.setMessage(
                        String.format(
                                "Combination of participant identifier \"%s\" and document type identifier \"%s\" not found on SML",
                                participantIdentifierString,
                                documentTypeIdentifierString
                        )
                );
            }

        } catch (Exception e) {
            // Logging.
            log.error("Something went wrong during lookup: {}", e.getMessage(), e);

            // Setting outcome.
            lookupResponse.setOutcome(false);
            lookupResponse.setMessage(e.getMessage());
        }
    }

    /**
     * @param serviceMetadata is the Service Metadata retrieved by lookup
     * @return a single metadata that needs to be added to the main DTO
     * @author Manuel Gozzi
     * @date 29 ago 2019
     * @time 16:39:59
     */
    private List<OxalisLookupMetadata> buildMetadata(ServiceMetadata serviceMetadata) {

        if (serviceMetadata == null) {
            return new ArrayList<>();
        }


        List<OxalisLookupEndpoint> endpointList = getEndpointList(serviceMetadata);
        List<ProcessMetadata<Endpoint>> processMetadata = serviceMetadata.getProcesses();

        if (processMetadata == null || processMetadata.isEmpty()) {
            return new ArrayList<>();
        }

        List<OxalisLookupMetadata> oxalisLookupMetadataList = new ArrayList<>();

        for (ProcessMetadata<Endpoint> processMetadatum : processMetadata) {
            for (ProcessIdentifier processIdentifier : processMetadatum.getProcessIdentifier()) {
                OxalisLookupMetadata metadataDTO = new OxalisLookupMetadata();
                metadataDTO.setDocumentTypeIdentifier(serviceMetadata.getDocumentTypeIdentifier().toString());
                metadataDTO.setEndpoint(endpointList);
                metadataDTO.setParticipantIdentifier(serviceMetadata.getParticipantIdentifier().toString());
                metadataDTO.setProcessTypeIdentifier(processIdentifier.toString());
                oxalisLookupMetadataList.add(metadataDTO);
            }
        }

        return oxalisLookupMetadataList;
    }

    private List<OxalisLookupEndpoint> getEndpointList(ServiceMetadata serviceMetadata) {
        List<OxalisLookupEndpoint> endpoints = new ArrayList<>();

        // Build a metadata for each endpoint.
        for (ProcessMetadata<Endpoint> process : serviceMetadata.getProcesses()) {

            if (process.getEndpoints() != null && !(process.getEndpoints().isEmpty())) {

                for (Endpoint endpoint : process.getEndpoints()) {

                    // Build endpoint.
                    OxalisLookupEndpoint oxalisEndpoint = buildEndpoint(endpoint);

                    // Add the built andpoint to the whole list.
                    endpoints.add(oxalisEndpoint);
                }
            }
        }

        return endpoints;
    }

    private OxalisLookupEndpoint buildEndpoint(Endpoint endpoint) {

        log.debug("Build endpoint");
        OxalisLookupEndpoint oxalisEndpoint = new OxalisLookupEndpoint();

        // Set address (URI).
        if (endpoint.getAddress() != null) {
            oxalisEndpoint.setAddress(endpoint.getAddress().normalize().toString());
        }

        // Set certificate.
        if (endpoint.getCertificate() != null) {
            try {
                log.debug("Encoding X509Certificate into PEM String");
                oxalisEndpoint.setCertificate(parseX509CertificateIntoString(endpoint.getCertificate()));
                log.debug("Encoding process completed successfully");
            } catch (CertificateEncodingException e) {
                log.error("Unable to encode certificate during lookup. Cause: {}", e.getMessage(), e);
            }
        }

        // Set period.
        if (endpoint.getPeriod() != null) {
            oxalisEndpoint.setServiceActivationDate(endpoint.getPeriod().getFrom());
            oxalisEndpoint.setServiceExpirationDate(endpoint.getPeriod().getTo());
        }

        // Set transport profile.
        if (endpoint.getTransportProfile() != null) {
            oxalisEndpoint.setTransportProfile(endpoint.getTransportProfile().toString());
        }

        // TODO: where???
        oxalisEndpoint.setServiceDescription(null);
        oxalisEndpoint.setTechnicalContactUrl(null);
        oxalisEndpoint.setTechnicalInformationUrl(null);

        return oxalisEndpoint;
    }

    private String parseX509CertificateIntoString(X509Certificate certificate)
            throws CertificateEncodingException {
        return new StringBuilder("-----BEGIN CERTIFICATE-----")
                .append(Base64.getEncoder().encodeToString(certificate.getEncoded()))
                .append("-----END CERTIFICATE-----")
                .toString();
    }

}
