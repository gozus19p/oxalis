package it.eng.intercenter.oxalis.quartz.config;

import com.google.inject.servlet.ServletModule;

/**
 * Servlet Module.
 * @author Manuel Gozzi
 */
public class ConfigServletModule extends ServletModule {

	@Override
	protected void configureServlets() {
		super.configureServlets();
		install(new QuartzModule());
	}
	
}
