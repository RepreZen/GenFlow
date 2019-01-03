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
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.genflow.common.jsonschema.JsonSchemaFormat
import com.reprezen.genflow.common.jsonschema.Options
import com.reprezen.genflow.rapidml.jsonschema.XGenerateJsonSchema
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import io.swagger.models.Model
import io.swagger.models.RefModel
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.properties.Property
import io.swagger.models.properties.RefProperty
import org.junit.Test

import static com.reprezen.genflow.rapidml.swagger.test.JsonNodeEqualsToMatcher.nodeEqualsToJson

class CollectionResourceTest extends SwaggerGenTestBase{

	def String model() {
		return model(false)
	}

	def String model(boolean refNotEmbed) {
		'''
			rapidModel TaxBlaster
				resourceAPI TaxBlasterAPI baseURI "http://my-namespace.com"
			
					collectionResource TaxFilingCollection type TaxFiling
						URI /taxFilings
						method GET getTaxFilingCollection
							request
							response TaxFilingCollection statusCode 200
					
						method POST updateTaxFilings
							request with TaxFilingCollection
							response with TaxFilingCollection statusCode 200
							
							response statusCode 400
					«IF refNotEmbed»objectResource TaxFilingObject type TaxFiling«ENDIF»
						«IF refNotEmbed»URI /taxfiling«ENDIF»

				dataModel TaxBlasterDataModel
					structure TaxFiling
						id : string
        '''
	}

	val expectedRapidLinkMap = '''
		{
			"type": "object",
			"minProperties": 1,
			"description": "A set of hyperlinks from a domain object representation to related resources.\nEach property maps a [link relation](https://www.iana.org/assignments/link-relations/link-relations.xhtml)\n(the map key or property name) to a hyperlink object (the value).\n",
			"readOnly": true,
			"additionalProperties": {
				"$ref": "#/definitions/_RapidLink"
			}
		}
	'''
	
	val expectedRapidLinkObject = '''
		{
			"description": "An object representing a hyperlink to a related resource.\nThe link relation is specified as the containing property name.\nThe `href` property specifies the target URL, and additional properties may specify other metadata.\n",
			"type": "object",
		  	"minProperties": 1,
			"properties": {
				"href": {
					"type": "string"
				}
			}
		}
	'''

	// @Test
	def void printModel() throws Exception {
		val zenModel = loadModelAndNormalize(model_with_simple_hyperlinks)

		val generator = new XGenerateSwagger()
		generator.init(createFakeGenTemplateContext)

		val swaggerText = generator.generate(zenModel)
		println(swaggerText)
	}

	def String model_with_simple_hyperlinks() {
		'''
			rapidModel Example
				resourceAPI ExampleAPI baseURI "http://my-namespace.com"
					collectionResource AccountCollection type Account
						URI /accounts
						mediaTypes
							application/json
						method GET getAccountCollection
							request
							response AccountCollection statusCode 200
					objectResource AccountObject type Account
						URI /accounts/{id}
						mediaTypes
							application/json
				dataModel Data
					structure Account
						id : string
						name : string
		'''
	}

	@Test
	def void test_simple_hyperlinks() throws Exception {
		val zenModel = loadModelAndNormalize(model_with_simple_hyperlinks)

		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createFakeGenTemplateContext)
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)

		val rapidLinksMap = definitions.get("_RapidLinksMap")
		assertNotNull(rapidLinksMap)

		assertThat(rapidLinksMap, nodeEqualsToJson(expectedRapidLinkMap))
		
		val RAPIDLinkObject = definitions.get("_RapidLink")
		assertNotNull(RAPIDLinkObject)

		assertThat(RAPIDLinkObject, nodeEqualsToJson(expectedRapidLinkObject))
		
		val accountCollection = definitions.get("AccountCollection")
		assertNotNull(accountCollection)
		val expectedAccountCollection = '''
			{
				"type": "array",
				"items": {
					"$ref": "#/definitions/AccountObject_link"
				}
			}
		'''
		assertThat(accountCollection, nodeEqualsToJson(expectedAccountCollection))

		val JsonNode accountCollectionItem = definitions.get("AccountObject_link")
		assertNotNull(accountCollection)
		val expectedAccountCollectionItem = '''
			{
				"type": "object",
				"properties": {
					"_links": {
						"$ref": "#/definitions/_RapidLinksMap"
					}
				}
			}
		'''
		assertThat(accountCollectionItem, nodeEqualsToJson(expectedAccountCollectionItem))
	}

	def String model_decorated_with_additional_properties() {
		'''
			rapidModel Example
				resourceAPI ExampleAPI baseURI "http://my-namespace.com"
					collectionResource AccountCollection type Account
						URI /accounts
						mediaTypes
							application/json
						method GET retrieveAccountCollection
							request
							response with AccountCollection statusCode 200
					objectResource AccountObject type Account
						URI /accounts/{id}
							required templateParam id property accountID
						linkDescriptor AccountLink
							accountID
							name
						mediaTypes
							application/json
				dataModel Data
					structure Account
						accountID : string
						name : string
						status : int
						openDate : date
		'''
	}

	@Test
	def void test_model_decorated_with_additional_properties() throws Exception {
		val zenModel = loadModelAndNormalize(model_decorated_with_additional_properties)

		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createFakeGenTemplateContext)
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)

		val RAPIDLinkObject = definitions.get("_RapidLinksMap")
		assertNotNull(RAPIDLinkObject)

		assertThat(RAPIDLinkObject, nodeEqualsToJson(expectedRapidLinkMap))
	
		val accountCollection = definitions.get("AccountCollection")
		assertNotNull(accountCollection)
		val expectedAccountCollection = ''' 
			{
				"type": "array",
				"items": {
					"$ref": "#/definitions/AccountLink"
				}
			}
		'''
		assertThat(accountCollection, nodeEqualsToJson(expectedAccountCollection))
		
		val accountCollectionItem = definitions.get("AccountLink")
		assertNotNull(accountCollection)
		val expectedAccountCollectionItem = ''' 
			{
				"type": "object",
				"properties": {
					"_links": {
						"$ref": "#/definitions/_RapidLinksMap"
					},
					"accountID": {
						"type": "string"
					},
					"name": {
						"type": "string"
					}
				}
			}
		  '''
		assertEquals(new ObjectMapper().readTree(expectedAccountCollectionItem), accountCollectionItem)
	}

	def String model_embedded_default_realization() {
		'''
			rapidModel Example
				resourceAPI ExampleAPI baseURI "http://my-namespace.com"
					collectionResource AccountCollection type Account
						URI /accounts
						mediaTypes
							application/json
						method GET retrieveAccountCollection
							request
							response with AccountCollection statusCode 200
				dataModel Data
					structure Account
						accountID : string
						name : string
						status : int
						openDate : date
		'''
	}

	@Test
	def void test_embedded_default_realization() throws Exception {
		val zenModel = loadModelAndNormalize(model_embedded_default_realization)

		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createFakeGenTemplateContext)
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)

		val accountObject = definitions.get("Account")
		assertNotNull(accountObject)

		val expectedAccountObject = '''
		{
			"type": "object",
			"properties": {
				"accountID": {
					"type": "string"
				},
				"name": {
					"type": "string"
				},
				"status": {
					"type": "integer"
				},
				"openDate": {
					"type": "string",
					"format": "date"
				}
			}
		}
		'''

		assertThat(accountObject, nodeEqualsToJson(expectedAccountObject))
		
		val accountCollection = definitions.get("AccountCollection")
		assertNotNull(accountCollection)
		val expectedAccountCollection = '''
			{
				"type": "array",
				"items": { 
					"$ref": "#/definitions/Account"
				}
			}
		'''
		assertThat(accountCollection, nodeEqualsToJson(expectedAccountCollection))
	}

	def String model_embedded_collection_realization() {
		'''
			rapidModel Example
				resourceAPI ExampleAPI baseURI "http://my-namespace.com"
					collectionResource AccountCollection type Account
						URI /accounts
						with only properties
							accountID!
							name
								of length from 1 to 20
							status			
						mediaTypes
							application/json
						method GET retrieveAccountCollection
							request
							response with AccountCollection statusCode 200
				dataModel Data
					structure Account
						accountID : string
						name : string
						status : int
						openDate : date
		'''
	}

	@Test
	def void test_embedded_collection_realization() throws Exception {
		val zenModel = loadModelAndNormalize(model_embedded_collection_realization)

		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createFakeGenTemplateContext)
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)

		val accountCollection = definitions.get("AccountCollection")
		assertNotNull(accountCollection)
		val expectedAccountCollection = '''
			{
				"type": "array",
				"items": {
					"$ref": "#/definitions/AccountCollection_item"
				}
			}
	  	'''
		assertThat(accountCollection, nodeEqualsToJson(expectedAccountCollection))

		val accountCollectionItem = definitions.get("AccountCollection_item")
		assertNotNull(accountCollection)
		val expectedAccountCollectionItem = '''
			{
				"type": "object",
				"properties": {
					"accountID": {
						"type": "string"
					},
					"name": {
						"type": "string",
						"minLength": 1,
						"maxLength": 20
					},
					"status": {
						"type": "integer"
					}
				},
				"required": [
					"accountID"
				]
			}
		'''
		assertThat(accountCollectionItem, nodeEqualsToJson(expectedAccountCollectionItem))
	}

	// *******END OF SPECIFICATION BY EXAMPLE TESTS ******///
	@Test
	def void test_CollectionResource_ref_reference_to_definition() throws Exception {
		val zenModel = loadModelAndNormalize(model(true))

		val generator = new XGenerateSwagger()
		generator.init(createFakeGenTemplateContext)
		val swagger = generator.getSwagger(zenModel)

		val resource = swagger.paths.get("/taxFilings")
		assertNotNull(resource)

		assertIsSchemaOfType(resource.get.responses.get('200').schema, "#/definitions/TaxFilingCollection")

		assertIsSchemaOfType(resource.post.responses.get('200').schema, "#/definitions/TaxFilingCollection")
		assertIsSchemaOfType((resource.post.parameters.get(0) as BodyParameter).schema,
			"#/definitions/TaxFilingCollection")
	}

	// @Test
	def void test_definition_for_CollectionResource_ref() throws Exception {
		val zenModel = loadModelAndNormalize(model(true))

		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createFakeGenTemplateContext)
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)
		val taxFilingCollection = definitions.get("TaxFilingCollection")
		assertNotNull(taxFilingCollection)

		val expectedTaxFilingCollection = '''
			{
				"type": "array",
				"items": {
					"type": "object",
					"additionalProperties": false,
					"required": [
						"_rapid_link"
					],
					"properties": {
						"_rapid_link": {
							"type": "object",
							"additionalProperties": true,
							"properties": {
								"rel": {
									"description": "A link relation, identifying the nature and semantics of the hyperlink",
									"type": "string"
								},
								"href": {
									"description": "A URL identifying the target resource for the hyperlink",
									"type": "string",
									"format": "uri"
								},
								"type": {
									"description": "Indicates a media type of the target resource",
									"type": "string"
								},
								"title": {
									"description": "Conveys human-readable information about the link",
									"type": "string"
								}
							},
							"required": [
								"href"
							]
						}
					}
				}
			}
		'''

		assertEquals(new ObjectMapper().readTree(expectedTaxFilingCollection).toString, taxFilingCollection.toString)

	// We cannot access 'definitions' in Swagger, see comments to SchemaRefsTest
	// assertTrue(taxFilingCollection instanceof ArrayProperty)
	// val items = (taxFilingCollection as ArrayProperty).items
	// assertEquals("object", items.type)
	// assertTrue(items instanceof ObjectProperty)
	// val itemProperties = (items as ObjectProperty).properties
	// assertEquals(1, itemProperties.size)
	// val idProperty = itemProperties.get("id")
	// assertNotNull(idProperty)
	// assertEquals("string", idProperty.type)
	}

	@Test
	def void test_CollectionResource_embed_reference_to_definition() throws Exception {
		val zenModel = loadModelAndNormalize(model())

		val generator = new XGenerateSwagger()
		generator.init(createFakeGenTemplateContext)
		val swagger = generator.getSwagger(zenModel)

		val resource = swagger.paths.get("/taxFilings")
		assertNotNull(resource)

		assertIsSchemaOfType(resource.get.responses.get('200').schema, "#/definitions/TaxFilingCollection")

		assertIsSchemaOfType(resource.post.responses.get('200').schema, "#/definitions/TaxFilingCollection")
		assertIsSchemaOfType((resource.post.parameters.get(0) as BodyParameter).schema,
			"#/definitions/TaxFilingCollection")
	}

	@Test
	def void test_definition_for_CollectionResource_embed() throws Exception {
		val zenModel = loadModelAndNormalize(model())

		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createFakeGenTemplateContext)
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)
		val taxFilingCollection = definitions.get("TaxFilingCollection")
		assertNotNull(taxFilingCollection)

		val expectedTaxFilingCollection = '''{"type":"array","items":{"$ref":"#/definitions/TaxFiling"}}'''

		assertEquals(new ObjectMapper().readTree(expectedTaxFilingCollection).toString, taxFilingCollection.toString)
	}

	@Test
	def void test_definition_for_the_this_keyword_in_message() {
		val modelText = '''
			rapidModel Example
				resourceAPI ExampleAPI baseURI "http://my-namespace.com"
					collectionResource AccountCollection type Account
						URI /accounts
						mediaTypes
							application/json
						method GET retrieveAccountCollection
							request
							response with this AccountCollection statusCode 200
							response with AccountCollection statusCode 201
			
				dataModel Data
					structure Account
						id : string
						name : string
		'''
		val zenModel = loadModelAndNormalize(modelText)

		val generator = new XGenerateSwagger()
		generator.init(createFakeGenTemplateContext)

//		val swaggerText = generator.generate(zenModel)
//		println(swaggerText)
		val swagger = generator.getSwagger(zenModel)

		val resource = swagger.paths.get("/accounts")
		assertNotNull(resource)

		assertIsSchemaOfType(resource.get.responses.get('200').schema, "#/definitions/AccountCollection")
		assertIsSchemaOfType(resource.get.responses.get('201').schema, "#/definitions/AccountCollection")

		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createFakeGenTemplateContext)
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)

		val accountCollection = definitions.get("AccountCollection")
		assertNotNull(accountCollection)

		val expectedAccountCollection = '''
			{
				"type" : "array",
				"items" : {
					"$ref" : "#/definitions/Account"
				}
			}
	    '''

		assertEquals(new ObjectMapper().readTree(expectedAccountCollection).toString, accountCollection.toString)

		val account = definitions.get("Account")
		assertNotNull(account)

		val expectedAccount = '''
			{
				"type" : "object",
				"properties" : {
					"id" : {
						"type" : "string"
					},
					"name" : {
						"type" : "string"
					}
				}
			}
	     '''

		assertEquals(new ObjectMapper().readTree(expectedAccount).toString, account.toString)

	}

	def void assertIsSchemaOfType(Property schema, String type) {
		assertEquals(type, (schema as RefProperty).$ref)
	}

	def void assertIsSchemaOfType(Model schema, String type) {
		assertEquals(type, (schema as RefModel).$ref)
	}

	def private createFakeGenTemplateContext() {
		return new FakeGenTemplateContext(
			#{Options::ALLOW_EMPTY_OBJECT -> true, Options::ALLOW_EMPTY_ARRAY -> true,
				Options::ALLOW_EMPTY_STRING -> true})
	}

}
