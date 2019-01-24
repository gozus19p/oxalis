package it.eng.intercenter.oxalis.rest.context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import it.eng.intercenter.oxalis.rest.ws.OutboundClientWS;

public class RestContextListener extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule() {
			@Override
			protected void configureServlets() {
				bind(Dao.class).to(DaoImpl.class);
				ResourceConfig rc = new PackagesResourceConfig("it.eng.intercenter.oxalis.rest.ws");
				for (Class<?> resource : rc.getClasses()) {
					bind(resource);
				}
				serve("/rest/*").with(GuiceContainer.class);
				bind(OutboundClientWS.class);
			}
		});
	}

}
