package it.eng.intercenter.oxalis.quartz.config;

import static com.google.inject.name.Names.bindProperties;

import java.io.IOException;
import java.util.Properties;

import org.quartz.Job;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.quartz.job.JobTest;
import it.eng.intercenter.oxalis.quartz.scheduler.GuiceJobFactory;
import it.eng.intercenter.oxalis.quartz.scheduler.Quartz;
import no.difi.oxalis.commons.guice.OxalisModule;

/**
 * 
 * @author Manuel Gozzi
 */
public class QuartzModule extends OxalisModule {
	
	private static final Logger log = LoggerFactory.getLogger(QuartzModule.class);

	@Override
	protected void configure() {
		bindProperties(binder(), getProperties("app-config.properties"));
		System.out.println();
		System.out.println();
		System.out.println("HO CONFIGURATO!");
		System.out.println();
		System.out.println();
		log.info("JobTest has been binded");
		bind(Job.class).to(JobTest.class).in(Singleton.class);
		bindScheduler();
	}
	
	private Properties getProperties(String str) {
		Properties properties = new Properties();
		try {
			properties.load(QuartzModule.class.getClassLoader().getResourceAsStream(str));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
	}
	
	private void bindScheduler() {
		try {
			log.info("Scheduler has been binded");
			bind(SchedulerFactory.class).toInstance(new StdSchedulerFactory(getProperties("quartz.properties")));
			bind(GuiceJobFactory.class);
			bind(Quartz.class).asEagerSingleton();
		} catch (SchedulerException e) {
			log.warn(e.getMessage(), e);
		}
	}
	
}
