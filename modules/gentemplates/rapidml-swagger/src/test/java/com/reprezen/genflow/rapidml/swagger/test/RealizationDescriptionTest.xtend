/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.genflow.common.jsonschema.JsonSchemaFormat
import com.reprezen.genflow.rapidml.jsonschema.XGenerateJsonSchema
import com.reprezen.genflow.rapidml.swagger.SwaggerOutputFormat
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import org.junit.Test

class RealizationDescriptionTest extends SwaggerGenTestBase {
	
	@Test
	def void testResponseDescription() {
		val zenModel = loadModelAndNormalize(model1)

		val generator = new XGenerateSwagger(SwaggerOutputFormat.YAML)
		generator.init(new FakeGenTemplateContext)
		val swagger = generator.getSwagger(zenModel)
		val path = swagger.paths.get("/index")
		assertNotNull(path)

		val response1 = path.get.responses.get("200") // withDescriptionFromDataType
		assertNotNull(response1)
		assertEquals("My data type comment", response1.description)

		val response2 = path.put.responses.get("200") // withDescriptionFromResource
		assertNotNull(response2)
		assertEquals("My data type comment", response2.description)

		val response3 = path.post.responses.get("200") // withDescriptionFromResponse1
		assertNotNull(response3)
		assertEquals("Response documentation 1", response3.description)

		val response4 = path.patch.responses.get("200") // withDescriptionFromResponse2
		assertNotNull(response4)
		assertEquals("Response documentation 2", response4.description)
	}

	def model1() {
		'''
			rapidModel TaxBlaster
				resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"
					/** The Index Resource is lorem ipsum*/
					objectResource IndexObject type Index
						URI index
						mediaTypes
							application/xml
						method GET withDescriptionFromDataType
							response type Index statusCode 200
						method PUT withDescriptionFromResource
							response IndexObject statusCode 200
						
						method POST withDescriptionFromResponse1
							/**Response documentation 1*/
							response IndexObject statusCode 200
						method PATCH withDescriptionFromResponse2
							/**Response documentation 2*/
							response type Index statusCode 200
				dataModel myModel
					/**My data type comment*/
					structure Index
						id : string
		'''
	}

	@Test
	def void testPropertyDescription() {
		val zenModel = loadModelAndNormalize(model2)

		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(new FakeGenTemplateContext)
		val jsonSchemaAsTest = jsonSchemaGenerator.generate(zenModel).toString
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaAsTest)

		// val generator = new XGenerateSwagger(SwaggerOutputFormat.YAML)
		// generator.init(new FakeGenTemplateContext)
		// println(generator.generate(zenModel))
		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)
		val indexObject = definitions.get("Type1")
		assertNotNull(indexObject)

		assertEquals("Documentation for prop1", indexObject.get("properties").get("prop1").get("description").asText)
		assertEquals("Documentation for prop2", indexObject.get("properties").get("prop2").get("description").asText)
		assertEquals("Documentation for ref1", indexObject.get("properties").get("ref1").get("description").asText)
		assertEquals("Documentation for ref2", indexObject.get("properties").get("ref2").get("description").asText)
	}

	def model2() {
		'''
			rapidModel TaxBlaster
				resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"
					objectResource IndexObject type Type1
						URI index
						mediaTypes
							application/xml
						method GET getObject
							response type Type1 statusCode 200
				dataModel myModel
					/**My data type comment*/
					structure Type1
						/** Documentation for prop1*/
						prop1 : string
						/** Documentation for prop2*/
						prop2 : string
						/** Documentation for ref1*/
						ref1: reference Type2
						/** Documentation for ref2*/
						ref2: reference Type2
					structure Type2
						prop1 : string
		'''
	}
}
