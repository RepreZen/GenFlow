/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi3;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

public abstract class OpenApi3GenTemplate extends GenTemplate<OpenApi3> {

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return OpenApi3.class;
	}

	public void defineOpenApi3Source() throws GenerationException {
		define(primarySource().ofType(OpenApi3Source.class));
	}

}
