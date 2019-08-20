package it.eng.intercenter.oxalis.integration.dto;

import java.util.Arrays;
import java.util.List;

import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandOutcomeEnum;

/**
 * Response relativa all'esecuzione del comando inviato allo schedulatore Quartz
 * di Oxalis da NoTI-ER.
 *
 * @author Manuel Gozzi
 * @date 20 ago 2019
 * @time 10:25:43
 */
public class OxalisQuartzCommandResult {

	private final OxalisQuartzCommandOutcomeEnum outcome;
	private final List<OxalisQuartzCommandResultDetails> details;
	private final String message;

	public OxalisQuartzCommandResult(final OxalisQuartzCommandOutcomeEnum outcome, final List<OxalisQuartzCommandResultDetails> details, String message) {
		this.outcome = outcome;
		this.details = details;
		this.message = message;
	}

	public OxalisQuartzCommandResult(final OxalisQuartzCommandOutcomeEnum outcome, final OxalisQuartzCommandResultDetails[] details, String message) {
		this(outcome, Arrays.asList(details), message);
	}

	public OxalisQuartzCommandResult(final OxalisQuartzCommandOutcomeEnum outcome, final OxalisQuartzCommandResultDetails details, String message) {
		this(outcome, Arrays.asList(new OxalisQuartzCommandResultDetails[] { details }), message);
	}

	public OxalisQuartzCommandOutcomeEnum getOutcome() {
		return outcome;
	}

	public List<OxalisQuartzCommandResultDetails> getDetails() {
		return details;
	}

	public String getMessage() {
		return message;
	}

}
