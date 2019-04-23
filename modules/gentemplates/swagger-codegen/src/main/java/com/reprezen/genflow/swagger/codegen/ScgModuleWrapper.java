package com.reprezen.genflow.swagger.codegen;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reprezen.genflow.common.codegen.GenModuleWrapper;

import io.swagger.codegen.CodegenConfig;

public class ScgModuleWrapper extends GenModuleWrapper<io.swagger.codegen.CodegenConfig> {

	private Logger logger = LoggerFactory.getLogger(ScgModuleWrapper.class);

	public ScgModuleWrapper(io.swagger.codegen.CodegenConfig config) {
		super(config);
	}

	public ScgModuleWrapper(Class<CodegenConfig> configClass) {
		super(configClass);
	}

	public static GenModuleWrapper<io.swagger.codegen.CodegenConfig> getDummyInstance() {
		return new ScgModuleWrapper(io.swagger.codegen.CodegenConfig.class);
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
		return config.cliOptions().stream().map(cliOpt -> new ScgCliOptWrapper(cliOpt)).collect(Collectors.toList());
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public Enum<?> typeNamed(String name) {
		return io.swagger.codegen.CodegenType.valueOf(name);
	}

}