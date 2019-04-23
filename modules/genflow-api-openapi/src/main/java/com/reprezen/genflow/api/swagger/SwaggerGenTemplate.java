/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.swagger;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplate;

import io.swagger.models.Swagger;

public abstract class SwaggerGenTemplate extends GenTemplate<Swagger> {

	public void defineSwaggerSource() throws GenerationException {
		defineSwaggerSource(true);
	}

	public void defineSwaggerSource(boolean primary) throws GenerationException {
		if (primary) {
			define(primarySource().ofType(SwaggerSource.class));
		} else {
			define(namedSource().named("swagger").ofType(SwaggerSource.class));
		}
	}

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return Swagger.class;
	}

}
