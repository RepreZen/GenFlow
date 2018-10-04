/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.swagger;

import java.io.File;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.outputitem.AbstractOutputItem;

import io.swagger.models.Swagger;

public abstract class SwaggerOutputItem extends AbstractOutputItem<Swagger, Swagger> {

	@Override
	public final String generate(Swagger swagger, Swagger item) throws GenerationException {
		assert (swagger == item);
		return generate(swagger);
	}

	public abstract String generate(Swagger swagger) throws GenerationException;

	@Override
	public final File getOutputFile(Swagger swagger, Swagger item) {
		return getOutputFile(swagger);
	}

	public File getOutputFile(Swagger swagger) {
		return null;
	}

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return Swagger.class;
	}

	@Override
	public Class<?> getItemType() throws GenerationException {
		return Swagger.class;
	}

}
