package it.eng.intercenter.oxalis.quartz.module;

import com.google.inject.servlet.ServletModule;

import it.eng.intercenter.oxalis.quartz.servlet.QuartzManagerServlet;

/**
 * Servlet Module.
 * 
 * @author Manuel Gozzi
 */
public class ConfigServletModule extends ServletModule {

	@Override
	protected void configureServlets() {
		super.configureServlets();
		install(new QuartzModule());
		serve("/quartz").with(QuartzManagerServlet.class);
	}

}
