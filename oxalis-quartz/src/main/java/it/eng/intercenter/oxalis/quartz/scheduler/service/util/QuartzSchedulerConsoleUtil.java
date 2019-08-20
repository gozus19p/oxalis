package it.eng.intercenter.oxalis.quartz.scheduler.service.util;

import java.util.List;

import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommandResult;
import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommandResultDetails;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandActionEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandOutcomeEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandScopeEnum;

/**
 * @author Manuel Gozzi
 * @date 20 ago 2019
 * @time 15:36:27
 */
public class QuartzSchedulerConsoleUtil {

	/**
	 * Return an OxalisQuartzCommandResult that communicates an invalid given scope.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 15:05:21
	 * @param scope
	 * @return
	 */
	public static OxalisQuartzCommandResult invalidScope(OxalisQuartzCommandScopeEnum scope) {
		return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.KO, (List<OxalisQuartzCommandResultDetails>) null,
				"OxalisQuartzCommand scope not supported: " + scope.name());
	}

	/**
	 * Return an OxalisQuartzCommandResult that communicates an invalid given
	 * action.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 15:38:09
	 * @param action
	 * @return
	 */
	public static OxalisQuartzCommandResult invalidAction(OxalisQuartzCommandActionEnum action) {
		return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.KO, (List<OxalisQuartzCommandResultDetails>) null,
				"OxalisQuartzCommand action not supported: " + action.name());
	}

	/**
	 * Return an OxalisQuartzCommandResult that communicates an invalid Scheduler
	 * status.
	 *
	 * @author Manuel Gozzi
	 * @date 20 ago 2019
	 * @time 15:05:43
	 * @param e
	 * @param action
	 * @return
	 */
	public static OxalisQuartzCommandResult invalidSchedulerStatus(Exception e, OxalisQuartzCommandActionEnum action) {
		String message = action.equals(OxalisQuartzCommandActionEnum.STOP) || action.equals(OxalisQuartzCommandActionEnum.VIEW) ? "Scheduler is not started "
				: "Scheduler status not valid for action: " + action.name() + " ";
		message += "; exception message: " + e.getMessage();
		return new OxalisQuartzCommandResult(OxalisQuartzCommandOutcomeEnum.KO, (List<OxalisQuartzCommandResultDetails>) null, message);
	}

}
