/** 
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 */
package com.reprezen.genflow.rapidml.jsonschema

import com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemeGeneratorTestFixture
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@SampleRestFile("TestJSONSchema.rapid")
class PrettyPrintTest extends Assert {

	@Rule public JSONSchemeGeneratorTestFixture fixture = new JSONSchemeGeneratorTestFixture()

	@Test def void prettyPrintTest() throws Exception {
		val lines = fixture.json.split("(?m)$")
		assertTrue("Generated schema is formatted in a single line, not pretty-printed", lines.size > 1)
	}
}
