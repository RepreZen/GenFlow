/*******************************************************************************
 * Copyright © 2013, 2018 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi3;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.AbstractDynamicGenerator;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

public abstract class OpenApi3DynamicGenerator extends AbstractDynamicGenerator<OpenApi3> {

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return OpenApi3.class;
	}

}
