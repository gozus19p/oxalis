package it.eng.intercenter.oxalis.quartz.scheduler;

import com.google.inject.Inject;
import it.eng.intercenter.oxalis.rest.client.config.QuartzConfigManager;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Manuel Gozzi
 */
public class QuartzSchedulerFactory extends StdSchedulerFactory {

	@Inject
	public QuartzSchedulerFactory(QuartzConfigManager configManager) throws SchedulerException {
		super(configManager.getFullConfiguration());
	}
}
