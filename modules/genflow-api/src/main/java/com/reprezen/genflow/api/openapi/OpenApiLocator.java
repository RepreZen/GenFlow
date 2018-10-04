/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.source.AbstractLocator;
import com.reprezen.kaizen.oasparser.OpenApi;

public class OpenApiLocator extends AbstractLocator<OpenApiDocument> {

	private final OpenApiDocument model;

	public OpenApiLocator(OpenApiDocument model) {
		this.model = model;
	}

	@Override
	public <T> String locate(T item) throws GenerationException {
		if (OpenApi.class.isAssignableFrom(item.getClass())) {
			return "/";
		} else {
			throw new GenerationException("OpenAPI locator currently only supports full OpenAPI object");
		}
	}

	@Override
	public Object dereference(String locator) throws GenerationException {
		if (locator.equals("/")) {
			return model;
		} else {
			throw new GenerationException("OpenAPI locator currently only supports full OpenAPI object");
		}
	}

}
