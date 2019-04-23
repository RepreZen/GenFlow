/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;

/**
 * SwaggerParser is accepts specs that don't specify a type in a schema, which
 * is incompatible with JSON Schema spec and makes it easy to trip up on such
 * schemas in code that makes use of the parser output. Here we remove this
 * trap.
 */
public class SwaggerTypeFixer {

	public static void fixTypes(Swagger swagger) {
		SwaggerWalker.walk(swagger, new SwaggerWalker.Callbacks() {
			@Override
			public void objectModel(ModelImpl objectModel) {
				if (objectModel.getType() == null) {
					objectModel.setType("object");
				}
			}
		});
	}
}
