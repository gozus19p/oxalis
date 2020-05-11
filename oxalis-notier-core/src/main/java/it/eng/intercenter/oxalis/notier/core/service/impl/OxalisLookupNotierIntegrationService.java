package it.eng.intercenter.oxalis.notier.core.service.impl;

import com.google.inject.Inject;
import it.eng.intercenter.oxalis.integration.dto.OxalisLookupEndpoint;
import it.eng.intercenter.oxalis.integration.dto.OxalisLookupMetadata;
import it.eng.intercenter.oxalis.integration.dto.OxalisLookupProcessMetadata;
import it.eng.intercenter.oxalis.integration.dto.OxalisLookupResponse;
import it.eng.intercenter.oxalis.notier.core.service.api.IOxalisLookupNotierIntegrationService;
import lombok.extern.slf4j.Slf4j;
import no.difi.vefa.peppol.common.lang.PeppolException;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.api.LookupException;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * @author Manuel Gozzi
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

        try {

            // Parse String into PEPPOL object.
            ParticipantIdentifier participantIdentifier = (participantIdentifierString != null && !participantIdentifierString.isEmpty()) ?
                    ParticipantIdentifier.of(participantIdentifierString)
                    : null;
            checkPartiticipantIdentifier(participantIdentifierString, participantIdentifier);
            log.info("Parsed participant identifier String into {}", ParticipantIdentifier.class.getSimpleName());

            DocumentTypeIdentifier givenDocumentTypeIdentifier = (documentTypeIdentifierString != null && !documentTypeIdentifierString.trim().isEmpty()) ?
                    DocumentTypeIdentifier.of(documentTypeIdentifierString)
                    : null;
            log.info("Parsed document type identifier String into {}", DocumentTypeIdentifier.class.getSimpleName());

            log.info("Starting lookup process");
            executeLookupAndSaveResultInDto(lookupResponse, participantIdentifier, givenDocumentTypeIdentifier);
        } catch (Exception e) {

            // Set error in response DTO.
            log.error("Something went wrong during lookup: {}", e.getMessage(), e);
            setNegativeOutcome(lookupResponse, e);
        }

        return lookupResponse;
    }

    /**
     * This one execute the whole lookup process, handling possible Exception clauses.
     *
     * @param lookupResponse              is the DTO that wraps the lookup result
     * @param participantIdentifier       is the given participant identifier that Oxalis needs to lookup for
     * @param givenDocumentTypeIdentifier is the specific document type identifier to search for (optional)
     */
    private void executeLookupAndSaveResultInDto(OxalisLookupResponse lookupResponse, ParticipantIdentifier participantIdentifier,
                                                 DocumentTypeIdentifier givenDocumentTypeIdentifier) {
        try {

            // If user gives me a document type identifier I have to search for it as single
            // occurrence.
            if (givenDocumentTypeIdentifier != null) {
                lookupForSingleDocumentTypeIdentifier(lookupResponse, participantIdentifier, givenDocumentTypeIdentifier);
            } else {
                lookupForAllDocumentTypeIdentifiersAvailable(lookupResponse, participantIdentifier);
            }
            log.info("Lookup executed successfully");

        } catch (LookupException | PeppolSecurityException e) {

            // Set error in response DTO.
            log.error("Something went wrong during lookup execution. Cause: {}", e.getMessage(), e);
            setNegativeOutcome(lookupResponse, e);
        }
    }

    /**
     * This one check the null condition on parsed ParticipantIdentifier.
     *
     * @param participantIdentifierString is the String object received from NoTI-ER
     * @param participantIdentifier       is the ParticipantIdentifier as PEPPOL DTO
     * @throws PeppolException if ParticipantIdentifier, intended as PEPPOL DTO, is null
     */
    private void checkPartiticipantIdentifier(String participantIdentifierString, ParticipantIdentifier participantIdentifier) throws PeppolException {
        if (participantIdentifier == null) {
            log.error("An invalid participant identifier has been provided: \"{}\"", participantIdentifierString);
            throw new PeppolException(
                    String.format("Invalid participant identifier provided. \"%s\" is not a valid value", participantIdentifierString)
            );
        }
    }

    /**
     * This one sets a negative outcome status on OxalisLookupResponse.
     *
     * @param lookupResponse is the response that Oxalis needs to send to NoTI-ER
     * @param e              is the Exception that has been thrown
     */
    private void setNegativeOutcome(OxalisLookupResponse lookupResponse, Exception e) {
        lookupResponse.setMessage(e.getMessage());
        lookupResponse.setOutcome(false);
    }

    /**
     * This one executes the single lookup for a given ParticipantIdentifier and DocumentTypeIdentifier.
     *
     * @param lookupResponse              is the response that Oxalis needs to send to NoTI-ER
     * @param participantIdentifier       is the ParticipantIdentifier that Oxalis needs to lookup for
     * @param givenDocumentTypeIdentifier is the DocumentTypeIdentifier that Oxalis needs to lookup for
     * @throws LookupException         if ParticipantIdentifier has not been found
     * @throws PeppolSecurityException if something goes wrong due to security issues with SML
     */
    private void lookupForSingleDocumentTypeIdentifier(OxalisLookupResponse lookupResponse, ParticipantIdentifier participantIdentifier,
                                                       DocumentTypeIdentifier givenDocumentTypeIdentifier)
            throws LookupException, PeppolSecurityException {

        ServiceMetadata serviceMetadata = lookupClient.getServiceMetadata(participantIdentifier, givenDocumentTypeIdentifier);
        OxalisLookupMetadata oxalisLookupMetadata = getOxalisLookupMetadata(
                participantIdentifier.toString(), givenDocumentTypeIdentifier.toString(), serviceMetadata
        );
        lookupResponse.setMetadata(Collections.singletonList(oxalisLookupMetadata));
        setPositiveOutcome(lookupResponse);
    }

    /**
     * This one sets a positive outcome status on OxalisLookupResponse.
     *
     * @param lookupResponse is the response that Oxalis needs to send to NoTI-ER
     */
    private void setPositiveOutcome(OxalisLookupResponse lookupResponse) {
        lookupResponse.setOutcome(true);
    }

    /**
     * This one executes the whole lookup process, without taking care of particular DocumentTypeIdentifier.
     *
     * @param lookupResponse        is the response that Oxalis needs to send to NoTI-ER
     * @param participantIdentifier is the ParticipantIdentifier that Oxalis needs to lookup for
     * @throws LookupException         if ParticipantIdentifier has not been found on SML (or similar)
     * @throws PeppolSecurityException if something goes wrong due to security issues with SML
     */
    private void lookupForAllDocumentTypeIdentifiersAvailable(OxalisLookupResponse lookupResponse,
                                                              ParticipantIdentifier participantIdentifier) throws LookupException, PeppolSecurityException {
        // If user doesn't give me a single document type identifier I have to retrieve
        // all occurrences.
        log.info("Retrieve document type identifiers list");
        List<DocumentTypeIdentifier> documentTypeIdentifiers = lookupClient.getDocumentIdentifiers(participantIdentifier);

        List<OxalisLookupMetadata> oxalisLookupMetadataList = new ArrayList<>();

        // Build metadata for each document type identifier.
        for (DocumentTypeIdentifier documentTypeIdentifier : documentTypeIdentifiers) {

            // Logging.
            log.info(
                    "Get service metadata of participant identifier \"{}\" and document type identifier \"{}\"",
                    participantIdentifier.toString(),
                    documentTypeIdentifier.toString()
            );

            // Retrieve service metadata for given participant identifier and document type
            // identifier.
            ServiceMetadata serviceMetadata = lookupClient.getServiceMetadata(participantIdentifier, documentTypeIdentifier);

            // Add single metadata to DTO.
            if (serviceMetadata != null) {
                oxalisLookupMetadataList.add(
                        getOxalisLookupMetadata(
                                participantIdentifier.toString(),
                                documentTypeIdentifier.toString(),
                                serviceMetadata
                        )
                );
            }
        }

        lookupResponse.setMetadata(oxalisLookupMetadataList);
        setPositiveOutcome(lookupResponse);
    }

    private OxalisLookupMetadata getOxalisLookupMetadata(String participantIdentifierString, String documentTypeIdentifierString,
                                                         ServiceMetadata serviceMetadata) {

        // Logging.
        log.info("Starting to build \"{}\" object", OxalisLookupMetadata.class.getSimpleName());

        OxalisLookupMetadata oxalisLookupMetadata = new OxalisLookupMetadata();
        oxalisLookupMetadata.setDocumentTypeIdentifier(documentTypeIdentifierString);
        oxalisLookupMetadata.setParticipantIdentifier(participantIdentifierString);

        List<OxalisLookupProcessMetadata> oxalisLookupProcessMetadataList = new ArrayList<>();
        if (serviceMetadata.getProcesses() != null && !serviceMetadata.getProcesses().isEmpty()) {
            log.info("Starting \"{}\" parsing", serviceMetadata.getProcesses().getClass().getSimpleName());
            for (ProcessMetadata<Endpoint> current : serviceMetadata.getProcesses()) {
                OxalisLookupProcessMetadata oxalisLookupProcessMetadata = new OxalisLookupProcessMetadata();
                List<OxalisLookupEndpoint> oxalisLookupEndpointList = new ArrayList<>();
                for (Endpoint endpoint : current.getEndpoints()) {
                    oxalisLookupEndpointList.add(buildEndpoint(endpoint));
                }
                oxalisLookupProcessMetadata.setEndpoint(oxalisLookupEndpointList);

                List<String> processTypeIdentifierList = new ArrayList<>();
                for (ProcessIdentifier processIdentifier : current.getProcessIdentifier()) {
                    processTypeIdentifierList.add(processIdentifier.toString());
                }

                oxalisLookupProcessMetadata.setProcessIdentifierType(processTypeIdentifierList);
                oxalisLookupProcessMetadataList.add(oxalisLookupProcessMetadata);
            }
            oxalisLookupMetadata.setProcessMetadata(oxalisLookupProcessMetadataList);
        } else {
            log.warn("\"{}\" list is null or empty", ProcessMetadata.class.getSimpleName());
        }
        return oxalisLookupMetadata;
    }

    private OxalisLookupEndpoint buildEndpoint(Endpoint endpoint) {

        log.info("Starting to parse single PEPPOL DTO endpoint");
        OxalisLookupEndpoint oxalisEndpoint = new OxalisLookupEndpoint();

        // Set address (URI).
        if (endpoint.getAddress() != null) {
            oxalisEndpoint.setEndpointUrl(endpoint.getAddress().normalize().toString());
        }

        // Set certificate.
        if (endpoint.getCertificate() != null) {
            try {
                log.info("Encoding X509Certificate into PEM String");
                oxalisEndpoint.setCertificate(parseX509CertificateIntoString(endpoint.getCertificate()));
                log.info("Encoding process completed successfully");
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

        // TODO: missing ServiceDescription, ContactURL, InformationURL
        return oxalisEndpoint;
    }

    /**
     * This one parses a X509 certificate into String.
     *
     * @param certificate is the given X509Certificate
     * @return the String that represents the X509Certificate
     * @throws CertificateEncodingException if something goes wrong during encoding process
     */
    private String parseX509CertificateIntoString(X509Certificate certificate)
            throws CertificateEncodingException {
        if (certificate == null) {
            log.warn("Given \"{}\" certificate is null, returning an empty String", X509Certificate.class.getSimpleName());
            return "";
        }
        return "-----BEGIN CERTIFICATE-----" +
                Base64.getEncoder().encodeToString(certificate.getEncoded()) +
                "-----END CERTIFICATE-----";
    }

}
