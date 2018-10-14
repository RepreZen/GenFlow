/*******************************************************************************
 * Copyright © 2013, 2018 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.swagger;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.outputitem.AbstractOutputItem;
import com.reprezen.genflow.api.util.TypeUtils;

import io.swagger.models.Swagger;

public abstract class SwaggerExtractOutputItem<ItemType> extends AbstractOutputItem<Swagger, ItemType> {

	@Override
	public Class<?> getPrimaryType() throws GenerationException {
		return Swagger.class;
	}

	@Override
	public Class<?> getItemType() throws GenerationException {
		return TypeUtils.getTypeParamClass(getClass(), SwaggerExtractOutputItem.class, 0);
	}
}
