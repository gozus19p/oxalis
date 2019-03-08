package it.eng.intercenter.oxalis.commons.quartz.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.quartz.scheduler.Quartz;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
@Singleton
public class QuartzManagerServlet extends HttpServlet {

	private static final long serialVersionUID = -3778250271926478122L;

	/**
	 * Constants.
	 */
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SSS");
	private static final String HTML_PARAMETER_START = "START";
	private static final String HTML_PARAMETER_STOP = "STOP";
	private static final String KEY_HTML_PARAMETER = "MODE";
	private static final String RESPONSE_CONTENT_TYPE = "text/html;charset=UTF-8";
	private static final String RESOURCE_CSS_FILE_NAME = "presentation.css";

	/**
	 * Attributes.
	 */
	private static String cssContent;

	/**
	 * DI instances.
	 */
	@Inject
	Quartz quartz;

	/**
	 * Handle POST request, managing Quartz actions.
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(RESPONSE_CONTENT_TYPE);
		printHead(response.getWriter());
		response.getWriter().write("<div>");
		if (request.getParameter(KEY_HTML_PARAMETER).equals(HTML_PARAMETER_STOP)) {
			quartz.pauseScheduler();
			response.getWriter().println("<h1 class=\"standby\">Scheduler has been paused succesfully</h1>");

		} else if (request.getParameter(KEY_HTML_PARAMETER).equals(HTML_PARAMETER_START)) {
			quartz.startScheduler();
			response.getWriter().println("<h1 class=\"online\">Scheduler has been started</h1>");

		} else {
			response.getWriter().println("<h1 class=\"offline\">Unsupported operation! Check system logs\"</h1>");
		}
		response.getWriter().println(
				"<form action=\"quartz\" method=\"GET\"><input type=\"submit\" value=\"Go back to Quartz management\"></input></form>");
		response.getWriter().println("</div></body></html>");
	}

	/**
	 * Handle GET request, printing Quartz status and available actions.
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType(RESPONSE_CONTENT_TYPE);
		try {
			printQuartzSchedulerStatus(response.getWriter());
		} catch (SchedulerException e) {
			response.getOutputStream().println(e.getMessage());
		}
	}

	/**
	 * Print Quartz and available actions.
	 * 
	 * @param writer is the response writer
	 * @throws IOException 
	 * @throws SchedulerException 
	 */
	@SuppressWarnings("unchecked")
	private void printQuartzSchedulerStatus(final PrintWriter writer) throws IOException, SchedulerException {
		printHead(writer);
		boolean schedulerIsRunning = !quartz.getScheduler().isInStandbyMode() && !quartz.getScheduler().isShutdown(),
				schedulerIsInStandby = quartz.getScheduler().isInStandbyMode(),
				schedulerIsShutdown = quartz.getScheduler().isShutdown();

		writer.println("<div>");
		if (schedulerIsRunning) {
			writer.println("<h1>Scheduler is: <span class=\"online\">Online</span></h1>");
			/**
			 * If the scheduler is online list all the configured jobs.
			 */
			for (String groupName : quartz.getScheduler().getJobGroupNames()) {
				writer.println("<h1>Scheduled jobs queried at: <span>" + DATE_FORMATTER.format(new Date()) + "</span></h1>");
				for (JobKey jobKey : quartz.getScheduler().getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
					writer.println("<p><b>Job name:</b> " + jobKey.getName() + "</p>");
					writer.println("<p><b>Job group:</b> " + jobKey.getGroup() + "</p>");
					List<Trigger> triggers = (List<Trigger>) quartz.getScheduler().getTriggersOfJob(jobKey);
					Trigger currentTrigger = triggers.get(0);
					if (currentTrigger.getNextFireTime() != null) {
						writer.println("<p><b>Next execution:</b> "
								+ DATE_FORMATTER.format(currentTrigger.getNextFireTime()) + "</p>");
					} else {
						writer.println("<p>No next execution found</p>");
					}
					if (currentTrigger.getPreviousFireTime() != null) {
						writer.println("<p><b>Last execution:</b> "
								+ DATE_FORMATTER.format(currentTrigger.getPreviousFireTime()) + "</p>");
					} else {
						writer.println("<p>No previous execution found</p>");
					}
					if (currentTrigger.getStartTime() != null) {
						writer.println("<p><b>Trigger started at:</b> "
								+ DATE_FORMATTER.format(currentTrigger.getStartTime()) + "</p>");
					} else {
						writer.println("<p>Trigger not started yet</p>");
					}
				}
			}
		} else if (schedulerIsInStandby) {
			writer.println("<h1>Scheduler is: <span class=\"standby\">In Standby</span></h1>");
		} else if (schedulerIsShutdown) {
			writer.println("<h1>Scheduler is: <span class=\"offline\">Offline</span></h1>");
		} else {
			writer.println("<h1>Scheduler is: <span class=\"offline\">In an unknown status</span></h1>");
		}

		if (schedulerIsRunning) {
			writer.println(
					"<form action=\"quartz\" method=\"POST\"><input type=\"submit\" value=\"Pause scheduler\"></input>"
							+ "<input type=\"hidden\" name=\"" + KEY_HTML_PARAMETER + "\" value=\""
							+ HTML_PARAMETER_STOP + "\"></input></form>");
		} else {
			writer.println(
					"<form action=\"quartz\" method=\"POST\"><input type=\"submit\" value=\"Start scheduler\"></input>"
							+ "<input type=\"hidden\" name=\"" + KEY_HTML_PARAMETER + "\" value=\""
							+ HTML_PARAMETER_START + "\"></input></form>");
		}

		writer.println("</div></body></html>");
	}

	/**
	 * Print the head element of HTML content.
	 * 
	 * @param w
	 *            is the response writer
	 */
	private static void printHead(PrintWriter w) {
		w.println("<!DOCTYPE html>");
		w.println("<head><style>" + getCss() + "</style></head>");
	}

	/**
	 * Method that gives CSS to the current page.
	 * 
	 * @return The whole page CSS.
	 */
	private static String getCss() {
		if (cssContent == null) {
			InputStream cssFile = QuartzManagerServlet.class.getClassLoader().getResourceAsStream(RESOURCE_CSS_FILE_NAME);
			String filePath = QuartzManagerServlet.class.getClassLoader().getResource(RESOURCE_CSS_FILE_NAME).getPath();
			
			log.info("Starting to parse CSS file located at: {}", filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(cssFile));
			StringBuilder sb = new StringBuilder();
			try {
				String currentLine = null;
				while((currentLine = br.readLine()) != null) {
					sb.append(currentLine);
				}
				cssContent = sb.toString();
				log.info("CSS file parsing succeeded");
			} catch (IOException e) {
				log.error("CSS file parsing failed with root cause: {}", e.getMessage());
				log.error("Full stack trace: {}", e);
				log.warn("CSS usage disabled");
				cssContent ="";
			}
		}
		return cssContent;
	}

}
