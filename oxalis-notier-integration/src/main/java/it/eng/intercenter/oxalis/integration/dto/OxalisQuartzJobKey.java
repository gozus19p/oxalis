package it.eng.intercenter.oxalis.integration.dto;

/**
 * @author Manuel Gozzi
 * @date 4 set 2019
 * @time 10:30:39
 */
public class OxalisQuartzJobKey {

	private String jobName;
	private String jobGroup;

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobGroup() {
		return jobGroup;
	}

	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}

}
