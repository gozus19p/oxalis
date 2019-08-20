package it.eng.intercenter.oxalis.quartz.module;

import com.google.inject.servlet.ServletModule;

import it.eng.intercenter.oxalis.quartz.servlet.OxalisQuartzConsoleServlet;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet Module.
 *
 * @author Manuel Gozzi
 */
@Slf4j
public class ConfigServletModule extends ServletModule {

	private static final String CONSOLE_SERVLET_PATH = "/quartzConsole";

	@Override
	protected void configureServlets() {
		super.configureServlets();

		log.info("Installing {}", QuartzModule.class.getName());
		install(new QuartzModule());

		log.info("Binding {} as eager singleton", OxalisQuartzConsoleServlet.class.getName());
		bind(OxalisQuartzConsoleServlet.class).asEagerSingleton();

		log.info("Serve {} with {}", CONSOLE_SERVLET_PATH, OxalisQuartzConsoleServlet.class.getName());
		serve(CONSOLE_SERVLET_PATH).with(OxalisQuartzConsoleServlet.class);
	}

}
