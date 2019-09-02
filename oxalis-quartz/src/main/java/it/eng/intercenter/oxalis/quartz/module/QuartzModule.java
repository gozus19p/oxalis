package it.eng.intercenter.oxalis.quartz.module;

import static com.google.inject.name.Names.bindProperties;

import org.quartz.Job;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import it.eng.intercenter.oxalis.quartz.job.OutboundJob;
import it.eng.intercenter.oxalis.quartz.job.TestJob;
import it.eng.intercenter.oxalis.quartz.scheduler.GuiceJobFactory;
import it.eng.intercenter.oxalis.quartz.scheduler.Quartz;
import it.eng.intercenter.oxalis.quartz.scheduler.QuartzSchedulerConsole;
import it.eng.intercenter.oxalis.quartz.scheduler.QuartzSchedulerFactory;
import it.eng.intercenter.oxalis.quartz.util.QuartzPropertiesUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class QuartzModule extends AbstractModule {

	/**
	 * Configure Guice's bindings.
	 */
	@Override
	protected void configure() {
		log.info("Binding properties retrieved from file: {}", QuartzPropertiesUtil.CONFIG_FILE_NAME);
		bindProperties(binder(), QuartzPropertiesUtil.getProperties(QuartzPropertiesUtil.CONFIG_FILE_NAME));

		log.info("Binding Quartz Scheduler");
		bindScheduler();

		log.info("Binding {} in {}", QuartzSchedulerConsole.class.getTypeName(), Singleton.class.getTypeName());
		bind(QuartzSchedulerConsole.class).in(Singleton.class);

		log.info("Starting multiple bindings related to {}", Job.class.getTypeName());
		Multibinder<Job> jobs = Multibinder.newSetBinder(binder(), Job.class);

		log.info("Binding {} to {} in {}", Job.class.getTypeName(), TestJob.class.getTypeName(), Singleton.class.getTypeName());
		jobs.addBinding().to(TestJob.class).in(Singleton.class);

		log.info("Binding {} to {} in {}", Job.class.getTypeName(), OutboundJob.class.getTypeName(), Singleton.class.getTypeName());
		jobs.addBinding().to(OutboundJob.class).in(Singleton.class);
	}

	/**
	 * Bind Quartz scheduler to the Guice context.
	 */
	private void bindScheduler() {
		log.info("Binding {} to {} in {} using properties defined in file: {}", new Object[] { SchedulerFactory.class.getTypeName(),
				StdSchedulerFactory.class.getTypeName(), Singleton.class.getTypeName(), QuartzPropertiesUtil.QUARTZ_PROPRERTIES_FILE_NAME });
		bind(SchedulerFactory.class).to(QuartzSchedulerFactory.class).in(Singleton.class);

		log.info("Binding {} to {} in {}", JobFactory.class.getTypeName(), GuiceJobFactory.class.getTypeName(), Singleton.class);
		bind(JobFactory.class).to(GuiceJobFactory.class).in(Singleton.class);

		log.info("Binding {} in {}", Quartz.class.getTypeName(), Singleton.class.getTypeName());
		bind(Quartz.class).in(Singleton.class);
	}

}
