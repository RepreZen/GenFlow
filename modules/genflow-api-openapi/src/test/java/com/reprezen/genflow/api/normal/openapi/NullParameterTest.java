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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.normal.openapi.OpenApiNormalizer;
import com.reprezen.genflow.api.normal.openapi.Option;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class NullParameterTest {

	private static Swagger spec;

	@BeforeClass
	public static void setup() throws IOException, GenerationException {
		URL url = Resources.getResource(NullParameterTest.class,
				"/normalizer/NormalizerTests/models/NullParameter.yaml");
		String yaml = IOUtils.toString(url.openStream(), "UTF-8");
		List<Option> options = Lists.newArrayList(Option.DOC_DEFAULT_OPTIONS);
		spec = new OpenApiNormalizer(SWAGGER_MODEL_VERSION, options.toArray(new Option[0])).of(yaml)
				.normalizeToSwagger(url);
	}

	// Test for ZEN-3428 Rendering error in all three live views on Swagger spec
	// with a broken parameter $ref
	@Test
	public void no_null_values_in_list_for_invalid_path_param_ref() {
		List<Parameter> pathParams = spec.getPath("/resourceUrl").getParameters();
		assertTrue("Broken parameter refs should never create null list elements", pathParams.isEmpty());
		// Broken path params are NOT copied to methods
		Operation method = spec.getPath("/resourceUrl").getOperationMap().get(HttpMethod.GET);
		Parameter parameter = method.getParameters().get(0);
		String paramRef = ((RefParameter) parameter).get$ref();
		assertEquals("Broken parameter refs should never create null list elements",
				"#/_UNRESOLVABLE/#/_UNRESOLVABLE//broken/param/ref", paramRef);
	}

	@Test
	public void unresolvable_ref_for_invalid_schema_ref() {
		Operation method = spec.getPath("/resourceUrl").getOperationMap().get(HttpMethod.GET);
		Property schema = method.getResponses().get("200").getSchema();
		String schemaRef = ((RefProperty) schema).get$ref();
		assertEquals("Broken schema ref should be resolved to a non-null _UNRESOLVABLE ref",
				"#/_UNRESOLVABLE/#/_UNRESOLVABLE//broken/schema/ref", schemaRef);
	}
}
