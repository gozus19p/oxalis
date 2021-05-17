package network.oxalis.commons.persist;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import it.eng.intercenter.oxalis.commons.persist.NotierPersisterHandler;
import network.oxalis.api.persist.ExceptionPersister;
import network.oxalis.api.persist.PayloadPersister;
import network.oxalis.api.persist.PersisterHandler;
import network.oxalis.api.persist.ReceiptPersister;
import network.oxalis.api.plugin.PluginFactory;
import network.oxalis.api.settings.Settings;
import network.oxalis.commons.guice.ImplLoader;
import network.oxalis.commons.guice.OxalisModule;

/**
 * @author Alessandro Vurro
 * @since 4.0.0
 */
public class PersisterCustomModule extends OxalisModule {

	@Override
	protected void configure() {
		// Creates bindings between the annotated PersisterConf items and external type safe config
		bindSettings(PersisterConf.class);

		// Default
		bindTyped(PayloadPersister.class, DefaultPersister.class);
		bindTyped(ReceiptPersister.class, DefaultPersister.class);
		bindTyped(ExceptionPersister.class, DefaultPersister.class);
		bindTyped(PersisterHandler.class, NotierPersisterHandler.class);

		// Noop
		bindTyped(PayloadPersister.class, NoopPersister.class);
		bindTyped(ReceiptPersister.class, NoopPersister.class);
		bindTyped(ExceptionPersister.class, NoopPersister.class);
		bindTyped(PersisterHandler.class, NoopPersister.class);

		// Temp
		bindTyped(PayloadPersister.class, TempPersister.class);
		bindTyped(ReceiptPersister.class, TempPersister.class);
		bindTyped(ExceptionPersister.class, TempPersister.class);
		bindTyped(PersisterHandler.class, TempPersister.class);
	}

	@Provides
	@Singleton
	@Named("plugin")
	protected PayloadPersister getPluginPayloadPersister(PluginFactory pluginFactory) {
		return pluginFactory.newInstance(PayloadPersister.class);
	}

	@Provides
	@Singleton
	@Named("plugin")
	protected ReceiptPersister getPluginReceiptPersister(PluginFactory pluginFactory) {
		return pluginFactory.newInstance(ReceiptPersister.class);
	}

	@Provides
	@Singleton
	@Named("plugin")
	protected PersisterHandler getPluginPersisterHandler(PluginFactory pluginFactory) {
		return pluginFactory.newInstance(PersisterHandler.class);
	}

	@Provides
	@Singleton
	protected PayloadPersister getPayloadPersister(Injector injector, Settings<PersisterConf> settings) {
		return ImplLoader.get(injector, PayloadPersister.class, settings, PersisterConf.PAYLOAD);
	}

	@Provides
	@Singleton
	protected ReceiptPersister getReceiptPersister(Injector injector, Settings<PersisterConf> settings) {
		return ImplLoader.get(injector, ReceiptPersister.class, settings, PersisterConf.RECEIPT);
	}

	/**
	 * @since 4.0.3
	 */
	@Provides
	@Singleton
	protected ExceptionPersister getExceptionPersister(Injector injector, Settings<PersisterConf> settings) {
		return ImplLoader.get(injector, ExceptionPersister.class, settings, PersisterConf.EXCEPTION);
	}

	@Provides
	@Singleton
	protected PersisterHandler getPersisterHandler(Injector injector, Settings<PersisterConf> settings) {
		return ImplLoader.get(injector, PersisterHandler.class, settings, PersisterConf.HANDLER);
	}
}
