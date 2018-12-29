package com.reprezen.genflow.swagger.codegen;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.reprezen.genflow.common.codegen.GenModuleDiscovery;
import com.reprezen.genflow.common.codegen.GenModuleWrapper;
import com.reprezen.genflow.common.codegen.GenModuleWrapper.ScgModuleWrapper;

import io.swagger.codegen.CodegenConfig;

public class ScgCodegenModuleDiscovery extends GenModuleDiscovery<CodegenConfig> {

	@Override
	protected String getLibraryVersion() {
		return io.swagger.codegen.CodegenConfig.class.getPackage().getImplementationVersion();
	}

	@Override
	protected GenModuleWrapper<CodegenConfig> getDummyWrapper() {
		return ScgModuleWrapper.getDummyInstance();
	}

	@Override
	protected GenModuleWrapper<CodegenConfig> wrap(Object configObject) {
		return new ScgModuleWrapper((io.swagger.codegen.CodegenConfig) configObject);
	}

	public static void main(String[] args) throws MalformedURLException, URISyntaxException, IOException {
		new ScgCodegenModuleDiscovery().runDiscovery(args);
	}
}
