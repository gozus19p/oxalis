/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.statistics.jdbc;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.h2.tools.RunScript;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import no.difi.oxalis.api.model.AccessPointIdentifier;
import no.difi.oxalis.persistence.annotation.Transactional;
import no.difi.oxalis.persistence.platform.MySQLPlatform;
import no.difi.oxalis.persistence.platform.PlatformModule;
import no.difi.oxalis.persistence.testng.PersistenceModuleFactory;
import no.difi.oxalis.statistics.api.ChannelId;
import no.difi.oxalis.statistics.api.RawStatisticsRepository;
import no.difi.oxalis.statistics.api.StatisticsGranularity;
import no.difi.oxalis.statistics.guice.RawStatisticsRepositoryModule;
import no.difi.oxalis.statistics.model.DefaultRawStatistics;
import no.difi.oxalis.test.identifier.PeppolDocumentTypeIdAcronym;
import no.difi.oxalis.test.identifier.PeppolProcessTypeIdAcronym;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;

/**
 * Tests the MySQL implementation
 *
 * @author steinar
 *         Date: 26.03.13
 *         Time: 10:38
 * @author erlend
 */
@Guice(moduleFactory = PersistenceModuleFactory.class,
        modules = {RawStatisticsRepositoryModule.class, PlatformModule.class})
public class RawStatisticsRepositoryMySqlMockTest {

    @Inject
    @Named(MySQLPlatform.IDENTIFIER)
    private RawStatisticsRepository repository;

    @Inject
    private DataSource dataSource;

    @BeforeClass
    @Transactional
    public void beforeClass() throws Exception {
        RunScript.execute(dataSource.getConnection(), new InputStreamReader(
                getClass().getResourceAsStream(PersistenceModuleFactory.CREATE_OXALIS_DBMS_H2_SQL),
                StandardCharsets.UTF_8));
    }

    @Test
    public void testPersist() throws Exception {

        assertTrue(repository instanceof RawStatisticsRepositoryMySqlImpl);

        DefaultRawStatistics rawStatistics = new DefaultRawStatistics.RawStatisticsBuilder()
                .accessPointIdentifier(new AccessPointIdentifier("AP_SendRegning"))
                .outbound()
                .sender(ParticipantIdentifier.of("9908:810017902"))
                .receiver(ParticipantIdentifier.of("9908:810017902"))
                .channel(new ChannelId("CH01"))
                .documentType(PeppolDocumentTypeIdAcronym.INVOICE.toVefa())
                .profile(PeppolProcessTypeIdAcronym.INVOICE_ONLY.toVefa())
                .build();
        repository.persist(rawStatistics);
    }

    @Test
    public void testMySqlDateFormatYear() throws Exception {
        String s = RawStatisticsRepositoryMySqlImpl.mySqlDateFormat(StatisticsGranularity.YEAR);
        assertEquals(s, "%Y");
    }

    @Test
    public void testMySqlDateFormatMonth() throws Exception {
        String s = RawStatisticsRepositoryMySqlImpl.mySqlDateFormat(StatisticsGranularity.MONTH);
        assertEquals(s, "%Y-%m");
    }

    @Test
    public void testMySqlDateFormatDay() throws Exception {
        String s = RawStatisticsRepositoryMySqlImpl.mySqlDateFormat(StatisticsGranularity.DAY);
        assertEquals(s, "%Y-%m-%d");
    }

    @Test
    public void testMySqlDateFormatHour() throws Exception {
        String s = RawStatisticsRepositoryMySqlImpl.mySqlDateFormat(StatisticsGranularity.HOUR);
        assertEquals(s, "%Y-%m-%dT%h");
    }

}
