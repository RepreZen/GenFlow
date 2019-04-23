/*******************************************************************************
 * Copyright © 2013, 2018 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.AbstractDynamicGenerator;
import com.reprezen.kaizen.oasparser.OpenApi;

public abstract class OpenApiDynamicGenerator extends AbstractDynamicGenerator<OpenApi<?>> {

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return OpenApi.class;
	}
}
