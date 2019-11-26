package it.eng.intercenter.oxalis.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

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
	private IOutboundService outboundService;

	@Override
	public void execute(JobExecutionContext context) {
		log.info("Executing {}", this.getClass().getTypeName());
		try {
			outboundService.processOutboundFlow();
		} catch (Exception e) {
			log.error("An error occurred during Outbound flow processing: {}", e.getMessage(), e);
		}
	}

}
