/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.swagger;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.source.AbstractLocator;

import io.swagger.models.Swagger;

// TODO: Implement a more reasonable locator capability - e.g. using JSONPaths
public class SwaggerLocator extends AbstractLocator<Swagger> {

	private final Swagger swagger;

	public SwaggerLocator(Swagger swagger) {
		this.swagger = swagger;
	}

	@Override
	public <T> String locate(T item) throws GenerationException {
		if (Swagger.class.isAssignableFrom(item.getClass())) {
			return "/";
		} else {
			throw new GenerationException("Swagger locator currently only supports full Swagger object");
		}
	}

	@Override
	public Object dereference(String locator) throws GenerationException {
		if (locator.equals("/")) {
			return this.swagger;
		} else {
			throw new GenerationException("Swagger locator currently only supports full Swagger object");
		}
	}

}
