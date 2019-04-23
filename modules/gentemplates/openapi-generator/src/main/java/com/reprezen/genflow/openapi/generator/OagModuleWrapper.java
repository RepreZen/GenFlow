package com.reprezen.genflow.openapi.generator;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reprezen.genflow.common.codegen.GenModuleWrapper;

public class OagModuleWrapper extends GenModuleWrapper<org.openapitools.codegen.CodegenConfig> {

	private Logger logger = LoggerFactory.getLogger(OagModuleWrapper.class);

	public OagModuleWrapper(org.openapitools.codegen.CodegenConfig config) {
		super(config);
	}

	public OagModuleWrapper(Class<org.openapitools.codegen.CodegenConfig> configClass) {
		super(configClass);
	}

	public static GenModuleWrapper<org.openapitools.codegen.CodegenConfig> getDummyInstance() {
		return new OagModuleWrapper(org.openapitools.codegen.CodegenConfig.class);
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
		return config.cliOptions().stream().map(cliOpt -> new OagCliOptWrapper(cliOpt)).collect(Collectors.toList());
	}

	@Override
	public Enum<?> typeNamed(String name) {
		return org.openapitools.codegen.CodegenType.valueOf(name);
	}

	@Override
	public Logger getLogger() {
		return logger;
	}
}