package com.reprezen.genflow.api.util;

public class Strings {

	public static String toFirstLower(String s) {
		return s != null && s.length() > 0 ? s.substring(0, 1).toLowerCase() + s.substring(1) : s;
	}
}
