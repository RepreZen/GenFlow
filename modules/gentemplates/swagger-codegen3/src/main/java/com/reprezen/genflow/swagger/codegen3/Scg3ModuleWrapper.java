/*******************************************************************************
 * Copyright © 2013, 2019 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen3;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reprezen.genflow.common.codegen.GenModuleWrapper;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.CodegenType;

public class Scg3ModuleWrapper extends GenModuleWrapper<CodegenConfig> {

	private Logger logger = LoggerFactory.getLogger(Scg3ModuleWrapper.class);

	public Scg3ModuleWrapper(CodegenConfig config) {
		super(config);
	}

	public Scg3ModuleWrapper(Class<CodegenConfig> configClass) {
		super(configClass);
	}

	public static GenModuleWrapper<CodegenConfig> getDummyInstance() {
		return new Scg3ModuleWrapper(CodegenConfig.class);
	}

	@Override
	public Enum<?> getType() {
		return config.getTag();
	}

	@Override
	public GenericType getGenericType() {
		switch (config.getTag()) {
		case CLIENT:
			return GenericType.CLIENT;
		case SERVER:
			return GenericType.SERVER;
		case DOCUMENTATION:
			return GenericType.DOCUMENTATION;
		case CONFIG:
			return GenericType.CONFIG;
		case OTHER:
		default:
			return GenericType.OTHER;
		}
	}

	@Override
	public String getName() {
		return config.getName();
	}

	@Override
	public List<CliOptWrapper<?>> getClientOptions() {
		return config.cliOptions().stream().map(Scg3CliOptWrapper::new).collect(Collectors.toList());
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public Enum<?> typeNamed(String name) {
		return CodegenType.valueOf(name);
	}

}