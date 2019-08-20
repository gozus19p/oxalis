package it.eng.intercenter.oxalis.integration.dto;

import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisJobStatusEnum;

/**
 * @author Manuel Gozzi
 * @date 20 ago 2019
 * @time 15:44:05
 */
public class OxalisQuartzCommandResultDetails {

	private final String jobName;
	private final OxalisJobStatusEnum jobStatus;

	private OxalisQuartzCommandResultDetails(final String jobName, final OxalisJobStatusEnum jobStatus) {
		this.jobName = jobName;
		this.jobStatus = jobStatus;
	}

	public static OxalisQuartzCommandResultDetails ofAliveJob(final String jobName) {
		return new OxalisQuartzCommandResultDetails(jobName, OxalisJobStatusEnum.ALIVE);
	}

	public static OxalisQuartzCommandResultDetails ofStandbyJob(final String jobName) {
		return new OxalisQuartzCommandResultDetails(jobName, OxalisJobStatusEnum.STANDBY);
	}

	public static OxalisQuartzCommandResultDetails ofDeadJob(final String jobName) {
		return new OxalisQuartzCommandResultDetails(jobName, OxalisJobStatusEnum.DEAD);
	}

	public String getJobName() {
		return jobName;
	}

	public OxalisJobStatusEnum getJobStatus() {
		return jobStatus;
	}

}
