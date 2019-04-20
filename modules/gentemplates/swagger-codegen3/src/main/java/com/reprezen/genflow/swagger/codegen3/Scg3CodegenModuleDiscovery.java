/*******************************************************************************
 * Copyright © 2013, 2019 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen3;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.reprezen.genflow.common.codegen.GenModuleDiscovery;
import com.reprezen.genflow.common.codegen.GenModuleWrapper;

import io.swagger.codegen.v3.CodegenConfig;

public class Scg3CodegenModuleDiscovery extends GenModuleDiscovery<io.swagger.codegen.v3.CodegenConfig> {

	@Override
	protected String getLibraryVersion() {
		return CodegenConfig.class.getPackage().getImplementationVersion();
	}

	@Override
	protected GenModuleWrapper<CodegenConfig> getDummyWrapper() {
		return Scg3ModuleWrapper.getDummyInstance();
	}

	@Override
	protected GenModuleWrapper<CodegenConfig> wrap(Object configObject) {
		return new Scg3ModuleWrapper((CodegenConfig) configObject);
	}

	public static void main(String[] args) throws MalformedURLException, URISyntaxException, IOException {
		new Scg3CodegenModuleDiscovery().runDiscovery(args);
	}
}
