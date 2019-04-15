package it.eng.intercenter.oxalis.quartz.module;

import static com.google.inject.name.Names.bindProperties;

import java.io.IOException;
import java.util.Properties;

import org.quartz.Job;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.quartz.job.JobTest;
import it.eng.intercenter.oxalis.quartz.scheduler.GuiceJobFactory;
import it.eng.intercenter.oxalis.quartz.scheduler.Quartz;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Manuel Gozzi
 */
@Slf4j
public class QuartzModule extends AbstractModule {

	/**
	 * Constants.
	 */
	private static final String CONFIG_FILE_NAME = "app-config.properties";
	private static final String QUARTZ_PROPRERTIES_FILE_NAME = "quartz.properties";

	@Override
	protected void configure() {
		log.info("Binding properties retrieved from file: {}", CONFIG_FILE_NAME);
		bindProperties(binder(), getProperties(CONFIG_FILE_NAME));
		bindScheduler();
		bind(Job.class).to(JobTest.class).in(Singleton.class);
		log.info("JobTest has been binded");
	}

	/**
	 * Retrieve Quartz properties from file name located in src/main/resources.
	 * 
	 * @param resourceStreamFileName is the file name
	 * @return the properties defined in the given file
	 */
	private Properties getProperties(String resourceStreamFileName) {
		Properties properties = new Properties();
		try {
			properties.load(QuartzModule.class.getClassLoader().getResourceAsStream(resourceStreamFileName));
		} catch (IOException e) {
			log.error("Error: {}", e.getMessage());
			log.error("{}", e);
		}
		return properties;
	}

	/**
	 * Bind Quartz scheduler to the Guice context.
	 */
	private void bindScheduler() {
		try {
			log.info("Binding {} to {} using properties defined in file: {}",
					new Object[] { SchedulerFactory.class.getName(), StdSchedulerFactory.class.getName(),
							QUARTZ_PROPRERTIES_FILE_NAME });
			bind(SchedulerFactory.class)
					.toInstance(new StdSchedulerFactory(getProperties(QUARTZ_PROPRERTIES_FILE_NAME)));
			log.info("Binding {} as eager singleton", GuiceJobFactory.class.getName());
			bind(GuiceJobFactory.class).asEagerSingleton();
			log.info("Binding {} as eager singleton", Quartz.class.getName());
			bind(Quartz.class).asEagerSingleton();
		} catch (SchedulerException e) {
			log.error(e.getMessage(), e);
		}
	}

}
