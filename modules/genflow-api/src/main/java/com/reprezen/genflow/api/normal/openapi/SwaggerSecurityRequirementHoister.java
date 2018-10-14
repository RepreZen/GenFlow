/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.util.List;
import java.util.Map;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;

/**
 * Propagate API-level (i.e. top-level) security requirements down to any
 * operation that doesn't already declare a security requirement
 */
public class SwaggerSecurityRequirementHoister {

	public static void hoist(final Swagger model) {
		List<SecurityRequirement> modelSec = model.getSecurity();
		if (modelSec == null || modelSec.isEmpty()) {
			return; // nothing to hoist
		}
		SwaggerWalker.walk(model, new SwaggerWalker.Callbacks() {
			@Override
			public void operation(HttpMethod httpMethod, Operation operation) {
				if (operation.getSecurity() == null) {
					for (SecurityRequirement secReq : modelSec) {
						Map<String, List<String>> requirements = secReq.getRequirements();
						for (String reqName : requirements.keySet()) {
							operation.addSecurity(reqName, requirements.get(reqName));
						}
					}
				}
			}
		});
	}
}
