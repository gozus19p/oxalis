package it.eng.intercenter.oxalis.notier.rest.server.module;

import com.google.inject.servlet.ServletModule;

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

	// Path denotating "server side" context of Oxalis.
	private static final String SUB_CONTEXT_SERVLET_PATH = "/server";

	// Send outbound on demand servlet path.
	private static final String OUTBOUND_SERVLET_PATH = SUB_CONTEXT_SERVLET_PATH + "/sendOutbound";

	// Quartz console servlet path.
	private static final String QUARTZ_CONSOLE_SERVLET_PATH = SUB_CONTEXT_SERVLET_PATH + "/quartzConsole";

	@Override
	protected void configureServlets() {
		super.configureServlets();

		log.info("Binding {} as eager singleton", OxalisQuartzConsoleServlet.class.getName());
		bind(OxalisQuartzConsoleServlet.class).asEagerSingleton();

		log.info("Serve {} with {}", QUARTZ_CONSOLE_SERVLET_PATH, OxalisQuartzConsoleServlet.class.getName());
		serve(QUARTZ_CONSOLE_SERVLET_PATH).with(OxalisQuartzConsoleServlet.class);

		log.info("Binding {} as eager singleton", OutboundServlet.class.getName());
		bind(OutboundServlet.class).asEagerSingleton();

		log.info("Serve {} with {}", OUTBOUND_SERVLET_PATH, OutboundServlet.class.getName());
		serve(OUTBOUND_SERVLET_PATH).with(OutboundServlet.class);
	}

}
