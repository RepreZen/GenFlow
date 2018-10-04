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

public interface ILocator<RootType> {

	public <T> String locate(T item) throws GenerationException;

	public <T> T dereference(String locator, Class<T> expectedType) throws GenerationException;

	public Object dereference(String locator) throws GenerationException;
}
