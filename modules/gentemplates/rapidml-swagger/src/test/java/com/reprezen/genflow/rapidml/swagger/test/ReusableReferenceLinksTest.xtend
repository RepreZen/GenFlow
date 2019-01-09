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
import com.reprezen.genflow.common.jsonschema.Options
import com.reprezen.genflow.rapidml.jsonschema.XGenerateJsonSchema
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import com.reprezen.rapidml.implicit.ZenModelNormalizer
import io.swagger.models.Model
import io.swagger.models.RefModel
import io.swagger.models.properties.Property
import io.swagger.models.properties.RefProperty
import org.junit.Test

import static com.reprezen.genflow.rapidml.swagger.test.JsonNodeEqualsToMatcher.nodeEqualsToJson

class ReusableReferenceLinksTest extends SwaggerGenTestBase {

	@Test
	def void printModel() throws Exception {
		val zenModel = loadModelAndNormalize(no_link_descriptor(linkDescriptorOnTarget))

		val generator = new XGenerateSwagger()
		generator.init(createGenTemplateContext())

		val swaggerText = generator.generate(zenModel)
		println(swaggerText)
	}

	def String linkDescriptorOnTarget() {
		'''
		linkDescriptor LinkDescriptorOnTarget
			id'''
	}

	def String no_link_descriptor(String linkDescriptorOnTarget) {
		'''
			rapidModel ReuseIncomingHyperlinks
				resourceAPI ReuseIncomingHyperlinksAPI baseURI "http://my-namespace.com"
			
					objectResource ReferencedDataTypeObject type ReferencedDataType
						URI /uri/{id}
						«linkDescriptorOnTarget»
			
					objectResource DataType1Object type DataType1
						URI /uri/{id}
						method GET getDataType1Object
							request
							response DataType1Object statusCode 200
					
					objectResource DataType2Object type DataType2
						URI /uri/{id}
						referenceEmbed > ref1
						method GET getDataType2Object
							request
							response DataType1Object statusCode 200
			
			
				dataModel ReuseIncomingHyperlinksDataModel
					structure ReferencedDataType
						id : string
						prop2: string
					structure DataType1
						id : string
						ref: reference to ReferencedDataType
					structure DataType2
						id : string
						ref1: reference to DataType1
						ref2: reference to DataType2
		'''
	}

	@Test
	def void test_no_link_descriptor() throws Exception {
		val zenModel = loadModelAndNormalize(no_link_descriptor(""))

		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createGenTemplateContext())
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)

		val RAPIDLinkObject = definitions.get("_RapidLinksMap")
		assertNotNull(RAPIDLinkObject)

		// definitions for the links to the resources
		val referencedDataTypeObject_link = definitions.get("ReferencedDataTypeObject_link")
		assertNotNull(referencedDataTypeObject_link)
		val expectedReferencedDataTypeObject_link = '''
			{
				"type" : "object",
				"properties" : {
					"_links" : {
						"$ref" : "#/definitions/_RapidLinksMap"
					}
				}
			}
		  '''
		assertThat(referencedDataTypeObject_link, nodeEqualsToJson(expectedReferencedDataTypeObject_link))

		// usages of this definitions for the hyperlink	
		val dataType1 = definitions.get("DataType1")
		assertNotNull(dataType1)
		val expectedDataType1 = '''
			{
				"type" : "object",
				"properties" : {
					"id" : {
						"type" : "string"
					},
					"ref" : {
						"$ref" : "#/definitions/ReferencedDataTypeObject_link"
					}
				}
			}
		   '''
		assertThat(dataType1, nodeEqualsToJson(expectedDataType1))

		// nested reference
		val dataType2 = definitions.get("DataType2Object")
		val ref1name = "DataType1"
		assertNotNull(dataType2)
		val expectedDataType2 = '''
			{
				"type" : "object",
				"properties" : {
					"id" : {
						"type" : "string"
					},
					"ref1" : {
						"$ref":"#/definitions/«ref1name»"
					},
					"ref2" : {
						"$ref" : "#/definitions/DataType2Object_link"
						     }
				}
			}
		  '''
		assertThat(dataType2, nodeEqualsToJson(expectedDataType2))
	}

	@Test
	def void test_link_descriptor_on_target() throws Exception {
		val zenModel = loadModelAndNormalize(no_link_descriptor(linkDescriptorOnTarget))
		new ZenModelNormalizer().normalize(zenModel)

		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createGenTemplateContext())
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)

		val RAPIDLinkObject = definitions.get("_RapidLinksMap")
		assertNotNull(RAPIDLinkObject)

		// definitions for the links to the resources
		val referencedDataTypeObject_link = definitions.get("LinkDescriptorOnTarget")
		assertNotNull(referencedDataTypeObject_link)
		// note that it now has the id property 
		val expectedReferencedDataTypeObject_link = '''
			{
				"type" : "object",
				"properties" : {
					"_links" : {
						"$ref" : "#/definitions/_RapidLinksMap"
					},
					"id" : {
						"type" : "string"
					}
				}
			}
		   '''
		assertThat(referencedDataTypeObject_link, nodeEqualsToJson(expectedReferencedDataTypeObject_link))

		// usages of this definitions for the hyperlink	
		val dataType1 = definitions.get("DataType1")
		assertNotNull(dataType1)
		val expectedDataType1 = '''
			{
				"type" : "object",
				"properties" : {
					"id" : {
						"type" : "string"
					},
					"ref" : {
						"$ref" : "#/definitions/LinkDescriptorOnTarget"
					}
				}
			}
		  '''
		assertThat(dataType1, nodeEqualsToJson(expectedDataType1))

		// nested reference - the same
		val dataType2 = definitions.get("DataType2Object")
		assertNotNull(dataType2)
		val expectedDataType2 = '''
			{
				"type" : "object",
				"properties" : {
					"id" : {
						"type" : "string"
					},
					"ref1" : {
						"$ref":"#/definitions/DataType1"
					},
					"ref2" : {
						"$ref" : "#/definitions/DataType2Object_link"
					}
				}
			}
	    '''
		assertThat(dataType2, nodeEqualsToJson(expectedDataType2))
	}

	@Test
	def void test_link_to_nested() {
		val model = '''
			rapidModel Nested
				resourceAPI NestedAPI baseURI "http://my-namespace.com"
					objectResource NestedObject type DataType1
						URI /uri
						method GET getNestedObject
							request
							response NestedObject statusCode 200
						
			
				dataModel NestedDataModel
					structure Nested
						id : string
					structure DataType1
						prop1: string
						refToNested: reference to Nested
		'''

		val zenModel = loadModelAndNormalize(model)

//		val generator = new XGenerateSwagger()
//		generator.init()
//
//		val swaggerText = generator.generate(zenModel)
		// println(swaggerText)
		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createGenTemplateContext())
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)

		val datatype1 = definitions.get("DataType1")
		assertNotNull(datatype1)
		val expectedDataType1 = '''
			{
				"type" : "object",
				"properties" : {
					"prop1" : {
						"type" : "string"
					},
					"refToNested" : {
						"$ref" : "#/definitions/Nested"
					}
				}
			}
	    '''
		assertThat(datatype1, nodeEqualsToJson(expectedDataType1))

		val nested = definitions.get("Nested")
		assertNotNull(nested)
		val expectedNested = '''
			{
				"type" : "object",
				"properties" : {
					"id" : {
						"type" : "string"
					}
				}
			}
		'''
		assertThat(nested, nodeEqualsToJson(expectedNested))
	}

	@Test
	def void test_link_to_nested_second_level() {
		val model = '''
			rapidModel Nested
				resourceAPI NestedAPI baseURI "http://my-namespace.com"
					objectResource ParentObject type Parent
						URI /uri
						method GET getElement
							request
							response ParentObject statusCode 200
						
			
				dataModel NestedDataModel
					structure Parent
						id : string
						refToLevel1: reference to Level1
					structure Level1
						prop1: string
						refToLevel2: reference to Level2
					structure Level2
						prop1: string
		'''

		val zenModel = loadModelAndNormalize(model)

//		val generator = new XGenerateSwagger()
//		generator.init(createGenTemplateContext())
//
//		val swaggerText = generator.generate(zenModel)
//		println(swaggerText)
		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createGenTemplateContext())
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)

		val parent = definitions.get("Parent")
		assertNotNull(parent)
		val expectedParent = '''
			{
				"type" : "object",
				"properties" : {
					"id" : {
						"type" : "string"
					},
					"refToLevel1" : {
						"$ref" : "#/definitions/Level1"
					}
				}
			}
		'''
		assertThat(parent, nodeEqualsToJson(expectedParent))

		val level1 = definitions.get("Level1")
		assertNotNull(level1)
		val expectedLevel1 = '''
			{
				"type" : "object",
				"properties" : {
					"prop1" : {
						"type" : "string"
					},
					"refToLevel2" : {
						"$ref" : "#/definitions/Level2"
					}
				}
			}
		'''
		assertThat(level1, nodeEqualsToJson(expectedLevel1))

		val level2 = definitions.get("Level2")
		assertNotNull(level2)
		val expectedLevel2 = '''
			{
				"type" : "object",
				"properties" : {
					"prop1" : {
						"type" : "string"
					}
				}
			}
	    '''
		assertThat(level2, nodeEqualsToJson(expectedLevel2))
	}

	@Test
	def void test_link_to_nested_second_level_via_multi_property() {
		val model = '''
			rapidModel Nested
				resourceAPI NestedAPI baseURI "http://my-namespace.com"
					objectResource ParentObject type Parent
						URI /uri
						method GET getElement
							request
							response ParentObject statusCode 200
						
			
				dataModel NestedDataModel
					structure Parent
						id : string
						refToLevel1: reference to Level1
					structure Level1
						prop1: string
						refToLevel2_multi: reference to Level2*
					structure Level2
						prop1: string
		'''

		val zenModel = loadModelAndNormalize(model)

//		val generator = new XGenerateSwagger()
//		generator.init(createGenTemplateContext())
//
//		val swaggerText = generator.generate(zenModel)
//		println(swaggerText)
		val jsonSchemaGenerator = new XGenerateJsonSchema(JsonSchemaFormat::SWAGGER)
		jsonSchemaGenerator.init(createGenTemplateContext())
		val jsonSchemasNode = new ObjectMapper().readTree(jsonSchemaGenerator.generate(zenModel).toString)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)

		val parent = definitions.get("Parent")
		assertNotNull(parent)
		val expectedParent = '''
			{
				"type" : "object",
				"properties" : {
					"id" : {
						"type" : "string"
					},
					"refToLevel1" : {
						"$ref" : "#/definitions/Level1"
					}
				}
			}
	    '''
		assertThat(parent, nodeEqualsToJson(expectedParent))

		val level1 = definitions.get("Level1")
		assertNotNull(level1)
		val expectedLevel1 = '''
			{
				"type" : "object",
				"properties" : {
					"prop1" : {
						"type" : "string"
					},
					"refToLevel2_multi" : {
						"type" : "array",
						"items" : {
							"$ref" : "#/definitions/Level2"
						}
					}
				}
			}
	    '''
		assertThat(level1, nodeEqualsToJson(expectedLevel1))

		val level2 = definitions.get("Level2")
		assertNotNull(level2)
		val expectedLevel2 = '''
			{
				"type" : "object",
				"properties" : {
					"prop1" : {
						"type" : "string"
					}
				}
			}
		'''
		assertThat(level2, nodeEqualsToJson(expectedLevel2))
	}

	protected def FakeGenTemplateContext createGenTemplateContext() {
		new FakeGenTemplateContext(
			#{Options::ALLOW_EMPTY_OBJECT -> true, Options::ALLOW_EMPTY_ARRAY -> true,
				Options::ALLOW_EMPTY_STRING -> true})
	}

	def void assertIsSchemaOfType(Property schema, String type) {
		assertEquals(type, (schema as RefProperty).$ref)
	}

	def void assertIsSchemaOfType(Model schema, String type) {
		assertEquals(type, (schema as RefModel).$ref)
	}

}
