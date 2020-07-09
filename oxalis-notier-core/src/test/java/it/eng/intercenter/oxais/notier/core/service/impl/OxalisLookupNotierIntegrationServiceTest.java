package it.eng.intercenter.oxais.notier.core.service.impl;

import com.google.inject.Inject;
import it.eng.intercenter.oxalis.integration.dto.OxalisLookupResponse;
import it.eng.intercenter.oxalis.notier.core.service.impl.OxalisLookupNotierIntegrationService;
import net.lamberto.junit.GuiceJUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Manuel Gozzi
 */
@RunWith(GuiceJUnitRunner.class)
public class OxalisLookupNotierIntegrationServiceTest {

    @Inject
    private OxalisLookupNotierIntegrationService service;

    @Test
    public void testLookup() {

        // Declare existing test participant identifier String.
        String tc_participantIdentifier = "0201:testap";

        // Execute lookup.
        final OxalisLookupResponse oxalisLookupResponse = service.executeLookup(tc_participantIdentifier);

        // Process asserts.
        Assert.assertTrue(oxalisLookupResponse.getOutcome());

    }

}
