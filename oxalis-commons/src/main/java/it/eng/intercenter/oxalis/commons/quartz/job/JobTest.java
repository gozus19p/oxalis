package it.eng.intercenter.oxalis.commons.quartz.job;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobTest implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println();
		System.out.println();
		System.out.println("Il job ha girato!");
		System.out.println();
		System.out.println();
		log.info("Job: {} has been executed at {}", new Object[] { "JobTest", new Date().toString() });
	}

}
