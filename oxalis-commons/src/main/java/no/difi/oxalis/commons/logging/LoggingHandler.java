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

package no.difi.oxalis.commons.logging;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import no.difi.oxalis.api.logging.Configurator;
import no.difi.oxalis.api.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class triggers {@link Configurator} only in the case of {@link LoggingConf#CONFIG} being set. This makes is
 * possible to both configure logging very early in startup and remove coupling towards Logback.
 *
 * @author erlend
 */
class LoggingHandler {

    private static Logger logger = LoggerFactory.getLogger(LoggingHandler.class);

    @Inject
    @SuppressWarnings("unchecked")
    public void load(Injector injector, Settings<LoggingConf> settings) {
        logger.debug("Logger config: {}", settings.getString(LoggingConf.CONFIG));

        if (settings.getString(LoggingConf.CONFIG) == null)
            return;

        logger.info("Logging service: {}", settings.getString(LoggingConf.SERVICE));

        injector.getInstance(Key.get(Configurator.class, settings.getNamed(LoggingConf.SERVICE))).execute();
    }
}
