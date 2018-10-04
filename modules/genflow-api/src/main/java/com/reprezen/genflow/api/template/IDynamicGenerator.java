/*******************************************************************************
 * Copyright © 2013, 2018 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template;

import com.reprezen.genflow.api.GenerationException;

public interface IDynamicGenerator<PrimaryType> {

	void init(IGenTemplateContext context) throws GenerationException;

	void generate(PrimaryType primaryValue) throws GenerationException;

	Class<?> getPrimaryType() throws GenerationException;
}
