package it.eng.intercenter.oxalis.integration.dto;

import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisJobStatusEnum;

/**
 * @author Manuel Gozzi
 * @date 20 ago 2019
 * @time 15:44:05
 */
public class OxalisQuartzCommandResultDetails {

	private final OxalisQuartzJobKey jobName;
	private final OxalisJobStatusEnum jobStatus;

	private OxalisQuartzCommandResultDetails(final OxalisQuartzJobKey jobName, final OxalisJobStatusEnum jobStatus) {
		this.jobName = jobName;
		this.jobStatus = jobStatus;
	}

	public static OxalisQuartzCommandResultDetails ofAliveJob(final OxalisQuartzJobKey jobName) {
		return new OxalisQuartzCommandResultDetails(jobName, OxalisJobStatusEnum.ALIVE);
	}

	public static OxalisQuartzCommandResultDetails ofStandbyJob(final OxalisQuartzJobKey jobName) {
		return new OxalisQuartzCommandResultDetails(jobName, OxalisJobStatusEnum.STANDBY);
	}

	public static OxalisQuartzCommandResultDetails ofDeadJob(final OxalisQuartzJobKey jobName) {
		return new OxalisQuartzCommandResultDetails(jobName, OxalisJobStatusEnum.DEAD);
	}

	public OxalisQuartzJobKey getJobName() {
		return jobName;
	}

	public OxalisJobStatusEnum getJobStatus() {
		return jobStatus;
	}

}
