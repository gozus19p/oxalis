package it.eng.intercenter.oxalis.quartz.scheduler;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import lombok.extern.slf4j.Slf4j;

/**
 * Class that implements Quartz inside Guice workflow
 * @author Manuel Gozzi
 */
@Singleton
@Slf4j
public class Quartz {

	/**
	 * Class attributes
	 */
	private final Scheduler scheduler;

	/**
	 * Quartz DI injection
	 * 
	 * @param factory
	 *            is the Quartz Scheduler factory class
	 * @param jobFactory
	 *            is the Quartz Job factory class
	 * @throws SchedulerException
	 *             if something goes wrong during the Quartz scheduler startup
	 *             process
	 */
	@Inject
	public Quartz(final SchedulerFactory factory, final GuiceJobFactory jobFactory) throws SchedulerException {
		log.info("Inject {}", Quartz.class.getName());
		this.scheduler = factory.getScheduler();
		this.scheduler.setJobFactory(jobFactory);
		startScheduler();
		log.info("{} injecting process has been completed successfully", Quartz.class.getName());
	}

	/**
	 * @return the scheduler
	 */
	public final Scheduler getScheduler() {
		return this.scheduler;
	}

	/**
	 * @author Manuel Gozzi
	 * @return true if scheduler paused correctly, false otherwise
	 */
	public boolean pauseScheduler() {
		log.info("Pausing Quartz scheduler");
		try {
			for (String groupName : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
					try {
						scheduler.interrupt(jobKey);
						log.info("Job {} has been interrupted successfully", jobKey.getName());
					} catch (SchedulerException e) {
						log.error("Job {} has not been interrupted with root cause: {}",
								new Object[] { jobKey.getName(), e.getMessage() });
						log.error("Full stack trace: {}", e);
					}
				}
			}
			this.scheduler.standby();
			log.warn("Quartz scheduler entered in standby mode successfully");
			return true;
		} catch (SchedulerException e) {
			log.error("Pausing Quartz scheduler process failed with root cause: {}", e.getMessage());
			log.error("Full stack trace: {}", e);
			return false;
		}
	}

	/**
	 * @author Manuel Gozzi
	 * @return true if scheduler started correctly, false otherwise
	 */
	public boolean startScheduler() {
		log.info("Starting Quartz scheduler");
		try {
			this.scheduler.start();
			log.info("Quartz scheduler started succesfully");
			return true;
		} catch (Exception e) {
			log.error("Quartz scheduler starting process failed with root cause: {}", e.getMessage());
			log.error("Full stack trace: {}", e);
			return false;
		}
	}

}
