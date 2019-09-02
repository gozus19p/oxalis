package it.eng.intercenter.oxalis.notier.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.notier.core.service.api.IOutboundService;
import it.eng.intercenter.oxalis.notier.core.service.api.IOxalisLookupNotierIntegrationService;
import it.eng.intercenter.oxalis.notier.core.service.impl.OutboundService;
import it.eng.intercenter.oxalis.notier.core.service.impl.OxalisLookupNotierIntegrationService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 * @date 30 ago 2019
 * @time 17:13:15
 */
@Slf4j
public class NotierCoreModule extends AbstractModule {

	@Override
	public void configure() {

		log.info("Binding {} to {} in {}", IOxalisLookupNotierIntegrationService.class.getTypeName(), OxalisLookupNotierIntegrationService.class.getTypeName(),
				Singleton.class.getTypeName());
		bind(IOxalisLookupNotierIntegrationService.class).to(OxalisLookupNotierIntegrationService.class).in(Singleton.class);

		log.info("Binding {} to {} in {}", IOutboundService.class.getTypeName(), OutboundService.class.getTypeName(), Singleton.class.getTypeName());
		bind(IOutboundService.class).to(OutboundService.class).in(Singleton.class);

	}

}
