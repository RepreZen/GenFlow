/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplate;

public abstract class OpenApiGenTemplate extends GenTemplate<OpenApiDocument> {

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return OpenApiDocument.class;
	}

	public void defineOpenApiSource() throws GenerationException {
		define(primarySource().ofType(OpenApiSource.class));
	}
}
