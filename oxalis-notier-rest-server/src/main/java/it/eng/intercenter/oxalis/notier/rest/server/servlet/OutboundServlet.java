package it.eng.intercenter.oxalis.notier.rest.server.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * @author Manuel Gozzi
 * @date 21 ago 2019
 * @time 12:24:46
 */
@Singleton
public class OutboundServlet extends HttpServlet {

	private static final long serialVersionUID = 8352314062161491298L;

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) {

		// TODO: extends PeppolMessage in order to contain endpoint URI, transport
		// protocol and destination AP certificate (avoid lookup).

		// 1. Retrieve peppol message from reuest and parse it into an object.

		// 2. Directly send it on PEPPOL without process lookup. //TODO: understand
		// "how"

	}

}
