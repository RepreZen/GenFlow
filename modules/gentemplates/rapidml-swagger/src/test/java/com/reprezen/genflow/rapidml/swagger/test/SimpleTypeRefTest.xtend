/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.genflow.common.jsonschema.Options
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import io.swagger.util.Json
import org.junit.Test

/*
 * ZEN-3743
 * RAPID-ML -> Swagger GenTemplate: simpleType bound as parameter left unresolved in generated Swagger
 */
class SimpleTypeRefTest extends SwaggerGenTestBase {

	def String model(){
		'''
			rapidModel TaxBlaster
				resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"
					objectResource PersonObject type Person
						URI people/{id}
							required templateParam id property taxpayerID
						mediaTypes
							application/json
						method GET getPersonObject
							request
							response PersonObject statusCode 200
				dataModel TaxBlasterDataModel
					simpleType SSN defined as string
					structure Person
						taxpayerID : SSN
						lastName : string
						firstName : string
		'''
	}

	def String model2() {
		'''
		rapidModel TaxBlaster
			resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"
				objectResource PersonObject type Person
					URI people/{id}
						required templateParam id property id
					mediaTypes
						application/json
					method GET getPersonObject
						request
						response PersonObject statusCode 200
			dataModel TaxBlasterDataMode
				enum string enumType
					"One": "One"
					"Two": "Two"
					"Three": "Three"
				structure Person
					id : string
					birthDate: date
					birthDateTime: dateTime
					longValue: long
					floatValue: float
					doubleValue: double
					enumValue: enumType
		'''
	}

	def getSwagger(String model) {
		val zenModel = loadModelAndNormalize(model)
	
		new XGenerateSwagger().getSwagger(zenModel)
	}

	@Test
	def void testSimpleTypeRefOnUserDefinedType() {
		val swagger = getSwagger(model())		
		val spec = new ObjectMapper().readTree(Json.pretty(swagger))

		assertEquals("string", 
			spec.get("paths").get("/people/{id}").get("parameters").get(0).get("type").asText
		)
	}

	@Test
	def void testSimpleTypeRef() {
		val swagger = getSwagger(model2())
		val spec = new ObjectMapper().readTree(Json.pretty(swagger))

		assertEquals("string", 
			spec.get("paths").get("/people/{id}").get("parameters").get(0).get("type").asText
		)
	}

	@Test
	def void testEnumValue() {
		val zenModel = loadModelAndNormalize(model2())
	
		val gen = new XGenerateSwagger()
		gen.init(createFakeGenTemplateContext)
		val result = gen.generate(zenModel)
		val spec = new ObjectMapper().readTree(result)

		val	 enumValue = spec.get("definitions").get("Person").get("properties").get("enumValue")
		assertEquals("#/definitions/enumType", enumValue.get("$ref").asText)

		val enumType = spec.get("definitions").get("enumType")
		assertEquals("string", enumType.get("type").asText)	
		assertEquals(#["One", "Two", "Three"], enumType.get("enum").map[asText].toList)		
	}

	@Test
	def void testSimpleTypeFormat() {
		val zenModel = loadModelAndNormalize(model2())
	
		val gen = new XGenerateSwagger()
		gen.init(createFakeGenTemplateContext)
		val result = gen.generate(zenModel)
		val spec = new ObjectMapper().readTree(result)

		val birthDate = spec.get("definitions").get("Person").get("properties").get("birthDate")
		assertEquals("string", birthDate.get("type").asText)
		assertEquals("date", birthDate.get("format").asText)

		val birthDateTime = spec.get("definitions").get("Person").get("properties").get("birthDateTime")
		assertEquals("string", birthDateTime.get("type").asText)
		assertEquals("date-time", birthDateTime.get("format").asText)

		val longValue = spec.get("definitions").get("Person").get("properties").get("longValue")
		assertEquals("integer", longValue.get("type").asText)
		assertEquals("int64", longValue.get("format").asText)

		val doubleValue = spec.get("definitions").get("Person").get("properties").get("doubleValue")
		assertEquals("number", doubleValue.get("type").asText)
		assertEquals("double", doubleValue.get("format").asText)

		val floatValue = spec.get("definitions").get("Person").get("properties").get("floatValue")
		assertEquals("number", floatValue.get("type").asText)
		assertEquals("float", floatValue.get("format").asText)
	}

	def private createFakeGenTemplateContext() {
		return new FakeGenTemplateContext(
			#{Options::ALLOW_EMPTY_OBJECT -> true, Options::ALLOW_EMPTY_ARRAY -> true,
				Options::ALLOW_EMPTY_STRING -> true})
	}
}