package it.eng.intercenter.oxalis.quartz.job;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestJob implements Job {

	@Inject
	@Named("reference")
	Config referenceConf;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("Executed");
		log.info("Job: {} has been executed at {}{}Message: {}", new Object[] { TestJob.class.getName(),
				new Date().toString(), System.getProperty("line.separator"), referenceConf.getString("test.test") });
	}

}
