/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;

/**
 * Copy top-level "consumes" and "produces" lists to operations that don't
 * define their own
 */
public class SwaggerMediaTypesHoister {

	public static void hoist(final Swagger model) {
		SwaggerWalker.walk(model, new SwaggerWalker.Callbacks() {
			@Override
			public void operation(HttpMethod httpMethod, Operation operation) {
				if (operation.getConsumes() == null) {
					operation.setConsumes(model.getConsumes());
				}
				if (operation.getProduces() == null) {
					operation.setProduces(model.getProduces());
				}
			}
		});
	}
}
