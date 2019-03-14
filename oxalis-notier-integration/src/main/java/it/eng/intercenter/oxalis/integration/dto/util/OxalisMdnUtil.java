package it.eng.intercenter.oxalis.integration.dto.util;

import it.eng.intercenter.oxalis.integration.dto.OxalisMdn;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisStatusEnum;

/**
 * @author Manuel Gozzi
 */
public class OxalisMdnUtil {

	public static OxalisMdn createPositiveMdn(String documentUrn, String message) {
		return new OxalisMdn(documentUrn, OxalisStatusEnum.OK, message);
	}
	
	public static OxalisMdn createNegativeMdn(String documentUrn, String message) {
		return new OxalisMdn(documentUrn, OxalisStatusEnum.KO, message);
	}
	
	public static boolean isPositive(OxalisMdn mdn) {
		return mdn.getStatus().equals(OxalisStatusEnum.OK);
	}
	
}
