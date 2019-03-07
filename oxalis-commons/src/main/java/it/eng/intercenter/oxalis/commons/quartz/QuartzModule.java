package it.eng.intercenter.oxalis.commons.quartz;

import static com.google.inject.name.Names.bindProperties;

import java.io.IOException;
import java.util.Properties;

import org.quartz.Job;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.commons.quartz.job.JobTest;
import it.eng.intercenter.oxalis.quartz.scheduler.GuiceJobFactory;
import it.eng.intercenter.oxalis.quartz.scheduler.Quartz;
import lombok.extern.slf4j.Slf4j;
import no.difi.oxalis.commons.guice.OxalisModule;

/**
 * 
 * @author Manuel Gozzi
 */
@Slf4j
public class QuartzModule extends OxalisModule {
	
	@Override
	protected void configure() {
		bindProperties(binder(), getProperties("app-config.properties"));
		System.out.println();
		System.out.println();
		System.out.println("HO CONFIGURATO!");
		System.out.println();
		System.out.println();
		log.info("JobTest has been binded");
		bindScheduler();
		bind(Job.class).to(JobTest.class).in(Singleton.class);
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
			bind(GuiceJobFactory.class).asEagerSingleton();
			System.out.println();System.out.println();
			System.out.println("Bind di Quartz.class come eager singleton");
			bind(Quartz.class).asEagerSingleton();
		} catch (SchedulerException e) {
			log.warn(e.getMessage(), e);
		}
	}
	
}
