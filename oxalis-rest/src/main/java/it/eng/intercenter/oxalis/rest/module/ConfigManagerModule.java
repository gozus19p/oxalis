package it.eng.intercenter.oxalis.rest.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import it.eng.intercenter.oxalis.rest.client.api.AbstractConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.CertificateConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.EmailSenderConfigManager;
import it.eng.intercenter.oxalis.rest.client.config.RestConfigManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class ConfigManagerModule extends AbstractModule {

	@Override
	protected void configure() {
		Multibinder<AbstractConfigManager> configMultibinder = Multibinder.newSetBinder(binder(), AbstractConfigManager.class);
		log.info("Binding {} as Singleton", CertificateConfigManager.class.getName());
		configMultibinder.addBinding().to(CertificateConfigManager.class).in(Singleton.class);
		log.info("Binding {} as Singleton", EmailSenderConfigManager.class.getName());
		configMultibinder.addBinding().to(EmailSenderConfigManager.class).in(Singleton.class);
		log.info("Binding {} as Singleton", RestConfigManager.class.getName());
		configMultibinder.addBinding().to(RestConfigManager.class).in(Singleton.class);
	}

}
