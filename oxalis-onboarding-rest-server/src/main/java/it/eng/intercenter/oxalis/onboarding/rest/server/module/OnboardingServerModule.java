package it.eng.intercenter.oxalis.onboarding.rest.server.module;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import it.eng.intercenter.oxalis.onboarding.rest.server.servlet.OutboundFlowServlet;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class OnboardingServerModule extends ServletModule {

	private static final String OUTBOUND_FLOW_SERVLET_PATH = "/outboundFlow";

	@Override
	protected void configureServlets() {
		super.configureServlets();

		log.info("Binding {} in {}", OutboundFlowServlet.class.getTypeName(), Singleton.class.getTypeName());
		bind(OutboundFlowServlet.class).in(Singleton.class);

		log.info("Serve {} with {}", OutboundFlowServlet.class.getTypeName(), OUTBOUND_FLOW_SERVLET_PATH);
		serve(OUTBOUND_FLOW_SERVLET_PATH).with(OutboundFlowServlet.class);
	}
}
