package it.eng.intercenter.oxalis.quartz.module;

import com.google.inject.servlet.ServletModule;

import lombok.extern.slf4j.Slf4j;

/**
 * Servlet Module.
 *
 * @author Manuel Gozzi
 */
@Slf4j
public class ConfigServletModule extends ServletModule {

	@Override
	protected void configureServlets() {
		super.configureServlets();

		log.info("Installing {}", QuartzModule.class.getName());
		install(new QuartzModule());
	}

}
