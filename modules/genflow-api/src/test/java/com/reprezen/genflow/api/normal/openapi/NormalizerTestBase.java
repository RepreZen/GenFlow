/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import static com.reprezen.genflow.api.normal.openapi.ObjectType.SWAGGER_MODEL_VERSION;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.google.common.io.Resources;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.normal.openapi.OpenApiNormalizer;
import com.reprezen.genflow.api.normal.openapi.Option;
import com.reprezen.genflow.api.normal.openapi.Options;

import io.swagger.models.Swagger;

/**
 * @author Andy Lowry
 * 
 */
public class NormalizerTestBase extends Assert {

	static Swagger spec = null;

	@BeforeClass
	public static void staticSetup() throws IOException, GenerationException {
		URL url = Resources.getResource(NormalizerTestBase.class, "/normalizer/NormalizerTests/models/Test.yaml");
		String yaml = IOUtils.toString(url.openStream(), "UTF-8");
		Options options = new Options(SWAGGER_MODEL_VERSION, Option.DOC_DEFAULT_OPTIONS);
		options.replace(Option.INLINE_ALL);
		spec = new OpenApiNormalizer(options).of(yaml).normalizeToSwagger(url);
	}
}
