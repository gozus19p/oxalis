package it.eng.intercenter.oxalis.quartz.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.quartz.job.service.OutboundService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 * @date 21 ago 2019
 * @time 12:34:19
 */
@Slf4j
public class JobModule extends AbstractModule {

	@Override
	protected void configure() {
		log.info("Binding {} in {}", OutboundService.class.getName(), Singleton.class.getName());
		bind(OutboundService.class).in(Singleton.class);
	}

}
