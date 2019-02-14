package it.eng.intercenter.oxalis.quartz.job;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobTest implements Job {

	private final Logger log = LoggerFactory.getLogger(JobTest.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		File file = new File("C:\\test.txt");
		try {
			FileWriter fw = new FileWriter(file);
			fw.write("Prova");
			fw.flush();
			fw.close();
		} catch (Exception e) {
			
		}
		System.out.println();
		System.out.println();
		System.out.println("Il job ha girato!");
		System.out.println();
		System.out.println();
		log.info("Job: {} has been executed at {}", new Object[] {"JobTest", new Date().toString()});
	}

}
