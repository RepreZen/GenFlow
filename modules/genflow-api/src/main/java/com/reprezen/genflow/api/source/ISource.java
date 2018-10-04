/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.source;

import java.io.File;

import com.reprezen.genflow.api.GenerationException;

public interface ISource<S> {
	S load(File inFile) throws GenerationException;

	S load() throws GenerationException;

	File getInputFile();

	void setInputFile(File inputFile);

	boolean hasInputFile();

	Iterable<Object> extractByType(S sourceValue, Class<?> itemClass) throws GenerationException;

	Class<?> getValueType() throws GenerationException;

	ILocator<S> getLocator(S soureValue);

	String getLabel();
}
