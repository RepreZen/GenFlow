/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template.builders;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class BuilderUtil {
	public static Optional<Class<?>> getClass(String className) {
		try {
			return Optional.<Class<?>>of(Class.forName(className));
		} catch (ClassNotFoundException e) {
			try {
				return Optional.<Class<?>>of(Thread.currentThread().getContextClassLoader().loadClass(className));
			} catch (ClassNotFoundException e1) {
				return Optional.empty();
			}
		}
	}

	public static Optional<Object> getInstance(String className) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			try {
				clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
			} catch (ClassNotFoundException e1) {
			}
		}
		Object instance = null;
		if (clazz != null) {
			try {
				instance = clazz.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			}
		}
		return instance != null ? Optional.of(instance) : Optional.empty();
	}

	public static String simpleName(Object o) {
		return o.getClass().getSimpleName();
	}
}
