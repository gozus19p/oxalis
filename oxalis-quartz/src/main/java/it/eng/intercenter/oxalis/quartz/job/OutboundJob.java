package it.eng.intercenter.oxalis.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.inject.Inject;

import it.eng.intercenter.oxalis.quartz.job.service.OutboundService;
import lombok.extern.slf4j.Slf4j;

/**
 * Job che si occupa dell'acquisizione e dell'invio dei documenti da Notier
 * verso rete Peppol.
 *
 * @author Manuel Gozzi
 */
@Slf4j
public class OutboundJob implements Job {

	@Inject
	OutboundService outboundService;

	/**
	 * Esegue una chiamata a Notier per recuperare i documenti dal WS relativo.
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		outboundService.processOutbound();

	}

}
