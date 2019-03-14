package com.reprezen.genflow.openapi.generator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.openapitools.codegen.CodegenConfig;

import com.reprezen.genflow.common.codegen.GenModuleDiscovery;
import com.reprezen.genflow.common.codegen.GenModuleWrapper;

public class OagCodegenModuleDiscovery extends GenModuleDiscovery<CodegenConfig> {

	@Override
	protected String getLibraryVersion() {
		return CodegenConfig.class.getPackage().getImplementationVersion();
	}

	@Override
	protected GenModuleWrapper<CodegenConfig> getDummyWrapper() {
		return OagModuleWrapper.getDummyInstance();
	}

	@Override
	protected GenModuleWrapper<CodegenConfig> wrap(Object configObject) {
		return new OagModuleWrapper((CodegenConfig) configObject);
	}

	public static void main(String[] args) throws MalformedURLException, URISyntaxException, IOException {
		new OagCodegenModuleDiscovery().runDiscovery(args);
	}
}
