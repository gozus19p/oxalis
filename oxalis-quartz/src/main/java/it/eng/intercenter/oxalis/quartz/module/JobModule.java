package it.eng.intercenter.oxalis.quartz.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.quartz.api.IOutboundService;
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
		log.info("Binding {} to {} in {}", IOutboundService.class.getTypeName(), OutboundService.class.getTypeName(), Singleton.class.getTypeName());
		bind(IOutboundService.class).to(OutboundService.class).in(Singleton.class);
	}

}
