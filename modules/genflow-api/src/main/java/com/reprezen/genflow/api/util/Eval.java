/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.util;

import java.util.HashMap;
import java.util.Map;

import org.mvel2.MVEL;
import org.mvel2.templates.TemplateRuntime;

public class Eval {

	public static class VarName {
		private final String varName;

		public VarName(String varName) {
			this.varName = varName;
		}

		public String getVarName() {
			return varName;
		}
	}

	// prevent instantiation
	private Eval() {
	}

	public static Object substEval(String expr, Map<String, Object> parameters, Object... boundObjects) {
		return substEval(expr, getContext(parameters, boundObjects));
	}

	public static Object substEval(String expr, Context context) {
		return eval(subst(expr, context), context);
	}

	public static String subst(String expr, Map<String, Object> parameters, Object... boundObjects) {
		return subst(expr, getContext(parameters, boundObjects));
	}

	public static String subst(String expr, Context context) {
		return (String) TemplateRuntime.eval(expr, null, context);
	}

	public static Object eval(String expr, Map<String, Object> parameters, Object... boundObjects) {
		return eval(expr, getContext(parameters, boundObjects));
	}

	public static Object eval(String expr, Context context) {
		return MVEL.eval(expr, context);
	}

	public static Context getContext(Map<String, Object> parameters, Object... boundObjects) {
		Context context = new Context(parameters);
		String nextName = null;
		for (Object boundObject : boundObjects) {
			if (boundObject == null) {
				nextName = null;
				continue;
			} else if (nextName != null) {
				context.put(nextName, boundObject);
				nextName = null;
			} else if (boundObject instanceof VarName) {
				nextName = ((VarName) boundObject).getVarName();
			} else {
				addWithTypeName(context, boundObject);
			}
		}
		return context;
	}

	private static void addWithTypeName(Map<String, Object> evalContext, Object value) {
		String varName = Strings.toFirstLower(value.getClass().getSimpleName()) + "_";
		evalContext.put(varName, value);
	}

	public static class Context extends HashMap<String, Object> {
		private static final long serialVersionUID = -4241880437070520079L;

		public Context(Map<String, Object> initial) {
			super(initial);
		}
	}
}
