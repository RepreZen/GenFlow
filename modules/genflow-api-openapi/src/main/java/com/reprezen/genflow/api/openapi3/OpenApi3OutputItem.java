/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi3;

import java.io.File;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.outputitem.AbstractOutputItem;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

public abstract class OpenApi3OutputItem extends AbstractOutputItem<OpenApi3, OpenApi3> {

	@Override
	public String generate(OpenApi3 primarySource, OpenApi3 inputItem) throws GenerationException {
		assert (primarySource == inputItem);
		return generate(primarySource);
	}

	public abstract String generate(OpenApi3 model) throws GenerationException;

	@Override
	public File getOutputFile(OpenApi3 sourceValue, OpenApi3 itemValue) {
		return getOutputFile(sourceValue);
	}

	private File getOutputFile(OpenApi3 sourceValue) {
		return null;
	}

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return OpenApi3.class;
	}

	@Override
	public Class<?> getItemType() throws GenerationException {
		return OpenApi3.class;
	}
}
