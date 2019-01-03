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

@SampleRestFile("TestJSONSchema.rapid")
class JsonSchemaBaseTest {

	@Rule public JSONSchemeGeneratorTestFixture fixture = new JSONSchemeGeneratorTestFixture()

	@Test def void baseTest() throws Exception {
		var JsonNode objectNode = fixture.getDefinition("DataType1")
		assertThat(objectNode, nodeEqualsToYaml('''
type: "object"
minProperties: 1
description: "\"comment with quotes and backslash \\\""
properties:
  simplePropString:
    description: "\"comment with quotes and backslash \\\""
    type: "string"
  simplePropBoolean:
    type: "boolean"
  simplePropBase64Binary:
    type: "string"
    format: "byte"
    media:
      binaryEncoding: "base64"
  simplePropDate:
    type: "string"
    format: "date"
  simplePropDateTime:
    type: "string"
    format: "date-time"
  simplePropDecimal:
    type: "number"
  simplePropDouble:
    type: "number"
    format: "double"
  simplePropFloat:
    type: "number"
    format: "float"
  simplePropInteger:
    type: "integer"
  simplePropInt:
    type: "integer"
  simplePropLong:
    type: "integer"
    format: "int64"
  simplePropUnbounded:
    type: "array"
    minItems: 1
    items:
      type: "number"
  simplePropBounded:
    type: "array"
    minItems: 3
    maxItems: 5
    items:
      type: "boolean"
  simpleReadOnlyProperty:
    type: "string"
    readOnly: true
  keyReadOnlyProperty:
      type: "integer"
      readOnly: true
  simpleRef:
    $ref: "#/definitions/DataType2"
  containmentRef:
    $ref: "#/definitions/DataType2"
  simpleRefUnbounded:
    type: "array"
    minItems: 1
    items:
      $ref: "#/definitions/DataType2"
  simpleRefUnboundedMandatory:
    type: "array"
    minItems: 1
    items:
      $ref: "#/definitions/DataType2"
  containmentRefUnbounded:
    type: "array"
    minItems: 1
    items:
      $ref: "#/definitions/DataType2"
  simpleRefBounded:
    type: "array"
    minItems: 3
    maxItems: 5
    items:
      $ref: "#/definitions/DataType2"
  containmentRefBounded:
    type: "array"
    minItems: 8
    maxItems: 15
    items:
      $ref: "#/definitions/DataType2"
required:
- "simplePropBounded"
- "simpleRefUnboundedMandatory"
- "simpleRefBounded"
- "containmentRefBounded"'''))
	}
}
