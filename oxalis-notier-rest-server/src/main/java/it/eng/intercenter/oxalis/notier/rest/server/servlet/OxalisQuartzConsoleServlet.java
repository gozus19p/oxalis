package it.eng.intercenter.oxalis.notier.rest.server.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommand;
import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommandResult;
import it.eng.intercenter.oxalis.integration.util.GsonUtil;
import it.eng.intercenter.oxalis.quartz.scheduler.service.QuartzSchedulerConsole;

/**
 * @author Manuel Gozzi
 * @date 20 ago 2019
 * @time 16:23:49
 */
@Singleton
public class OxalisQuartzConsoleServlet extends HttpServlet {

	private static final long serialVersionUID = 7488655158610538495L;

	@Inject
	QuartzSchedulerConsole console;

	/**
	 * Receive and execute command in order to manage Scheduler from NoTI-ER.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO: add SSL context here...
		// Retrieve json content.
		String jsonContent = IOUtils.toString(request.getInputStream());

		// Parse json String into object.
		OxalisQuartzCommand command = GsonUtil.getInstance().fromJson(jsonContent, OxalisQuartzCommand.class);

		// Execute command and retrieve result.
		OxalisQuartzCommandResult result = console.executeCommand(command);

		// Parse result into pretty printed json String.
		String jsonOutput = GsonUtil.getPrettyPrintedInstance().toJson(result);

		// Declare content-type as "application/json".
		response.setContentType("application/json");

		// Print json output into response.
		response.getOutputStream().print(jsonOutput);

		// Set HTTP status to 200.
		response.setStatus(HttpServletResponse.SC_OK);
	}

}
