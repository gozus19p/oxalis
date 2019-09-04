package it.eng.intercenter.oxalis.integration.dto;

import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisJobStatusEnum;

/**
 * @author Manuel Gozzi
 * @date 20 ago 2019
 * @time 15:44:05
 */
public class OxalisQuartzCommandResultDetails {

	private final OxalisQuartzJobKey job;
	private final OxalisJobStatusEnum jobStatus;

	private OxalisQuartzCommandResultDetails(final OxalisQuartzJobKey job, final OxalisJobStatusEnum jobStatus) {
		this.job = job;
		this.jobStatus = jobStatus;
	}

	public static OxalisQuartzCommandResultDetails ofAliveJob(final OxalisQuartzJobKey job) {
		return new OxalisQuartzCommandResultDetails(job, OxalisJobStatusEnum.ALIVE);
	}

	public static OxalisQuartzCommandResultDetails ofStandbyJob(final OxalisQuartzJobKey job) {
		return new OxalisQuartzCommandResultDetails(job, OxalisJobStatusEnum.STANDBY);
	}

	public static OxalisQuartzCommandResultDetails ofDeadJob(final OxalisQuartzJobKey job) {
		return new OxalisQuartzCommandResultDetails(job, OxalisJobStatusEnum.DEAD);
	}

	public OxalisQuartzJobKey getJob() {
		return job;
	}

	public OxalisJobStatusEnum getJobStatus() {
		return jobStatus;
	}

}
