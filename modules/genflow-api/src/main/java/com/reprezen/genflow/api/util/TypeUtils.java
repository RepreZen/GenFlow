/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.reprezen.genflow.api.GenerationException;

public class TypeUtils {
	// prevent instantiation
	private TypeUtils() {
	}

	public static Class<?> getTypeParamClass(Class<?> clazz, Class<?> superClass, int position)
			throws GenerationException {
		Throwable t = null;
		boolean match = false;
		Type type = null;
		try {
			while (clazz != Object.class) {
				type = clazz.getGenericSuperclass();
				if (type instanceof ParameterizedType) {
					ParameterizedType ptype = (ParameterizedType) type;
					match = ptype.getRawType() == superClass;
					if (match) {
						break;
					}
				}
				clazz = clazz.getSuperclass();

			}
			if (match) {
				return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[position];
			}
		} catch (Exception e) {
			t = e;
		}
		if (type == Object.class) {
			throw new GenerationException("Class " + clazz + " is not a subtype of " + superClass);
		} else {
			throw new GenerationException("Failed to resolve type parameter", t);
		}
	}
}
