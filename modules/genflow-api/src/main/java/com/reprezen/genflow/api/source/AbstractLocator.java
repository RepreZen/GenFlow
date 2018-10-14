/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.source;

import com.reprezen.genflow.api.GenerationException;

public abstract class AbstractLocator<RootType> implements ILocator<RootType> {

	@Override
	public final <T> T dereference(String locator, Class<T> expectedType) throws GenerationException {
		Object result = dereference(locator);
		if (expectedType.isAssignableFrom(result.getClass())) {
			@SuppressWarnings("unchecked")
			T tResult = (T) result;
			return tResult;
		} else {
			throw new GenerationException("Locator resolved to object of incorrect type; expected "
					+ expectedType.getClass() + ", got " + result.getClass());
		}
	}

}
