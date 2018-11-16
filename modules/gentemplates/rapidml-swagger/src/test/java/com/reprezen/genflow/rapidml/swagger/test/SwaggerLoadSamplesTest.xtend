/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger.test

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.genflow.rapidml.swagger.SwaggerOutputFormat
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import com.reprezen.rapidml.ZenModel
import com.reprezen.rapidml.implicit.ZenModelNormalizer
import com.reprezen.rapidml.xtext.loaders.ZenModelLoader
import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.ArrayList
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(typeof(Parameterized))
class SwaggerLoadSamplesTest extends Assert {
	var JsonSchema schema = null

	@Parameters(name="{index}: {0}")
	def static Iterable<Object[]> data() {
		val dataList = new ArrayList<Object[]>
		val modelDir = new File(SwaggerLoadSamplesTest.getResource("models").toURI.path)
		Files.walkFileTree(modelDir.toPath, new SimpleFileVisitor<Path>() {
			override FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
				if (path.last.toString.endsWith(".rapid")) {
					dataList.add(#[path.toString] as Object[])
				}
				return FileVisitResult.CONTINUE
			}
		});
		dataList
	}

	val String modelPath

	new(String modelPath) {
		this.modelPath = modelPath
	}

	@Test
	def void validateSwaggerModel() {
		val model = loadSampleAndAssertNoError(modelPath)
		new ZenModelNormalizer().normalize(model)

		val generator = new XGenerateSwagger()
		generator.init(
			new FakeGenTemplateContext(
				#{XGenerateSwagger::OUTPUT_FORMAT_PARAM -> SwaggerOutputFormat.JSON.toString as Object}))
		val swagger = generator.generate(model)
		val JsonSchema schema = getSwaggerSchema()
		val ProcessingReport report = schema.validate(JsonLoader.fromString(swagger), true);
		if (!report.success) {
			val StringBuilder errors = new StringBuilder
			report.forEach [
				errors.append(
					it.logLevel.name.toUpperCase + ' [' + it.asJson.get('instance').get('pointer') + '] - ' +
						it.message + '\n')
			]
			fail(errors.toString)
		}
	}

	val loader = new ZenModelLoader
	def ZenModel loadSampleAndAssertNoError(String path) {
		return loader.loadAndValidateModel(new File(path))
	}

	def getSwaggerSchema() {
		if (schema === null) {
			val schemaUrl = SwaggerLoadSamplesTest.getResource("schema.json")
			schema = JsonSchemaFactory.byDefault.getJsonSchema(schemaUrl.toString)
		}
		return schema
	}
}
