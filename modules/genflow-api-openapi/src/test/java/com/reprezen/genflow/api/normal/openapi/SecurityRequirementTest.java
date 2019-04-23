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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.normal.openapi.OpenApiNormalizer;
import com.reprezen.genflow.api.normal.openapi.Option;

import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;

public class SecurityRequirementTest {

	private static Swagger spec;

	@BeforeClass
	public static void setup() throws IOException, GenerationException {
		URL url = Resources.getResource(SecurityRequirementTest.class,
				"/normalizer/NormalizerTests/models/SecurityRequirement.yaml");
		String yaml = IOUtils.toString(url.openStream(), "UTF-8");
		List<Option> options = Lists.newArrayList(Option.DOC_DEFAULT_OPTIONS);
		spec = new OpenApiNormalizer(SWAGGER_MODEL_VERSION, options.toArray(new Option[0])).of(yaml)
				.normalizeToSwagger(url);
	}

	@Test
	public void default_security_requirement() {
		List<Map<String, List<String>>> security = spec.getPath("/taxFilings").getOperationMap().get(HttpMethod.GET)
				.getSecurity();
		assertNotNull(security);
		assertFalse(security.isEmpty());
		List<String> basicAuth = security.get(0).get("basic_auth");
		assertNotNull(basicAuth);
		assertTrue(basicAuth.isEmpty());
	}

	@Test
	public void explicitly_set_security_requirement() {
		List<Map<String, List<String>>> security = spec.getPath("/taxFilings").getOperationMap().get(HttpMethod.POST)
				.getSecurity();
		assertNotNull(security);
		assertFalse(security.isEmpty());
		List<String> oauth2 = security.get(0).get("OAuth2");
		assertNotNull(oauth2);
		assertEquals(1, oauth2.size());
		assertEquals("scope1", oauth2.get(0));
	}

}
