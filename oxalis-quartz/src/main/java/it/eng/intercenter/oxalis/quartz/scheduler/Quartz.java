package it.eng.intercenter.oxalis.quartz.scheduler;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;

/**
 * 
 * @author Manuel Gozzi
 */
@Singleton
public class Quartz {
	
	private final Scheduler scheduler;
	
	@Inject
	public Quartz(final SchedulerFactory factory, final GuiceJobFactory jobFactory) throws SchedulerException {
		this.scheduler = factory.getScheduler();
		this.scheduler.setJobFactory(jobFactory);
		this.scheduler.start();
	}
	
	public final Scheduler getScheduler() {
		return this.scheduler;
	}

}
