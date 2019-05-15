/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger.test

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.genflow.common.jsonschema.Options
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import com.reprezen.rapidml.implicit.ZenModelNormalizer
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.Test

import static org.hamcrest.CoreMatchers.equalTo

class VendorExtensionsTest extends SwaggerGenTestBase {

	def String model() {
		'''
			rapidModel Extensions
				extensions
					x-foo: "model-extension"
				resourceAPI ExtensionsInterface baseURI "http://taxblaster.com/api"
			
					objectResource PersonObject type Person
						URI people/{name}
							required templateParam name property name
								extensions
									x-bar: "uri-parameter-extension"

						mediaTypes
							application/json
						
						method GET retrievePersonObject
							request
							response statusCode 200
							extensions
								x-foo: "method-extension"
			
						method PUT putPersonObject
							request
								required param id type string in header
									extensions
										x-bar: "message-parameter-extension"
							response PersonObject statusCode 200
			
						method POST postPersonObject
							request type Person
								referenceEmbed >ref
									targetProperties
										 id
									extensions
										x-foo-bar: "reference-embed-extension"
			
						method DELETE deletePersonObject
							request PersonObject
								extensions
									x-extension: "request-extension"
							response PersonObject
								extensions
									x-foo-bar: "response-extension"
			
					objectResource SecuredResource type Person
						URI securedResource
						secured by
							Basic
						method GET retrieveSecuredResource
							request
							response SecuredResource statusCode 200
							response statusCode 400
						
			
				dataModel ExtensionsDataModel
					structure Person
						name: string
						email: string
						ref: reference to MyDataType1
					
					structure MyDataType1
						id: string
						
				securitySchemesLibrary TaxBlasterAuthSchemes
					/** HTTP Basic authentication. 
					     
					    https://www.ietf.org/rfc/rfc2617.txt */
					securityScheme Basic
						type basic
						methodInvocation
							requires authorization
								/** userid and password, separated by a single colon (":") character, within a 
								    base64 [7] encoded string in the credentials.*/
								param basic_credentials type base64Binary in header
							errorResponse statusCode 401 //Unauthorized
						extensions
							x-foo: "securityScheme-extension"
		'''
	}

	// @Test
	def void printModel() throws Exception {
		println(generatedSwagger)
	}

	def JsonNode getGeneratedSwagger() {
		val zenModel = loadModelAndNormalize(model)
		// force-resolve references, otherwise test results can differ from actual generato results
		// Example: test_uri_parameter_extensions() will have a null parameter name
		new ValidationTestHelper().validate(zenModel)
		new ZenModelNormalizer().normalize(zenModel)

		val generator = new XGenerateSwagger()
		generator.init(createFakeGenTemplateContext)

		val swaggerText = generator.generate(zenModel)
		val swaggerJson = new ObjectMapper().readTree(swaggerText)
		return swaggerJson
	}

	@Test
	def void test_model_extensions() throws Exception {
		val root = generatedSwagger
		assertThat(root.get("x-foo").asText, equalTo("model-extension"))
	}

	@Test
	def void test_method_extensions() throws Exception {
		val root = generatedSwagger
		val expectedMethod = '''
			{
				"tags" : [ "PersonObject" ],
				"description" : "",
				"operationId" : "retrievePersonObject",
				"consumes" : [ "application/json" ],
				"produces" : [ "application/json" ],
				"responses" : {
					"200" : {
						"description" : ""
					}
				},
				"x-foo": "method-extension"
			}
		'''
		val method = root.get("paths").get("/people/{name}").get("get");
		assertThat(method, equalTo(new ObjectMapper().readTree(expectedMethod)))
	}

	@Test
	def void test_response_extensions() throws Exception {
		val root = generatedSwagger
		val expectedResponse = '''
			{
				"description": "",
				"schema": {
					"$ref": "#/definitions/Person"
				},
				"x-foo-bar": "response-extension"
			}
      '''
		val responses = root.get("paths").get("/people/{name}").get("delete").get("responses");
		val response = responses.get("200")
		assertThat(response, equalTo(new ObjectMapper().readTree(expectedResponse)))
	}

	@Test
	def void test_request_extensions() throws Exception {
		val root = generatedSwagger
		val expectedRequest = '''
			{
				"in": "body",
				"name": "Person",
				"description": "",
				"required": true,
				"schema": {
					"$ref": "#/definitions/Person"
				},
				"x-extension": "request-extension"
			}
		'''
		val params = root.get("paths").get("/people/{name}").get("delete").get("parameters") as ArrayNode;
		val request = params.get(0)
		assertThat(request, equalTo(new ObjectMapper().readTree(expectedRequest)))
	}

	@Test
	def void test_message_parameter_extensions() throws Exception {
		val root = generatedSwagger
		val expectedHeaderParam = '''
			{
				"name" : "id",
				"in" : "header",
				"description" : "",
				"required" : true,
				"type" : "string",
				"x-bar" : "message-parameter-extension"
			}
        '''
		val params = root.get("paths").get("/people/{name}").get("put").get("parameters") as ArrayNode;
		val param = params.get(0);
		assertThat(param, equalTo(new ObjectMapper().readTree(expectedHeaderParam)))
	}

	@Test
	def void test_uri_parameter_extensions() throws Exception {
		val root = generatedSwagger
		val expectedParameter = '''
			{
				"name": "name",
				"in" : "path",
				"description" : "",
				"required" : true,
				"type" : "string",
				"x-bar" : "uri-parameter-extension"
			}
	    '''
		val params = root.get("paths").get("/people/{name}").get("parameters") as ArrayNode;
		val param = params.get(0)
		assertThat(param, equalTo(new ObjectMapper().readTree(expectedParameter)))
	}

	@Test
	def void test_reference_treatment_extensions() throws Exception {
		val root = generatedSwagger
		val expectedPersonDefinition = '''
			{
				"type" : "object",
					"properties" : {
					"name" : {
						"type" : "string"
					},
					"email" : {
						"type" : "string"
					},
					"ref" : {
						"$ref" : "#/definitions/PersonObject_postPersonObject_request_ref"
					}
				}
			}		
		'''
		val personDefinition = root.get("definitions").get("PersonObject_postPersonObject_request");
		assertThat(personDefinition, equalTo(new ObjectMapper().readTree(expectedPersonDefinition)))

		val expectedRefDefinition = '''
			{
				"type" : "object",
				"x-foo-bar" : "reference-embed-extension",
				"properties" : {
					"id" : {
						"type" : "string"
					}
				}
			}
		'''
		val refDefinition = root.get("definitions").get("PersonObject_postPersonObject_request_ref");
		assertThat(refDefinition, equalTo(new ObjectMapper().readTree(expectedRefDefinition)))

	}

	@Test
	def void test_security_defiitionn_extensions() throws Exception {
		val root = generatedSwagger
		val expectedSecurityDefs = '''
			{
				"Basic": {
					"type": "basic",
					"x-foo": "securityScheme-extension"
				}
			}
		'''
		val securityDefs = root.get("securityDefinitions")
		assertThat(securityDefs, equalTo(new ObjectMapper().readTree(expectedSecurityDefs)))
	}

	def private createFakeGenTemplateContext() {
		return new FakeGenTemplateContext(
			#{Options::ALLOW_EMPTY_OBJECT -> true, Options::ALLOW_EMPTY_ARRAY -> true,
				Options::ALLOW_EMPTY_STRING -> true})
	}

}
