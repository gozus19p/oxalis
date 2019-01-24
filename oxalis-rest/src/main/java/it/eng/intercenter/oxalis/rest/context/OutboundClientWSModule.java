package it.eng.intercenter.oxalis.rest.context;

import org.glassfish.jersey.server.ResourceConfig;

import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import it.eng.intercenter.oxalis.rest.ws.OutboundClientWS;

/**
 * 
 * @author Manuel Gozzi
 *
 */
public class OutboundClientWSModule extends ServletModule {
	
	@Override
	protected void configureServlets() {
		bind(new TypeLiteral<Dao<String>>() {}).to(DaoImpl.class);
		ResourceConfig rc = new ResourceConfig();
		rc.register(OutboundClientWS.class);
		for (Class<?> resource : rc.getClasses()) {
			bind(resource);
		}
		serve("/rest*").with(GuiceContainer.class);
	}

}
