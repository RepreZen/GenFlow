/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi;

import java.io.File;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.outputitem.AbstractOutputItem;

public abstract class OpenApiOutputItem extends AbstractOutputItem<OpenApiDocument, OpenApiDocument> {

	@Override
	public String generate(OpenApiDocument primarySource, OpenApiDocument inputItem) throws GenerationException {
		assert (primarySource == inputItem);
		return generate(primarySource);
	}

	public abstract String generate(OpenApiDocument model) throws GenerationException;

	@Override
	public File getOutputFile(OpenApiDocument sourceValue, OpenApiDocument itemValue) {
		return getOutputFile(sourceValue);
	}

	private File getOutputFile(OpenApiDocument sourceValue) {
		return null;
	}

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return OpenApiDocument.class;
	}

	@Override
	public Class<?> getItemType() throws GenerationException {
		return OpenApiDocument.class;
	}
}
