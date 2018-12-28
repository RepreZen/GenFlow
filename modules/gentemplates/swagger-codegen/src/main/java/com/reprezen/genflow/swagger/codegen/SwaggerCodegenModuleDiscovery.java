package com.reprezen.genflow.swagger.codegen;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.reprezen.genflow.swagger.codegen.ModuleWrapper.ScgModuleWrapper;

public class SwaggerCodegenModuleDiscovery extends GenModuleDiscovery {

	@Override
	protected String getLibraryVersion() {
		return io.swagger.codegen.CodegenConfig.class.getPackage().getImplementationVersion();
	}

	@Override
	protected ModuleWrapper getDummyWrapper() {
		return ScgModuleWrapper.getDummyInstance();
	}

	@Override
	protected ModuleWrapper wrap(Object configObject) {
		return new ScgModuleWrapper((io.swagger.codegen.CodegenConfig) configObject);
	}

	public static void main(String[] args) throws MalformedURLException, URISyntaxException, IOException {
		new SwaggerCodegenModuleDiscovery().runDiscovery(args);
	}
}
