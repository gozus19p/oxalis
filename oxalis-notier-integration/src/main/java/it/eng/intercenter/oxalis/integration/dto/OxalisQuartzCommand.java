package it.eng.intercenter.oxalis.integration.dto;

import java.util.Arrays;
import java.util.List;

import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandActionEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandScopeEnum;

/**
 * @author Manuel Gozzi
 * @date 20 ago 2019
 * @time 10:32:32
 */
public class OxalisQuartzCommand {

	private final OxalisQuartzCommandActionEnum action;
	private final OxalisQuartzCommandScopeEnum scope;
	private final List<String> jobNames;

	private OxalisQuartzCommand(OxalisQuartzCommandActionEnum action, OxalisQuartzCommandScopeEnum scope, List<String> jobNames) {
		this.action = action;
		this.scope = scope;
		this.jobNames = jobNames;
	}

	/**
	 * Command that stops a single Quartz job.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 10:38:40
	 * @param jobName is the job name
	 * @return
	 */
	public static OxalisQuartzCommand stopSingleJob(String jobName) {
		return new OxalisQuartzCommand(OxalisQuartzCommandActionEnum.STOP, OxalisQuartzCommandScopeEnum.SINGLE_JOB, Arrays.asList(new String[] { jobName }));
	}

	/**
	 * Command that starts a single job.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 10:40:37
	 * @param jobName
	 * @return
	 */
	public static OxalisQuartzCommand startSingleJob(String jobName) {
		return new OxalisQuartzCommand(OxalisQuartzCommandActionEnum.START, OxalisQuartzCommandScopeEnum.SINGLE_JOB, Arrays.asList(new String[] { jobName }));
	}

	/**
	 * Command that stops a list of jobs.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 10:40:55
	 * @param jobNames
	 * @return
	 */
	public static OxalisQuartzCommand stopJobs(List<String> jobNames) {
		return new OxalisQuartzCommand(OxalisQuartzCommandActionEnum.STOP, OxalisQuartzCommandScopeEnum.JOB_LIST, jobNames);
	}

	/**
	 * Command that starts a list of jobs.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 10:41:12
	 * @param jobNames
	 * @return
	 */
	public static OxalisQuartzCommand startJobs(List<String> jobNames) {
		return new OxalisQuartzCommand(OxalisQuartzCommandActionEnum.START, OxalisQuartzCommandScopeEnum.JOB_LIST, jobNames);
	}

	/**
	 * Command that stops a list of jobs (array version).
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 10:41:27
	 * @param jobNames
	 * @return
	 */
	public static OxalisQuartzCommand stopJobs(String[] jobNames) {
		return new OxalisQuartzCommand(OxalisQuartzCommandActionEnum.STOP, OxalisQuartzCommandScopeEnum.JOB_LIST, Arrays.asList(jobNames));
	}

	/**
	 * Command that starts a list of jobs (array version).
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 10:41:52
	 * @param jobNames
	 * @return
	 */
	public static OxalisQuartzCommand startJobs(String[] jobNames) {
		return new OxalisQuartzCommand(OxalisQuartzCommandActionEnum.START, OxalisQuartzCommandScopeEnum.JOB_LIST, Arrays.asList(jobNames));
	}

	/**
	 * Command that stops all jobs.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 10:42:05
	 * @return
	 */
	public static OxalisQuartzCommand stopAllJobs() {
		return new OxalisQuartzCommand(OxalisQuartzCommandActionEnum.STOP, OxalisQuartzCommandScopeEnum.ALL_JOBS, null);
	}

	/**
	 * Command that starts all jobs.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 10:42:15
	 * @return
	 */
	public static OxalisQuartzCommand startAllJobs() {
		return new OxalisQuartzCommand(OxalisQuartzCommandActionEnum.START, OxalisQuartzCommandScopeEnum.ALL_JOBS, null);
	}

	/**
	 * Command that retrieves a single job status.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 10:42:24
	 * @param jobName
	 * @return
	 */
	public static OxalisQuartzCommand viewSingleJobStatus(String jobName) {
		return new OxalisQuartzCommand(OxalisQuartzCommandActionEnum.VIEW, OxalisQuartzCommandScopeEnum.SINGLE_JOB, Arrays.asList(new String[] { jobName }));
	}

	/**
	 * Command that retrieves all jobs status.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 10:42:35
	 * @return
	 */
	public static OxalisQuartzCommand viewAllJobsStatus() {
		return new OxalisQuartzCommand(OxalisQuartzCommandActionEnum.VIEW, OxalisQuartzCommandScopeEnum.ALL_JOBS, null);
	}

	public OxalisQuartzCommandActionEnum getAction() {
		return action;
	}

	public OxalisQuartzCommandScopeEnum getScope() {
		return scope;
	}

	public List<String> getJobNames() {
		return jobNames;
	}

}
