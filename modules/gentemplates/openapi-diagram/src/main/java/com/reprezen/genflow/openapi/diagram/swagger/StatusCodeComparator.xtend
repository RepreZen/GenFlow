/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.swagger

import java.util.Comparator

/**
 * Natural ordering with exception of default response ('default') which is bigger than any normal response  
 * */
class StatusCodeComparator implements Comparator<String> {

	override compare(String s1, String s2) {
		val code1 = safeParse(s1, Integer.MAX_VALUE)
		val code2 = safeParse(s2, Integer.MAX_VALUE)
		return Integer.valueOf(code1).compareTo(code2)
	}

	static def int safeParse(String number, int orDefault) {
		if (number.nullOrEmpty || number == "default") {
			return orDefault
		}
		try {
			return Integer.parseInt(number)
		} catch (NumberFormatException e) {
			return orDefault
		}
	}

}
