package it.eng.intercenter.oxalis.integration.dto.enumerator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel Gozzi
 * @date 20 ago 2019
 * @time 10:23:28
 */
public enum OxalisQuartzCommandActionEnum {

	START, STOP, VIEW;

	public static List<String> valuesNamesAsStringList() {
		List<String> valuesAsStringList = new ArrayList<>();
		for (OxalisQuartzCommandActionEnum string : values()) {
			valuesAsStringList.add(string.name());
		}
		return valuesAsStringList;
	}

}
