/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

package no.difi.oxalis.commons.mode;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.typesafe.config.Config;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import net.klakegg.pkix.ocsp.api.OcspFetcher;
import no.difi.certvalidator.api.CrlFetcher;
import no.difi.oxalis.api.settings.DefaultValue;
import no.difi.oxalis.api.settings.Path;
import no.difi.vefa.peppol.mode.Mode;

/**
 * @author erlend
 * @since 4.0.4
 */
public class ModeProvider implements Provider<Mode> {

	@Inject
	@Named("reference")
	private Config config;

	@Inject
	private OcspFetcher ocspFetcher;

	@Inject
	private CrlFetcher crlFetcher;

	@Inject
	private Tracer tracer;

	@Path("oxalis.operation.mode")
	@DefaultValue("TEST")
	private String operationMode;

	@Override
	public Mode get() {
		Span span = tracer.buildSpan("Mode detection").start();
		DefaultSpanManager.getInstance().activate(span);
		try {
			Map<String, Object> objectStorage = new HashMap<>();
			objectStorage.put("ocsp_fetcher", ocspFetcher);
			objectStorage.put("crlFetcher", crlFetcher);

			return Mode.of(operationMode);
		} finally {
			span.finish();
		}
	}
}
