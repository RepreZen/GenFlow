package com.reprezen.genflow.openapi.generator;

import com.reprezen.genflow.common.codegen.GenModuleWrapper.CliOptWrapper;

public class OagCliOptWrapper extends CliOptWrapper<org.openapitools.codegen.CliOption> {

	public OagCliOptWrapper(org.openapitools.codegen.CliOption cliOpt) {
		super(cliOpt);
	}

	@Override
	public String getName() {
		return cliOpt.getOpt();
	}

	@Override
	public String getDescription() {
		return cliOpt.getDescription();
	}

}