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

@SampleRestFile("constraints/ResourceRealizationConstraints.rapid")
class ConstraintsOverrideTest {
	@Rule public JSONSchemeGeneratorTestFixture fixture = new JSONSchemeGeneratorTestFixture()

	@Test def void testIsValidJSONSchema() throws Exception {
		fixture.isValidJsonSchema()
	}

	@Test def void constraintsTest() throws Exception {
		var JsonNode objectNode = fixture.getDefinition("MyObject")
		assertThat(objectNode, nodeEqualsToYaml('''
type: "object"
minProperties: 1
properties:
  stringProp1:
    type: "string"
    minLength: 5
    maxLength: 10
    pattern: "^[A-Za-z0-9\\w]*$"
  stringProp2:
    type: "string"
    pattern: "^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})+$"
    maxLength: 10
  stringProp3:
    type: "string"
    minLength: 10
  intProp1:
    type: "integer"
    minimum: 2
    maximum: 8
  doubleProp1:
    type: "number"
    format: "double"
    minimum: -1.23
    maximum: 1.23'''))

	}
}
