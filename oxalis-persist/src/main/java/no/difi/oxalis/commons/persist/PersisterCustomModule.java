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

import it.eng.intercenter.oxalis.commons.persist.NotierPersisterHandler;
import no.difi.oxalis.api.persist.ExceptionPersister;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.persist.ReceiptPersister;

/**
 * @author erlend
 * @since 4.0.0
 */
public class PersisterCustomModule extends PersisterModule {

	@Override
	protected void configure() {
		// Creates bindings between the annotated PersisterConf items and external type
		// safe config
		bindSettings(PersisterConf.class);

		// Default
		bindTyped(PayloadPersister.class, DefaultPersister.class);
		bindTyped(ReceiptPersister.class, DefaultPersister.class);
		bindTyped(ExceptionPersister.class, DefaultPersister.class);
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

}
