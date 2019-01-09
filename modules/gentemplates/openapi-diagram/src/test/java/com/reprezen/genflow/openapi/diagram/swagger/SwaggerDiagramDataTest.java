/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.swagger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.reprezen.genflow.api.util.OpenApiIO;
import com.reprezen.genflow.openapi.diagram.swagger.SwaggerDiagramData;

import io.swagger.models.Swagger;

@RunWith(Parameterized.class)
public class SwaggerDiagramDataTest {

	@Parameter
	public URI swaggerYamlURL;

	@Parameter(value = 1)
	public String userFriendlyName;

	@Parameters(name = "{1}")
	public static Collection<Object[]> data() throws Exception {
		List<Path> entries = Lists.newArrayList();
		File modelDir = new File(Resources.getResource(SwaggerDiagramDataTest.class, "/").toURI().getPath());
		Files.walkFileTree(modelDir.toPath(), new SimpleFileVisitor<Path>() {
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
				if (path.toString().endsWith(".yaml")) {
					entries.add(path);
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return entries.stream() //
				.map(e -> new Object[] { e.toUri(), e.toString() }) //
				.collect(Collectors.toList());
	}

	@Test
	public void testDiagramData() throws Exception {
		File swaggerYamlFile = toFile(swaggerYamlURL);
		String yaml = com.google.common.io.Files.toString(swaggerYamlFile, Charsets.UTF_8);
		Swagger swagger = OpenApiIO.loadSwagger(yaml);
		assertNotNull(swagger);
		String dataJsonAsString = new SwaggerDiagramData(swagger).generateDiagramData();
		assertNotNull(dataJsonAsString);
		JsonNode dataJson = null;
		try {
			dataJson = new ObjectMapper().readTree(dataJsonAsString);
		} catch (JsonParseException e) {
			fail("Invalid JSON: " + e.getMessage());
		}
		assertNotNull(dataJson);
	}

	protected File toFile(URI uri) {
		return new File(uri);
	}

}
