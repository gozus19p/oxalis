package it.eng.intercenter.oxalis.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.inject.Inject;

import it.eng.intercenter.oxalis.notier.core.service.api.IOutboundService;
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
	IOutboundService outboundService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("Executing {}", this.getClass().getTypeName());
		try {
			outboundService.processOutboundFlow();
		} catch (Exception e) {
			log.error("{}", e.getMessage(), e);
		}
	}

}
