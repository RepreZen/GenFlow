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
import com.reprezen.genflow.api.util.TypeUtils;

public abstract class AbstractOutputItem<PrimaryType, ItemType> implements IOutputItem<PrimaryType, ItemType> {

	protected IGenTemplateContext context;

	@Override
	public void init(IGenTemplateContext context) {
		this.context = context;
	}

	@Override
	public File getOutputFile(PrimaryType sourceValue, ItemType itemValue) {
		return null;
	}

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return TypeUtils.getTypeParamClass(this.getClass(), AbstractOutputItem.class, 0);
	}

	@Override
	public Class<?> getItemType() throws GenerationException {
		return TypeUtils.getTypeParamClass(this.getClass(), AbstractOutputItem.class, 1);
	}
}
