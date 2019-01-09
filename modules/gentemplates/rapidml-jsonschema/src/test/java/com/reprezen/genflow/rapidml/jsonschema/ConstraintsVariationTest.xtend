/** 
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 */
package com.reprezen.genflow.rapidml.jsonschema

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemaKeywords
import com.reprezen.genflow.test.common.RapidMLInjectorProvider
import com.reprezen.rapidml.ZenModel
import com.reprezen.rapidml.implicit.ZenModelNormalizer
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*
import static com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemeGeneratorTestFixture.*

@InjectWith(RapidMLInjectorProvider)
@RunWith(XtextRunner)
class ConstraintsVariationTest {
	@Inject package ParseHelper<ZenModel> modelParser

	@Test def void stringLength_FromTest() throws Exception {
		checkConstraints("string", "length from 1", //
		'''
		prop:
		  type: string
		  minLength: 1''')
	}

	@Test def void stringLength_ToTest() throws Exception {
		checkConstraints("string", "length to 10", //
		'''
		prop:
		  type: string
		  maxLength: 10''')
	}

	@Test def void stringLength_FromToEqualsTest() throws Exception {
		checkConstraints("string", "length from 10 to 10", //
		'''
		prop:
		  type: string
		  minLength: 10
		  maxLength: 10''')
	}

	@Test def void stringLength_FromToTest() throws Exception {
		checkConstraints("string", "length from 0 to 10", //
		'''
		prop:
		  type: string
		  minLength: 0
		  maxLength: 10''')
	}

	@Test def void regexTest() throws Exception {
		checkConstraints("string", "matching regex \"[A-Z]+\"", //
		'''
		prop:
		  type: string
		  pattern: "^[A-Z]+$"''')
	}

	@Test def void valueRange_FromExToIncTest() throws Exception {
		checkConstraints("double", "valueRange from '1.0' exclusive to '2.0' inclusive", //
		'''
		prop:
		  type: number
		  format: double
		  minimum: 1
		  exclusiveMinimum: true
		  maximum: 2''')
	}

	@Test def void valueRange_FromIncToExTest() throws Exception {
		checkConstraints("double", "valueRange from '1.0' inclusive to '2.0' exclusive", //
		'''
			prop:
			  type: number
			  format: double
			  minimum: 1
			  maximum: 2
			  exclusiveMaximum: true
		 ''')
	}

	@Test def void valueRange_FromTest() throws Exception {
		checkConstraints("double", "valueRange from minimum '1.0'", //
		'''
		prop:
		  type: number
		  format: double
		  minimum: 1''')
	}

	@Test def void valueRange_ToTest() throws Exception {
		checkConstraints("double", "valueRange up to '2.0'", //
		'''
		prop:
		  type: number
		  format: double
		  maximum: 2''')
	}

	def private void checkConstraints(String type, String constraint, String expectedNodeInYaml) throws Exception {
		var JsonNode structureNode = getStructureDefinition(generateJsonSchema(type, constraint))
		assertNotNull('''Cannot find a JSON Schema definition for «constraint»''', structureNode)

		val propertiesNode = structureNode.get("properties")
		assertNotNull("The 'properties' node does not exist", propertiesNode)

		val propertyNode = propertiesNode.get("prop")
		assertNotNull("JSON property for 'prop' does not exist", propertyNode)

		assertThat(propertiesNode, nodeEqualsToYaml(expectedNodeInYaml))
	}

	def private String generateJsonSchema(String type, String constraint) {
		var ZenModel zenModel = modelParser.parse(getModel(type, constraint))
		new ZenModelNormalizer().normalize(zenModel)

		var XGenerateJsonSchema generator = new XGenerateJsonSchema()
		generator.init(new FakeGenTemplateContext())
		var String result = generator.generate(zenModel)
		return result
	}

	def private JsonNode getStructureDefinition(String jsonSchema) throws Exception {
		var JsonFactory factory = new JsonFactory()
		var JsonParser parser = factory.createParser(jsonSchema)
		var ObjectMapper mapper = new ObjectMapper()
		var JsonNode jsonSchemeRoot = mapper.readTree(parser)

		var JsonNode definitionsRoot = jsonSchemeRoot.get(JSONSchemaKeywords.DEFINITIONS_FIELD_NAME)
		Assert.assertNotNull('''"«JSONSchemaKeywords.DEFINITIONS_FIELD_NAME»" schema field not found''',
			definitionsRoot)
		// $NON-NLS-1$ //$NON-NLS-2$
		var JsonNode definition = definitionsRoot.get("Structure")
		Assert.assertNotNull("Definition \"Structure\" not found", definition)
		// $NON-NLS-1$ //$NON-NLS-2$
		return definition
	}

	/** 
	 * @param constraints
	 * @return
	 */
	def private String getModel(String type, String constraint) {
		var StringBuilder sb = new StringBuilder()
		sb.append("rapidModel ResourceRealizationConstraints \n")
		sb.append("\tresourceAPI ResourceAPI baseURI \"baseURI\"\n")
		sb.append("\t\tobjectResource MyObject type Structure\n")
		sb.append("\tdataModel DataModel\n")
		sb.append("\t\tstructure Structure\n")
		sb.append('''			prop: «type»
''')
		sb.append('''				«constraint»
''')
		return sb.toString()
	}
}
