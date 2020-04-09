package it.eng.intercenter.oxalis.quartz.scheduler;

import com.google.inject.Inject;
import it.eng.intercenter.oxalis.rest.client.config.QuartzConfigManager;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import it.eng.intercenter.oxalis.quartz.util.QuartzPropertiesUtil;

/**
 * @author Manuel Gozzi
 * @date 2 ago 2019
 * @time 12:53:37
 */
public class QuartzSchedulerFactory extends StdSchedulerFactory {

	@Inject
	public QuartzSchedulerFactory(QuartzConfigManager configManager) throws SchedulerException {
		//super(QuartzPropertiesUtil.getProperties(QuartzPropertiesUtil.QUARTZ_PROPRERTIES_FILE_NAME));
		super(configManager.getFullConfiguration());
	}

}
