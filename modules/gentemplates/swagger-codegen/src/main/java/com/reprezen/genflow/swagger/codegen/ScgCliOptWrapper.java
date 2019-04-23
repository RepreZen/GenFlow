package com.reprezen.genflow.swagger.codegen;

import com.reprezen.genflow.common.codegen.GenModuleWrapper.CliOptWrapper;

public class ScgCliOptWrapper extends CliOptWrapper<io.swagger.codegen.CliOption> {

	public ScgCliOptWrapper(io.swagger.codegen.CliOption cliOpt) {
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