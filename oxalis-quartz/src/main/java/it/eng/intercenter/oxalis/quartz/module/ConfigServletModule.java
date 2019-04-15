package it.eng.intercenter.oxalis.quartz.module;

import com.google.inject.servlet.ServletModule;

import it.eng.intercenter.oxalis.quartz.servlet.QuartzManagerServlet;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet Module.
 * 
 * @author Manuel Gozzi
 */
@Slf4j
public class ConfigServletModule extends ServletModule {

	private static final String SERVLET_PATH_QUARTZ = "/quartz";

	@Override
	protected void configureServlets() {
		super.configureServlets();
		log.info("Install {}", QuartzModule.class.getName());
		install(new QuartzModule());
		log.info("Serving Quartz management servlet on {} with {}",
				new Object[] { SERVLET_PATH_QUARTZ, QuartzManagerServlet.class.getName() });
		serve(SERVLET_PATH_QUARTZ).with(QuartzManagerServlet.class);
	}

}
