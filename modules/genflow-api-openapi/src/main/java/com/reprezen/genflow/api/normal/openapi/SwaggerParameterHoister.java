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

import com.google.common.collect.Lists;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;

/**
 * Copy path-level parameters to all operations in the same path that do not
 * define an overriding parameter (same name and "in" value).
 * <p>
 * This assumes that reference parameters, both at path-level and
 * operation-level, have been resolved and replaced by their definitions. Any
 * remaining reference parameters are thus assumed to be unresolvable and are
 * left as-is.
 */
public class SwaggerParameterHoister {

	public static void hoist(Swagger model) {
		Map<String, Path> paths = model.getPaths();
		if (paths != null) {
			for (Path path : paths.values()) {
				hoistInPath(path);
			}
		}
	}

	private static void hoistInPath(Path path) {
		List<Parameter> parameters = path.getParameters();
		if (parameters != null) {
			for (Parameter parameter : parameters) {
				hoistParameter(parameter, path.getOperations());
			}
			parameters.clear();
		}
	}

	private static void hoistParameter(Parameter pathParameter, List<Operation> operations) {
		if (operations != null) {
			for (Operation operation : operations) {
				hoistParameter(pathParameter, operation);
			}
		}
	}

	private static void hoistParameter(Parameter pathParameter, Operation operation) {
		if (pathParameter instanceof RefParameter) {
			return; // can't hoist unresolvable ref parameter
		}
		List<Parameter> parameters = operation.getParameters();
		if (parameters != null) {
			for (Parameter parameter : parameters) {
				if (parameter instanceof RefParameter) {
					continue; // can't compare to unresolvable ref parameter
				}
				if (parameter.getName().equals(pathParameter.getName())
						&& parameter.getIn().equals(pathParameter.getIn())) {
					return;
				}
			}
		} else {
			parameters = Lists.newArrayList();
			operation.setParameters(parameters);
		}
		parameters.add(pathParameter);
	}
}
