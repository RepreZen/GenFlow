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

@SampleRestFile("perspective/ReferenceLinks.rapid") 
class ReferenceLinkTest {
	@Rule public JSONSchemeGeneratorTestFixture fixture = new JSONSchemeGeneratorTestFixture()

	@Test 
	def void testIsValidJSONSchema() throws Exception {
		fixture.isValidJsonSchema()
	}

	@Test 
	def void testRootWithLevel1Link() throws Exception {
		var JsonNode definition = fixture.getDefinition("RootWithLevel1Link") // $NON-NLS-1$
		assertThat(definition, nodeEqualsToYaml('''
type: "object"
minProperties: 1
properties:
  prop1:
    type: "string"
  prop2:
    type: "string"
  linked:
    type: "object"
    minProperties: 1
    properties:
      _links:
        $ref: "#/definitions/_RapidLinksMap"
      linkedProp1:
        type: "string"'''))
	}

	@Test 
	def void testRootWithLevel1LinkMultivalued() throws Exception {
		var JsonNode definition = fixture.getDefinition("RootWithLevel1LinkMultivalued") // $NON-NLS-1$
		assertThat(definition, nodeEqualsToYaml('''
type: "object"
minProperties: 1
properties:
  prop1:
    type: "string"
  prop2:
    type: "string"
  linkedMultiValued:
    type: "array"
    minItems: 1
    items:
      type: "object"
      minProperties: 1
      properties:
        _links:
          $ref: "#/definitions/_RapidLinksMap"
        linkedProp2:
          type: "string"'''))
	}

	@Test 
	def void testRootWithLevel12Link() throws Exception {
		var JsonNode definition = fixture.getDefinition("RootWithLevel12Link") // $NON-NLS-1$
		assertThat(definition, nodeEqualsToYaml('''
type: "object"
minProperties: 1
properties:
  prop1:
    type: "string"
  prop2:
    type: "string"
  nested:
    $ref: "#/definitions/RootWithLevel12Link_nested"'''))
	}
}
