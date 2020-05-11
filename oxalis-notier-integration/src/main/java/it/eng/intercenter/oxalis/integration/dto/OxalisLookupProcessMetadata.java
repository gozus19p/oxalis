package it.eng.intercenter.oxalis.integration.dto;

import java.util.List;

/**
 * @author Manuel Gozzi
 */
public class OxalisLookupProcessMetadata {

    private List<OxalisLookupEndpoint> endpoint;

    private List<PeppolIdentifier> processIdentifierType;

    public List<OxalisLookupEndpoint> getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(List<OxalisLookupEndpoint> endpoint) {
        this.endpoint = endpoint;
    }

    public List<PeppolIdentifier> getProcessIdentifierType() {
        return processIdentifierType;
    }

    public void setProcessIdentifierType(List<PeppolIdentifier> processIdentifierType) {
        this.processIdentifierType = processIdentifierType;
    }

}
