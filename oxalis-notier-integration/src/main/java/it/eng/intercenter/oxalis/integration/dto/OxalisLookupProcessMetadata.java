package it.eng.intercenter.oxalis.integration.dto;

import java.util.List;

/**
 * @author Manuel Gozzi
 */
public class OxalisLookupProcessMetadata {

    private List<OxalisLookupEndpoint> endpoint;

    private List<String> processIdentifierType;

    public List<OxalisLookupEndpoint> getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(List<OxalisLookupEndpoint> endpoint) {
        this.endpoint = endpoint;
    }

    public List<String> getProcessIdentifierType() {
        return processIdentifierType;
    }

    public void setProcessIdentifierType(List<String> processIdentifierType) {
        this.processIdentifierType = processIdentifierType;
    }

}
