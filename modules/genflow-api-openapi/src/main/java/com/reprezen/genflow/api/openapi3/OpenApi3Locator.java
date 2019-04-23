/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi3;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.source.AbstractLocator;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

public class OpenApi3Locator extends AbstractLocator<OpenApi3> {

	private final OpenApi3 model;

	public OpenApi3Locator(OpenApi3 model) {
		this.model = model;
	}

	@Override
	public <T> String locate(T item) throws GenerationException {
		if (OpenApi3.class.isAssignableFrom(item.getClass())) {
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
