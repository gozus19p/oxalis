package it.eng.intercenter.oxalis.notier.rest.server.module;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

import it.eng.intercenter.oxalis.notier.rest.server.service.OxalisLookupNotierIntegrationService;
import it.eng.intercenter.oxalis.notier.rest.server.service.api.IOxalisLookupNotierIntegrationService;
import it.eng.intercenter.oxalis.notier.rest.server.servlet.LookupServlet;
import it.eng.intercenter.oxalis.notier.rest.server.servlet.OutboundServlet;
import it.eng.intercenter.oxalis.notier.rest.server.servlet.OxalisQuartzConsoleServlet;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 * @date 21 ago 2019
 * @time 12:27:00
 */
@Slf4j
public class ServerModule extends ServletModule {

	// Quartz console servlet path.
	private static final String QUARTZ_CONSOLE_SERVLET_PATH = "/quartzConsole";

	// Send outbound on demand servlet path.
	private static final String OUTBOUND_SERVLET_PATH = "/sendOutbound";

	// Lookup.
	private static final String LOOKUP_SERVLET_PATH = "/lookup";

	@Override
	protected void configureServlets() {
		super.configureServlets();

		log.info("Binding {} to {} in {}", IOxalisLookupNotierIntegrationService.class.getTypeName(), OxalisLookupNotierIntegrationService.class.getTypeName(),
				Singleton.class.getTypeName());
		bind(IOxalisLookupNotierIntegrationService.class).to(OxalisLookupNotierIntegrationService.class).in(Singleton.class);

		log.info("Binding {} in {}", LookupServlet.class.getTypeName(), Singleton.class.getTypeName());
		bind(LookupServlet.class).in(Singleton.class);

		log.info("Serve {} with {}", "", LookupServlet.class.getTypeName());
		serve(LOOKUP_SERVLET_PATH).with(LookupServlet.class);

		log.info("Binding {} in {}", OxalisQuartzConsoleServlet.class.getTypeName(), Singleton.class.getTypeName());
		bind(OxalisQuartzConsoleServlet.class).in(Singleton.class);

		log.info("Serve {} with {}", QUARTZ_CONSOLE_SERVLET_PATH, OxalisQuartzConsoleServlet.class.getTypeName());
		serve(QUARTZ_CONSOLE_SERVLET_PATH).with(OxalisQuartzConsoleServlet.class);

		log.info("Binding {} in {}", OutboundServlet.class.getTypeName(), Singleton.class.getTypeName());
		bind(OutboundServlet.class).in(Singleton.class);

		log.info("Serve {} with {}", OUTBOUND_SERVLET_PATH, OutboundServlet.class.getTypeName());
		serve(OUTBOUND_SERVLET_PATH).with(OutboundServlet.class);
	}

}
