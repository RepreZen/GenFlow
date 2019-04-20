/*******************************************************************************
 * Copyright © 2013, 2019 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen3;

import com.reprezen.genflow.common.codegen.GenModuleWrapper.CliOptWrapper;

import io.swagger.codegen.v3.CliOption;

public class Scg3CliOptWrapper extends CliOptWrapper<CliOption> {

	public Scg3CliOptWrapper(CliOption cliOpt) {
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