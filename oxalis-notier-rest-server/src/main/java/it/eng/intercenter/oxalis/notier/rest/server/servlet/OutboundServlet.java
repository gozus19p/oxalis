package it.eng.intercenter.oxalis.notier.rest.server.servlet;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.integration.dto.FullPeppolMessage;
import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisStatusEnum;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import it.eng.intercenter.oxalis.quartz.api.IOutboundService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 * @date 21 ago 2019
 * @time 12:24:46
 */
@Slf4j
@Singleton
public class OutboundServlet extends HttpServlet {

	private static final long serialVersionUID = 8352314062161491298L;

	@Inject
	IOutboundService outboundService;

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		log.info("Received request in order to send a document on demand");
		try {
			// TODO: handle AS2 also!

			// Retrieve PEPPOL full message json String.
			ServletInputStream sis = request.getInputStream();
			String jsonFullPeppolMessage = IOUtils.toString(sis);
			log.info("Given json content has {} characters", jsonFullPeppolMessage.length());

			// Convert given json String into Object.
			FullPeppolMessage fullPeppolMessage = GsonUtil.getInstance().fromJson(jsonFullPeppolMessage, FullPeppolMessage.class);
			log.info("Json String successfully parsed into DTO");

			// Directly send it on PEPPOL without process lookup.
			// TODO: understand "how"
			OxalisMdn result = outboundService.sendFullPeppolMessageOnDemand(fullPeppolMessage);
			log.info("Outcome is {}", result.getStatus().name());

			// Set content-type as "application/json" on response.
			response.setContentType("application/json");

			// Write response in json format.
			response.getWriter().write(GsonUtil.getPrettyPrintedInstance().toJson(result));
			response.getWriter().flush();

			// Set HTTP status code to 200.
			response.setStatus(HttpServletResponse.SC_OK);

		} catch (Exception e) {
			log.error("An error occurred: {}", e.getMessage(), e);
			OxalisMdn result = new OxalisMdn(null, OxalisStatusEnum.KO, e.getMessage());

			// Set content-type as "application/json" on response.
			response.setContentType("application/json");

			// Write json content.
			response.getWriter().write(GsonUtil.getPrettyPrintedInstance().toJson(result));
			response.getWriter().flush();

			// Set HTTP status code to 500. If this code is running, something unexpected
			// happened.
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		}

	}

}
