/** 
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 */
package com.reprezen.genflow.rapidml.jsonschema

import com.fasterxml.jackson.databind.JsonNode
import com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemeGeneratorTestFixture
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile
import org.junit.Rule
import org.junit.Test

import static org.junit.Assert.*
import static com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemeGeneratorTestFixture.*

@SampleRestFile("perspective/ReferenceEmbed.rapid")
class ReferenceEmbedTest {
	@Rule public JSONSchemeGeneratorTestFixture fixture = new JSONSchemeGeneratorTestFixture()

	@Test def void testIsValidJSONSchema() throws Exception {
		fixture.isValidJsonSchema()
	}

	@Test
	def void testReferenceEmbed() throws Exception {
		var JsonNode refEmbed = fixture.getDefinition("ReferenceEmbedObject")
		assertThat(refEmbed, nodeEqualsToYaml('''
type: object
minProperties: 1
properties:
  prop1:
    type: string
  prop2:
    type: string
  order:
    "$ref": "#/definitions/ReferenceEmbedObject_order"
  target:
    "$ref": "#/definitions/TargetDataTypeObject_link"'''))
	}

	@Test
	def void testReferenceEmbedObject_order() throws Exception {
		var JsonNode refEmbed = fixture.getDefinition("ReferenceEmbedObject_order")
		assertThat(refEmbed, nodeEqualsToYaml('''
type: "object"
minProperties: 1
properties:
  orderID:
    type: "string"
  orderDate:
    type: "string"
  lineItems:
    $ref: "#/definitions/ReferenceEmbedObject_order_lineItems"'''))
	}

	@Test
	def void testTargetDataTypeObject_link() throws Exception {
		var JsonNode refEmbed = fixture.getDefinition("TargetDataTypeObject_link")
		assertThat(refEmbed, nodeEqualsToYaml('''
type: "object"
minProperties: 1
properties:
  _links:
    $ref: "#/definitions/_RapidLinksMap"'''))
	}
}
