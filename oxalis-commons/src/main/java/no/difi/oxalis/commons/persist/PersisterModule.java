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

package no.difi.oxalis.commons.persist;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import it.eng.intercenter.oxalis.commons.persist.NotierPersisterHandler;
import no.difi.oxalis.api.persist.ExceptionPersister;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.api.plugin.PluginFactory;
import no.difi.oxalis.api.settings.Settings;
import no.difi.oxalis.commons.guice.ImplLoader;
import no.difi.oxalis.commons.guice.OxalisModule;

/**
 * @author erlend
 * @since 4.0.0
 */
public class PersisterModule extends OxalisModule {

    @Override
    protected void configure() {
        // Creates bindings between the annotated PersisterConf items and external type safe config
        bindSettings(PersisterConf.class);

        // Default
        bindTyped(PayloadPersister.class, DefaultPersister.class);
        bindTyped(ReceiptPersister.class, DefaultPersister.class);
        bindTyped(ExceptionPersister.class, DefaultPersister.class);

        /**
         * @author Manuel Gozzi
         */
//      bindTyped(PersisterHandler.class, DefaultPersisterHandler.class);
        System.out.println("Binding di NPH");
        bindTyped(PersisterHandler.class, NotierPersisterHandler.class);

        // Noop
        bindTyped(PayloadPersister.class, NoopPersister.class);
        bindTyped(ReceiptPersister.class, NoopPersister.class);
        bindTyped(ExceptionPersister.class, NoopPersister.class);
        bindTyped(PersisterHandler.class, NoopPersister.class);

        // Temp
        bindTyped(PayloadPersister.class, TempPersister.class);
        bindTyped(ReceiptPersister.class, TempPersister.class);
        bindTyped(ExceptionPersister.class, TempPersister.class);
        bindTyped(PersisterHandler.class, TempPersister.class);
    }

    @Provides
    @Singleton
    @Named("plugin")
    protected PayloadPersister getPluginPayloadPersister(PluginFactory pluginFactory) {
        return pluginFactory.newInstance(PayloadPersister.class);
    }

    @Provides
    @Singleton
    @Named("plugin")
    protected ReceiptPersister getPluginReceiptPersister(PluginFactory pluginFactory) {
        return pluginFactory.newInstance(ReceiptPersister.class);
    }

    @Provides
    @Singleton
    @Named("plugin")
    protected PersisterHandler getPluginPersisterHandler(PluginFactory pluginFactory) {
        return pluginFactory.newInstance(PersisterHandler.class);
    }

    @Provides
    @Singleton
    protected PayloadPersister getPayloadPersister(Injector injector, Settings<PersisterConf> settings) {
        return ImplLoader.get(injector, PayloadPersister.class, settings, PersisterConf.PAYLOAD);
    }

    @Provides
    @Singleton
    protected ReceiptPersister getReceiptPersister(Injector injector, Settings<PersisterConf> settings) {
        return ImplLoader.get(injector, ReceiptPersister.class, settings, PersisterConf.RECEIPT);
    }

    /**
     * @since 4.0.3
     */
    @Provides
    @Singleton
    protected ExceptionPersister getExceptionPersister(Injector injector, Settings<PersisterConf> settings) {
        return ImplLoader.get(injector, ExceptionPersister.class, settings, PersisterConf.EXCEPTION);
    }

    @Provides
    @Singleton
    protected PersisterHandler getPersisterHandler(Injector injector, Settings<PersisterConf> settings) {
        return ImplLoader.get(injector, PersisterHandler.class, settings, PersisterConf.HANDLER);
    }
}
