package it.eng.intercenter.oxalis.quartz.scheduler.service;

import java.util.ArrayList;
import java.util.List;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommand;
import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommandResult;
import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommandResultDetails;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandActionEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandOutcomeEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandScopeEnum;
import it.eng.intercenter.oxalis.quartz.scheduler.Quartz;
import it.eng.intercenter.oxalis.quartz.scheduler.service.util.QuartzSchedulerConsoleUtil;
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
	Quartz quartz;

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
			return doStart(command.getScope(), command.getJobNames());
		case STOP:
			return doStop(command.getScope(), command.getJobNames());
		case VIEW:
			return doView(command.getScope(), command.getJobNames());
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
	 * @param jobNames
	 * @return
	 * @throws SchedulerException
	 */
	private OxalisQuartzCommandResult doStop(OxalisQuartzCommandScopeEnum scope, List<String> jobNames) {
		final OxalisQuartzCommandActionEnum action = OxalisQuartzCommandActionEnum.STOP;
		switch (scope) {
		case ALL_JOBS:
			try {
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}
				scheduler().pauseAll();
				log.info("All jobs paused successfully");

				List<OxalisQuartzCommandResultDetails> detailsList = new ArrayList<>();
				for (JobKey jk : scheduler().getJobKeys(GroupMatcher.anyJobGroup())) {
					detailsList.add(OxalisQuartzCommandResultDetails.ofDeadJob(jk.getName()));
				}
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, detailsList, SUCCESS_MESSAGE);

			} catch (SchedulerException e) {
				log.error("Quartz scheduler pausing process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}
		case JOB_LIST:
			try {
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}

				List<OxalisQuartzCommandResultDetails> detailsList = new ArrayList<>();
				for (String jobName : jobNames) {
					try {
						scheduler().pauseJob(new JobKey(jobName));
						log.info("{} job paused successfully", jobName);
						detailsList.add(OxalisQuartzCommandResultDetails.ofStandbyJob(jobName));
					} catch (SchedulerException e) {
						log.error("{} job did not pause successfully", jobName);
						detailsList.add(OxalisQuartzCommandResultDetails.ofAliveJob(jobName));
					}
				}
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, detailsList, SUCCESS_MESSAGE);

			} catch (SchedulerException e) {
				log.error("Quartz scheduler pausing process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}
		case SINGLE_JOB:
			try {
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}
				scheduler().pauseJob(new JobKey(jobNames.get(0)));
				log.info("{} job paused successfuly", jobNames.get(0));
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, OxalisQuartzCommandResultDetails.ofStandbyJob(jobNames.get(0)),
						SUCCESS_MESSAGE);

			} catch (SchedulerException e) {
				log.error("Quartz scheduler pausing process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}
		default:
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
	 * @param jobNames
	 * @return
	 */
	private OxalisQuartzCommandResult doStart(OxalisQuartzCommandScopeEnum scope, List<String> jobNames) {
		final OxalisQuartzCommandActionEnum action = OxalisQuartzCommandActionEnum.START;

		switch (scope) {

		case ALL_JOBS:
			try {
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}
				scheduler().resumeAll();
				log.info("Quartz scheduler started/resumed from NoTI-ER");

				List<OxalisQuartzCommandResultDetails> detailsList = new ArrayList<>();
				for (JobKey jk : scheduler().getJobKeys(GroupMatcher.anyJobGroup())) {
					detailsList.add(OxalisQuartzCommandResultDetails.ofAliveJob(jk.getName()));
				}
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, detailsList, SUCCESS_MESSAGE);

			} catch (SchedulerException e) {
				log.error("Quartz scheduler starting process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}

		case JOB_LIST:
			try {
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}

				List<OxalisQuartzCommandResultDetails> detailsList = new ArrayList<>();
				for (String jobName : jobNames) {
					try {
						scheduler().resumeJob(new JobKey(jobName));
						log.info("{} job resumed successfully", jobName);
						detailsList.add(OxalisQuartzCommandResultDetails.ofAliveJob(jobName));
					} catch (SchedulerException e) {
						log.error("{} job did not resume correctly", jobName);
						detailsList.add(OxalisQuartzCommandResultDetails.ofDeadJob(jobName));
					}
				}
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, detailsList, SUCCESS_MESSAGE);

			} catch (SchedulerException e) {
				log.error("Quartz JOB_LIST starting process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}

		case SINGLE_JOB:
			try {
				if (!scheduler().isStarted()) {
					return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(new SchedulerException("Scheduler is not started"), action);
				}
				scheduler().resumeJob(new JobKey(jobNames.get(0)));
				log.info("{} job resumed successfully", jobNames.get(0));
				return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.OK, OxalisQuartzCommandResultDetails.ofAliveJob(jobNames.get(0)),
						SUCCESS_MESSAGE);

			} catch (SchedulerException e) {
				log.error("Quartz SINGLE_JOB starting process failed!");
				return QuartzSchedulerConsoleUtil.invalidSchedulerStatus(e, action);
			}

		default:
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
	// TODO: to implement!
	private OxalisQuartzCommandResult doView(OxalisQuartzCommandScopeEnum scope, List<String> jobNames) {
//		final OxalisQuartzCommandActionEnum action = OxalisQuartzCommandActionEnum.VIEW;
		switch (scope) {
		case ALL_JOBS:
		case JOB_LIST:
		case SINGLE_JOB:
		default:
			log.warn("Unhandled scope in doView() method!");
			break;
		}
		return QuartzSchedulerConsoleUtil.invalidScope(scope);
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
