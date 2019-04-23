/*******************************************************************************
 * Copyright © 2013, 2018 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi3;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.outputitem.AbstractOutputItem;
import com.reprezen.genflow.api.util.TypeUtils;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

public abstract class OpenApi3ExtractOutputItem<ItemType> extends AbstractOutputItem<OpenApi3, ItemType> {
	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return OpenApi3.class;
	}

	@Override
	public Class<?> getItemType() throws GenerationException {
		return TypeUtils.getTypeParamClass(getClass(), OpenApi3ExtractOutputItem.class, 0);
	}

}
