/** 
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 */
package com.reprezen.genflow.rapidml.jsonschema

import com.fasterxml.jackson.databind.JsonNode
import com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemaKeywords
import com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemeGeneratorTestFixture
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile
import org.junit.Rule
import org.junit.Test

import static org.junit.Assert.*
import static com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemeGeneratorTestFixture.*

@SampleRestFile("Enumerations.rapid") 
class EnumTest {
	@Rule public JSONSchemeGeneratorTestFixture fixture = new JSONSchemeGeneratorTestFixture()

	@Test def void testIsValidJSONSchema() throws Exception {
		fixture.isValidJsonSchema()
	}

	@Test def void checkIntEnumDefinion() throws Exception {
		var JsonNode ^def = fixture.getDefinition("Record")
		// $NON-NLS-1$
		JSONSchemeGeneratorTestFixture.checkFieldValue(JSONSchemaKeywords.TYPE, ^def, JSONSchemaKeywords.TYPE_INTEGER)
		JSONSchemeGeneratorTestFixture.checkFieldValue(JSONSchemaKeywords.DESCRIPTION, ^def, "Integer enumeration")
		// $NON-NLS-1$
		JSONSchemeGeneratorTestFixture.checkFieldValue(JSONSchemaKeywords.ENUM, ^def,
			#[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13])
	}

	@Test def void checkStringEnumDefinion() throws Exception {
		var JsonNode ^def = fixture.getDefinition("Suit")
		// $NON-NLS-1$
		JSONSchemeGeneratorTestFixture.checkFieldValue(JSONSchemaKeywords.TYPE, ^def, JSONSchemaKeywords.TYPE_STRING)
		JSONSchemeGeneratorTestFixture.checkFieldValue(JSONSchemaKeywords.DESCRIPTION, ^def, "String enumeration")
		// $NON-NLS-1$ 
		JSONSchemeGeneratorTestFixture.checkFieldValue(JSONSchemaKeywords.ENUM, ^def,
			#["clubs", "spades", "diamonds", "hearts"]) // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	@Test def void checkEnumField() throws Exception {
		var JsonNode objectNode = fixture.getDefinition("UseEnum")
		assertThat(objectNode, nodeEqualsToYaml('''
type: "object"
minProperties: 1
properties:
  useIntEnum:
    $ref: "#/definitions/Record"
  useStringEnum:
    $ref: "#/definitions/Suit"'''))
	}
}
