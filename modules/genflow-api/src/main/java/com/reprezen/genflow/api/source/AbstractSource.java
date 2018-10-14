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
import java.util.Collections;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.util.TypeUtils;

public abstract class AbstractSource<S> implements ISource<S> {

	protected File inputFile = null;

	public AbstractSource() {
	}

	public AbstractSource(File inputFile) {
		this.inputFile = inputFile;
	}

	@Override
	public File getInputFile() {
		return inputFile;
	}

	@Override
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	@Override
	public boolean hasInputFile() {
		return inputFile != null;
	}

	@Override
	public final S load() throws GenerationException {
		if (inputFile != null) {
			return load(inputFile);
		} else {
			throw new GenerationException("Cannot load because no input file has been specified");
		}
	}

	@Override
	public Iterable<Object> extractByType(S sourceValue, Class<?> itemClass) throws GenerationException {
		if (getValueType().isAssignableFrom(itemClass)) {
			return Collections.singletonList((Object) sourceValue);
		} else {
			return extractByNonSourceType(sourceValue, itemClass);
		}
	}

	protected Iterable<Object> extractByNonSourceType(S sourceValue, Class<?> itemClass) throws GenerationException {
		throw cantExtractException(itemClass);
	}

	protected GenerationException cantExtractException(Class<?> itemClass) {
		return new GenerationException("Cannot extract items of type " + itemClass);
	}

	@Override
	public Class<?> getValueType() throws GenerationException {
		return TypeUtils.getTypeParamClass(getClass(), AbstractSource.class, 0);
	}
}