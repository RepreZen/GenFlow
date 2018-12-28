/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Info;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Parameter;
import com.reprezen.genflow.common.codegen.ModuleWrapper.ScgModuleWrapper;

import io.swagger.codegen.CodegenConfig;

public class BuiltinSwaggerCodegenGenTemplate extends SwaggerCodegenGenTemplate {

	private final Info info;

	public BuiltinSwaggerCodegenGenTemplate(Class<? extends CodegenConfig> codegenClass, Info info) {
		super(codegenClass, info);
		this.info = info;
	}

	@Override
	public String getName() {
		return getPreferredName(codegenClass, info);
	}

	@Override
	public String getId() {
		return getClass().getPackage().getName() + "." + codegenClass.getSimpleName();
	}

	@Override
	public void configure() throws GenerationException {
		if (info != null) {
			for (Parameter param : info.getParameters()) {
				define(parameter().named(param.getName()).required(param.isRequired())
						.withDescription(param.getDescription()).withDefault(param.getDefaultValue()));
			}
		}
		super.configure();
	}

	public static String getPreferredName(Class<? extends CodegenConfig> codegenClass, Info info) {
		if (info != null) {
			if (info.getDisplayName() != null) {
				return info.getDisplayName().trim();
			} else if (info.getDerivedDisplayName() != null) {
				return info.getDerivedDisplayName().trim();
			}
			// metadata is deficient (shouldn't happen) ... do a discovery-like name
			// generation
		}
		try {
			return new ScgModuleWrapper(codegenClass.newInstance()).getDerivedName();
		} catch (InstantiationException | IllegalAccessException e) {
			return "(unknown)";
		}
	}
}
