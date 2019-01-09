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
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import io.swagger.models.RefModel
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.properties.RefProperty
import org.junit.Test

import static org.hamcrest.CoreMatchers.*

class DefaultMessageTest extends SwaggerGenTestBase {

	def String model() {
		'''
			rapidModel MyModel
				resourceAPI MyResourceAPI baseURI "http://my-namespace.com"
					objectResource MyObjectNoCustomizations type MyDataType
						URI /uri
						mediaTypes
							application/json
						method GET getResourceWDefaultRealization
							request MyObjectNoCustomizations
							response MyObjectNoCustomizations statusCode 200
						method PUT useDefaultDataType
							request type MyDataType 
							response statusCode 200
							response type MyDataType statusCode 400
						method POST dataTypeWithCustomizations
							request type MyDataType
								with all properties excluding
									prop2
							response statusCode 200
							response type MyDataType statusCode 400
								with all properties excluding
									prop1
			
					objectResource ReferenceResource type MyDataType2
						URI /referenced
						mediaTypes
							application/json
			
				dataModel MyModel
					structure MyDataType
						id : string
						prop1: string
						prop2: string
						ref: reference to MyDataType2
					structure MyDataType2
						id : string
		'''.toString.replaceAll("    ", "\t")
	}

	// @Test
	def void printModel() throws Exception {
		val zenModel = prepareModelFor(model())
		val generator = prepareGenerator()

		val swaggerText = generator.generate(zenModel)
		println(swaggerText)
	}

	@Test
	def void empty_message_parameter() {
		val modelText = '''
			rapidModel MyModel
				resourceAPI MyResourceAPI baseURI "http://my-namespace.com"
					objectResource MyObject type MyDataType
						URI myurl
						method GET getMyObject
							request
							response MyObject statusCode 200
							
				dataModel TaxBlasterDataModel
					structure MyDataType
		'''
		val zenModel = prepareModelFor(modelText)
		val generator = prepareGenerator()
		val swaggerAsTree = new ObjectMapper().readTree(generator.generate(zenModel))
		val method = swaggerAsTree.get("paths").get("/myurl").get("get")
		assertNotNull(method)
		assertNull(method.get("parameters"))
	}

	@Test
	def void refTo_dataType_defaultRealization_inRequest() {
		val swagger = getSwagger(model())

		val resource = swagger.paths.get("/uri")
		assertNotNull(resource)
		val method = resource.put
		assertEquals(1, method.parameters.length)
		assertThat(method.parameters.head, instanceOf(typeof(BodyParameter)))
		assertThat(method.parameters.head.name, equalTo("MyDataType"))
		assertThat(((method.parameters.head as BodyParameter).schema as RefModel).$ref,
			equalTo("#/definitions/MyDataType"))
	}

	@Test
	def void refTo_dataType_defaultRealization_inResponse() {
		val swagger = getSwagger(model())

		val resource = swagger.paths.get("/uri")
		assertNotNull(resource)

		val schema = resource.put.responses.get("400").schema
		assertThat(schema, instanceOf(RefProperty))
		assertThat((schema as RefProperty).$ref, equalTo("#/definitions/MyDataType"))
	}

	@Test
	def void refTo_resource_defaultRealization() {
		val swagger = getSwagger(model())

		val resource = swagger.paths.get("/uri")
		assertNotNull(resource)

		val schema = resource.get.responses.get("200").schema
		assertThat(schema, instanceOf(RefProperty))
		assertThat((schema as RefProperty).$ref, equalTo("#/definitions/MyDataType"))
	}

	@Test
	def void refTo_dataType_wCustomization() {
		val swagger = getSwagger(model())
		val resource = swagger.paths.get("/uri")
		assertNotNull(resource)

		val schema = resource.post.responses.get("400").schema
		assertThat(schema, instanceOf(RefProperty))
		assertThat((schema as RefProperty).$ref,
			equalTo("#/definitions/MyObjectNoCustomizations_dataTypeWithCustomizations_response400"))
	}

	protected def getSwagger(String modelText) {
		val zenModel = prepareModelFor(modelText)
		val generator = prepareGenerator()
		val swagger = generator.getSwagger(zenModel)
		swagger
	}

	@Test
	def void schemaDef_count() {
		val jsonSchemasNode = getJsonSchema(model())
		val definitions = jsonSchemasNode.get("definitions")

		assertThat(definitions.size,
			equalTo(
				3 /* for MyDataType and messages */ + 1 /* for MyDataType2 */ +
					2 /* for RAPIDLinkMap and RAPIDLinkObject */ + 1 /*ReferenceResource_link */ ))
	}

	@Test
	def void schemaDef_resource_defaultRealization() {
		val jsonSchemasNode = getJsonSchema(model())
		val definitions = jsonSchemasNode.get("definitions")
		val schema = definitions.get("MyDataType")
		assertThat(schema, notNullValue)

		val properties = schema.get("properties")
		assertThat(properties.size, equalTo(4))

		val id = properties.findValue("id")
		assertNotNull(id)
		val prop1 = properties.findValue("prop1")
		assertNotNull(prop1)
		val prop2 = properties.findValue("prop2")
		assertNotNull(prop2)
		val ref = properties.findValue("ref")
		assertNotNull(ref)

	}

	@Test
	def void schemaDef_dataType_defaultRealization() {
		// the same as schemaDef_resource_defaultRealization
	}

	@Test
	def void schemaDef_dataType_wCustomization_1() {
		val jsonSchemasNode = getJsonSchema(model())
		val definitions = jsonSchemasNode.get("definitions")
		val schema = definitions.get("MyObjectNoCustomizations_dataTypeWithCustomizations_request")
		assertThat(schema, notNullValue)

		val properties = schema.get("properties")
		assertThat(properties.size, equalTo(3))

		val id = properties.findValue("id")
		assertNotNull(id)
		val prop1 = properties.findValue("prop1")
		assertNotNull(prop1)
		val ref = properties.findValue("ref")
		assertNotNull(ref)
	}

	@Test
	def void schemaDef_dataType_wCustomization_2() {
		val jsonSchemasNode = getJsonSchema(model())
		val definitions = jsonSchemasNode.get("definitions")
		val schema = definitions.get("MyObjectNoCustomizations_dataTypeWithCustomizations_response400")
		assertThat(schema, notNullValue)

		val properties = schema.get("properties")
		assertThat(properties.size, equalTo(3))

		val id = properties.findValue("id")
		assertNotNull(id)
		val prop2 = properties.findValue("prop2")
		assertNotNull(prop2)
		val ref = properties.findValue("ref")
		assertNotNull(ref)
	}

	def protected getJsonSchema(String modelText) {
		val zenModel = prepareModelFor(modelText)
		val generator = prepareGenerator()
		val jsonSchemaNode = new ObjectMapper().readTree(generator.generate(zenModel))
		return jsonSchemaNode
	}

	def protected prepareGenerator() {
		val generator = new XGenerateSwagger()
		generator.init(new FakeGenTemplateContext)
		return generator
	}

	def protected prepareModelFor(String modelText) {
		val zenModel = loadModelAndNormalize(modelText)
		return zenModel
	}
}
