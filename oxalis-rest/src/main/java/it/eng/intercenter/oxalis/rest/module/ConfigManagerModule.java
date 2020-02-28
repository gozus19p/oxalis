package it.eng.intercenter.oxalis.rest.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import it.eng.intercenter.oxalis.rest.client.api.AbstractConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.EmailSenderConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.QuartzConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.RestConfigManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class ConfigManagerModule extends AbstractModule {

	@Override
	protected void configure() {
		log.info("Starting multibinding related to {}", AbstractConfigManager.class.getTypeName());
		Multibinder<AbstractConfigManager> configMultibinder = Multibinder.newSetBinder(binder(), AbstractConfigManager.class);

		log.info("Binding {} to {} in {}", AbstractConfigManager.class.getTypeName(), CertificateConfigManager.class.getTypeName(),
				Singleton.class.getTypeName());
		configMultibinder.addBinding().to(CertificateConfigManager.class).in(Singleton.class);

		log.info("Binding {} to {} in {}", AbstractConfigManager.class.getTypeName(), EmailSenderConfigManager.class.getTypeName(),
				Singleton.class.getTypeName());
		configMultibinder.addBinding().to(EmailSenderConfigManager.class).in(Singleton.class);

		log.info("Binding {} to {} in {}", AbstractConfigManager.class.getTypeName(), QuartzConfigManager.class.getTypeName(), Singleton.class.getTypeName());
		configMultibinder.addBinding().to(QuartzConfigManager.class).in(Singleton.class);

		log.info("Binding {} to {} in {}", AbstractConfigManager.class.getTypeName(), RestConfigManager.class.getTypeName(), Singleton.class.getTypeName());
		configMultibinder.addBinding().to(RestConfigManager.class).in(Singleton.class);
	}

}
