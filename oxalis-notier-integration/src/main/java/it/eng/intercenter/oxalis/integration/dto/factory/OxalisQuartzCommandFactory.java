package it.eng.intercenter.oxalis.integration.dto.factory;

import java.util.ArrayList;
import java.util.List;

import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommand;
import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzJobKey;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandActionEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandScopeEnum;

/**
 * @author Manuel Gozzi
 * @date 4 set 2019
 * @time 10:32:48
 */
public class OxalisQuartzCommandFactory {

	private OxalisQuartzCommandActionEnum action;
	private OxalisQuartzCommandScopeEnum scope;
	private List<OxalisQuartzJobKey> jobKeys;

	private OxalisQuartzCommandFactory() {
	}

	public static OxalisQuartzCommandFactory prepare() {
		return new OxalisQuartzCommandFactory();
	}

	public OxalisQuartzCommandFactory asStop() {
		action = OxalisQuartzCommandActionEnum.STOP;
		return this;
	}

	public OxalisQuartzCommandFactory asStart() {
		action = OxalisQuartzCommandActionEnum.START;
		return this;
	}

	public OxalisQuartzCommandFactory asView() {
		action = OxalisQuartzCommandActionEnum.VIEW;
		return this;
	}

	public OxalisQuartzCommandFactory overAllJobs() {
		scope = OxalisQuartzCommandScopeEnum.ALL_JOBS;
		return this;
	}

	public OxalisQuartzCommandFactory overSingleJob(OxalisQuartzJobKey job) {
		jobKeys = new ArrayList<OxalisQuartzJobKey>();
		jobKeys.add(job);
		scope = OxalisQuartzCommandScopeEnum.SINGLE_JOB;
		return this;
	}

	public OxalisQuartzCommandFactory overTheseJobs(List<OxalisQuartzJobKey> jobs) {
		jobKeys = new ArrayList<OxalisQuartzJobKey>();
		jobKeys.addAll(jobs);
		scope = OxalisQuartzCommandScopeEnum.JOB_LIST;
		return this;
	}

	public OxalisQuartzCommand build() {
		OxalisQuartzCommand command = new OxalisQuartzCommand();
		command.setAction(action);
		command.setJobKeys(jobKeys);
		command.setScope(scope);
		return command;
	}

}
