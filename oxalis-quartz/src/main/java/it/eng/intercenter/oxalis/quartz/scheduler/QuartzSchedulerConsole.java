package it.eng.intercenter.oxalis.quartz.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommand;
import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommandResult;
import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommandResultDetails;
import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzJobKey;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandActionEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandOutcomeEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandScopeEnum;
import it.eng.intercenter.oxalis.quartz.scheduler.util.QuartzSchedulerConsoleUtil;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Manuel Gozzi
 * @date 20 ago 2019
 * @time 10:47:19
 * @jira
 */
@Singleton
@Slf4j
public class QuartzSchedulerConsole {

	@Inject
	private Quartz quartz;

	/**
	 * Constants.
	 */
	private static final String SUCCESS_MESSAGE = "Operation completed successfully";

	/**
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 15:38:38
	 * @jira
	 * @param command
	 * @return
	 * @throws SchedulerException
	 */
	public OxalisQuartzCommandResult executeCommand(OxalisQuartzCommand command) {
		switch (command.getAction()) {
		case START:
			return doStart(command.getScope(), command.getJobKeys());
		case STOP:
			return doStop(command.getScope(), command.getJobKeys());
		case VIEW:
			return doView(command.getScope(), command.getJobKeys());
		default:
			log.warn("Unhandled action in executeCommand() method: {}", command.getAction().name());
			break;
		}
		return QuartzSchedulerConsoleUtil.invalidAction(command.getAction());
	}

	/**
	 * Stops jobs.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 15:07:46
	 * @param scope
	 * @param jobs
	 * @return
	 * @throws SchedulerException
	 */
	private OxalisQuartzCommandResult doStop(OxalisQuartzCommandScopeEnum scope, List<OxalisQuartzJobKey> jobs) {
		final OxalisQuartzCommandActionEnum action = OxalisQuartzCommandActionEnum.STOP;
		switch (scope) {
		case ALL_JOBS:
			try {
				// Check if scheduler is started.
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}

				// Pause all jobs.
				scheduler().pauseAll();
				log.info("All jobs paused successfully");

				// Build details for every job.
				List<OxalisQuartzCommandResultDetails> detailsList = new ArrayList<>();
				for (JobKey jk : scheduler().getJobKeys(GroupMatcher.anyJobGroup())) {
					OxalisQuartzJobKey oxalisQuartzJob = new OxalisQuartzJobKey();
					oxalisQuartzJob.setJobGroup(jk.getGroup());
					oxalisQuartzJob.setJobName(jk.getName());
					detailsList.add(OxalisQuartzCommandResultDetails.ofStandbyJob(oxalisQuartzJob));
				}
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, detailsList, SUCCESS_MESSAGE);

			} catch (SchedulerException e) {

				// Logging.
				log.error("Quartz scheduler pausing process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}
		case JOB_LIST:
			try {
				// Check if scheduler is started.
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}

				List<OxalisQuartzCommandResultDetails> detailsList = new ArrayList<>();

				// Pause every given single job.
				for (OxalisQuartzJobKey job : jobs) {
					try {
						scheduler().pauseJob(new JobKey(job.getJobName(), job.getJobGroup()));

						// Logging.
						log.info("{} job of group {} paused successfully", job.getJobName(), job.getJobGroup());
						detailsList.add(OxalisQuartzCommandResultDetails.ofStandbyJob(job));

					} catch (SchedulerException e) {

						// Logging.
						log.error("{} job of group {} job did not pause successfully", job.getJobName(), job.getJobGroup());
						detailsList.add(OxalisQuartzCommandResultDetails.ofDeadJob(job));
					}
				}
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, detailsList, SUCCESS_MESSAGE);

			} catch (SchedulerException e) {

				// Logging.
				log.error("Quartz scheduler pausing process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}
		case SINGLE_JOB:
			try {

				// Check if scheduler is started.
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}

				// Pause single job.
				scheduler().pauseJob(new JobKey(jobs.get(0).getJobName(), jobs.get(0).getJobGroup()));
				log.info("{} job of group {} paused successfuly", jobs.get(0).getJobName(), jobs.get(0).getJobGroup());
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, OxalisQuartzCommandResultDetails.ofStandbyJob(jobs.get(0)),
						SUCCESS_MESSAGE);

			} catch (SchedulerException e) {

				// Logging.
				log.error("Quartz scheduler pausing process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}
		default:

			// Logging.
			log.warn("Unhandled scope in doStop() method!");
			break;
		}
		return QuartzSchedulerConsoleUtil.invalidScope(scope);
	}

	/**
	 * Starts or resumes jobs.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 15:07:16
	 * @param scope
	 * @param jobs
	 * @return
	 */
	private OxalisQuartzCommandResult doStart(OxalisQuartzCommandScopeEnum scope, List<OxalisQuartzJobKey> jobs) {
		final OxalisQuartzCommandActionEnum action = OxalisQuartzCommandActionEnum.START;
		switch (scope) {
		case ALL_JOBS:
			try {

				// Check if scheduler is started.
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}

				// Start all jobs.
				scheduler().resumeAll();
				log.info("Quartz scheduler started/resumed from NoTI-ER");

				// Add a detail for every job key.
				List<OxalisQuartzCommandResultDetails> detailsList = new ArrayList<>();
				for (JobKey jk : scheduler().getJobKeys(GroupMatcher.anyJobGroup())) {
					OxalisQuartzJobKey oxalisJobKey = new OxalisQuartzJobKey();
					oxalisJobKey.setJobGroup(jk.getGroup());
					oxalisJobKey.setJobName(jk.getName());
					detailsList.add(OxalisQuartzCommandResultDetails.ofAliveJob(oxalisJobKey));
				}

				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, detailsList, SUCCESS_MESSAGE);
			} catch (SchedulerException e) {

				// Logging.
				log.error("Quartz scheduler starting process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}

		case JOB_LIST:
			try {

				// Check if scheduler is started.
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}

				List<OxalisQuartzCommandResultDetails> detailsList = new ArrayList<>();

				// For every given job try to start it.
				for (OxalisQuartzJobKey job : jobs) {
					try {
						scheduler().resumeJob(new JobKey(job.getJobName(), job.getJobGroup()));
						log.info("{} job of group {} resumed successfully", job.getJobName(), job.getJobGroup());
						detailsList.add(OxalisQuartzCommandResultDetails.ofAliveJob(job));
					} catch (SchedulerException e) {
						log.error("{} job of group {} did not resume as expected", job.getJobName(), job.getJobGroup());
						detailsList.add(OxalisQuartzCommandResultDetails.ofDeadJob(job));
					}
				}
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, detailsList, SUCCESS_MESSAGE);

			} catch (SchedulerException e) {

				// Logging.
				log.error("Quartz JOB_LIST starting process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}

		case SINGLE_JOB:
			try {

				// Check if scheduler is started.
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}

				// Start single given job.
				scheduler().resumeJob(new JobKey(jobs.get(0).getJobName(), jobs.get(0).getJobGroup()));
				log.info("{} job of group {} resumed successfully", jobs.get(0).getJobName(), jobs.get(0).getJobGroup());
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, OxalisQuartzCommandResultDetails.ofAliveJob(jobs.get(0)),
						SUCCESS_MESSAGE);

			} catch (SchedulerException e) {

				// Logging.
				log.error("Quartz SINGLE_JOB starting process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}

		default:

			// Logging.
			log.warn("Unhandled scope in doStart() method!");
			break;
		}

		return QuartzSchedulerConsoleUtil.invalidScope(scope);
	}

	/**
	 * Executes a "get", retrieving all jobs status.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 15:03:43
	 * @param scope
	 * @param jobNames
	 * @return
	 * @throws SchedulerException
	 */
	private OxalisQuartzCommandResult doView(OxalisQuartzCommandScopeEnum scope, List<OxalisQuartzJobKey> jobs) {
		if (scope == null) {
			return QuartzSchedulerConsoleUtil.invalidScope(scope);
		}
		OxalisQuartzCommandActionEnum action = OxalisQuartzCommandActionEnum.VIEW;
		try {
			// If scheduler is not started return bad outcome.
			if (!scheduler().isStarted()) {
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
			}

			// Prepare Oxalis result.
			List<OxalisQuartzCommandResultDetails> detailsList = new ArrayList<>();

			// Get job groups list and iterator.
			List<String> jobGroups = scheduler().getJobGroupNames();
			Iterator<String> jobGroupsIterator = jobGroups.iterator();

			// Iterate trough jobs groups.
			while (jobGroupsIterator.hasNext()) {

				// Retrieve all job keys related to the given job group and get its iterator.
				String jobGroup = jobGroupsIterator.next();
				Set<JobKey> jobKeys = scheduler().getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
				Iterator<JobKey> jobKeysIterator = jobKeys.iterator();

				// Iterate through job jeys.
				while (jobKeysIterator.hasNext()) {

					// Retrieve all triggers related to the given job.
					JobKey jobKey = jobKeysIterator.next();

					// Parse JobKey in OxalisQuartzJobKey.
					OxalisQuartzJobKey oxalisJobKey = new OxalisQuartzJobKey();
					oxalisJobKey.setJobGroup(jobKey.getGroup());
					oxalisJobKey.setJobName(jobKey.getName());

					// If users gave me jobs I have to check that the given list contains the
					// retrieved job.
					if (((scope.equals(OxalisQuartzCommandScopeEnum.SINGLE_JOB) || scope.equals(OxalisQuartzCommandScopeEnum.JOB_LIST)) && jobs != null
							&& jobs.contains(oxalisJobKey)) || scope.equals(OxalisQuartzCommandScopeEnum.ALL_JOBS)) {

						// Retrieve trigger.
						List<? extends Trigger> triggers = scheduler().getTriggersOfJob(jobKey);
						Iterator<? extends Trigger> triggersIterator = triggers.iterator();

						// Iterate through job triggers.
						while (triggersIterator.hasNext()) {

							// Retrieve trigger state.
							Trigger trigger = triggersIterator.next();
							TriggerState state = scheduler().getTriggerState(trigger.getKey());

							// Add detail to list based on trigger state.
							switch (state) {
							case NORMAL:
							case COMPLETE:
								detailsList.add(OxalisQuartzCommandResultDetails.ofAliveJob(oxalisJobKey));
								break;
							case BLOCKED:
							case ERROR:
							case NONE:
								detailsList.add(OxalisQuartzCommandResultDetails.ofDeadJob(oxalisJobKey));
								break;
							case PAUSED:
								detailsList.add(OxalisQuartzCommandResultDetails.ofStandbyJob(oxalisJobKey));
								break;
							}
						}
					}
				}
			}

			// Logging.
			log.info("Successfully retrieved {} jobs status", detailsList.size());
			return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, detailsList, SUCCESS_MESSAGE);

		} catch (SchedulerException e) {

			// Logging.
			log.error("Quartz ALL_JOBS viewing process failed!");
			return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
		}
	}

	/**
	 * Retrieves Scheduler of Quartz.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 15:05:01
	 * @return
	 */
	private Scheduler scheduler() {
		return quartz.getScheduler();
	}

}
