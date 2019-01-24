package it.eng.intercenter.oxalis.rest.ws;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import it.eng.intercenter.oxalis.rest.context.Dao;
import no.difi.oxalis.api.lang.OxalisException;
import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.api.outbound.TransmissionResponse;
import no.difi.oxalis.api.outbound.Transmitter;
import no.difi.oxalis.outbound.OxalisOutboundComponent;

@Path("/invio")
public class OutboundClientWS {
	
	private Dao<String> dao;

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final OxalisOutboundComponent oxalisOutboundComponent = new OxalisOutboundComponent();
	
	@Inject
	public OutboundClientWS(Dao<String> dao) {
		this.dao = dao;
	}
	
	@GET
	@Path("/test")
	@Produces("text/plain")
	public String helloWorld() {
		StringBuilder sb = new StringBuilder();
		sb.append("<h1>GET: Test</h1>");
		sb.append(LINE_SEPARATOR);
		sb.append("<p>Hello World!</p>");
		return sb.toString();
	}

	@POST
	@Path("/messaggio")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON)
	public TransmissionResponse invioMessaggio(TransmissionMessage transmissionMessage) {
		Transmitter t = oxalisOutboundComponent.getTransmitter();
		TransmissionResponse transmissionResponse = null;
		try {
			transmissionResponse = t.transmit(transmissionMessage);
		} catch (OxalisException oex) {
			System.out.println(oex.getMessage());
		} catch (Exception nex) {
			System.out.println(nex.getMessage());
		}
		return transmissionResponse;
	}

}
