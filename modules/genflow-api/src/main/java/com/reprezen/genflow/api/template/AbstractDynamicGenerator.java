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
import com.reprezen.genflow.api.outputitem.AbstractOutputItem;
import com.reprezen.genflow.api.util.TypeUtils;

public abstract class AbstractDynamicGenerator<PrimaryType> implements IDynamicGenerator<PrimaryType> {

	protected IGenTemplateContext context;

	@Override
	public void init(IGenTemplateContext context) throws GenerationException {
		this.context = context;
	}

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return TypeUtils.getTypeParamClass(this.getClass(), AbstractOutputItem.class, 0);
	}
}
