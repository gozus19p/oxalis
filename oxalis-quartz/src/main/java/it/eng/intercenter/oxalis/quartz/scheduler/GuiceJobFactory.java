package it.eng.intercenter.oxalis.quartz.scheduler;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Singleton
@Slf4j
public class GuiceJobFactory implements JobFactory {

	private final Injector guice;

	@Inject
	public GuiceJobFactory(final Injector guice) {
		log.info("Injecting Guice Injector on {}", new Object[] { GuiceJobFactory.class.getName() });
		this.guice = guice;
	}

	@Override
	public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
		JobDetail jobDetail = bundle.getJobDetail();
		log.info("Creating instance of Job {}", jobDetail.getJobClass().getName());
		Class<? extends Job> jobClass = jobDetail.getJobClass();
		return guice.getInstance(jobClass);
	}

}
