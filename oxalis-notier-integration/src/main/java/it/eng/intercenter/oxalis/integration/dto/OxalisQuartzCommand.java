package it.eng.intercenter.oxalis.integration.dto;

import java.util.List;

import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandActionEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandScopeEnum;

/**
 * @author Manuel Gozzi
 * @date 20 ago 2019
 * @time 10:32:32
 */
public class OxalisQuartzCommand {

	private OxalisQuartzCommandActionEnum action;
	private OxalisQuartzCommandScopeEnum scope;
	private List<OxalisQuartzJobKey> jobKeys;

	public List<OxalisQuartzJobKey> getJobKeys() {
		return jobKeys;
	}

	public void setJobKeys(List<OxalisQuartzJobKey> jobKeys) {
		this.jobKeys = jobKeys;
	}

	public void setAction(OxalisQuartzCommandActionEnum action) {
		this.action = action;
	}

	public void setScope(OxalisQuartzCommandScopeEnum scope) {
		this.scope = scope;
	}

	public OxalisQuartzCommandActionEnum getAction() {
		return action;
	}

	public OxalisQuartzCommandScopeEnum getScope() {
		return scope;
	}

	public List<OxalisQuartzJobKey> getJobNames() {
		return jobKeys;
	}

}
