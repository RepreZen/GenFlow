/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.outputitem;

import java.io.File;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.IGenTemplateContext;

public interface IOutputItem<PrimaryType, ItemType> {

	void init(IGenTemplateContext context);

	String generate(PrimaryType primarySource, ItemType inputItem) throws GenerationException;

	File getOutputFile(PrimaryType sourceValue, ItemType itemValue);

	Class<?> getPrimaryType() throws GenerationException;

	Class<?> getItemType() throws GenerationException;

}
