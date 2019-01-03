package com.reprezen.genflow.rapidml.jsonschema.xchange

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.collect.Lists
import com.google.inject.Inject
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.genflow.common.jsonschema.JsonSchemaFormat
import com.reprezen.genflow.common.jsonschema.Options
import com.reprezen.genflow.common.jsonschema.builder.JsonSchemaNodeFactory
import com.reprezen.genflow.common.jsonschema.builder.xchange.ContractJsonSchemaNodeFactory
import com.reprezen.genflow.common.jsonschema.builder.xchange.InteropJsonSchemaNodeFactory
import com.reprezen.genflow.rapidml.jsonschema.XGenerateJsonSchema
import com.reprezen.genflow.test.common.RapidMLInjectorProvider
import com.reprezen.rapidml.RapidmlPackage
import com.reprezen.rapidml.ZenModel
import com.reprezen.rapidml.implicit.ZenModelNormalizer
import com.reprezen.rapidml.xtext.loaders.ZenModelLoader
import java.util.Map
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.junit4.AbstractXtextTests
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.util.ParseHelper

@InjectWith(typeof(RapidMLInjectorProvider))
abstract class XChangeSchemaTestBase extends AbstractXtextTests {

	@Inject
	ParseHelper<ZenModel> parser

	@Inject
	ResourceSet resourceSet

	new() {
		// TODO
		RapidmlPackage.eINSTANCE.eClass()
	}

	def abstract String rapid_model()

	def void testInterop(Pair<String, String> expectedDefinition) {
		testInterop(expectedDefinition.key, expectedDefinition.value)
	}

	def void testInterop(String definitionName, String expectedNode) {
		val jsonSchemaGenerator = new XGenerateJsonSchema(new InteropJsonSchemaNodeFactory())
		testSpec(jsonSchemaGenerator, definitionName, expectedNode)
	}

	def void testContract(Pair<String, String> expectedDefinition) {
		testContract(expectedDefinition.key, expectedDefinition.value)
	}

	def void testContract(String definitionName, String expectedNode) {
		val jsonSchemaGenerator = new XGenerateJsonSchema(new ContractJsonSchemaNodeFactory())
		testSpec(jsonSchemaGenerator, definitionName, expectedNode)
	}
	
	def void testLegacy(String definitionName, String expectedNode) {
		val jsonSchemaGenerator = new XGenerateJsonSchema(new JsonSchemaNodeFactory(JsonSchemaFormat.SWAGGER))
		val Map<String, Object> legacyParameters = #{Options::ALLOW_EMPTY_OBJECT -> true,
			Options::ALLOW_EMPTY_ARRAY -> true, Options::ALLOW_EMPTY_STRING -> true}
		testSpec(jsonSchemaGenerator, definitionName, expectedNode, legacyParameters)
	}

	def void testSpec(XGenerateJsonSchema jsonSchemaGenerator, String definitionName, String expectedNode) {
		testSpec(jsonSchemaGenerator, definitionName, expectedNode, null)
	}
	
	def void testSpec(XGenerateJsonSchema jsonSchemaGenerator, String definitionName, String expectedNode, Map<String, Object> parameters) {		
		new RapidMLInjectorProvider().injector.injectMembers(this)
		val definitions = requireJsonSchemaDefinitions(jsonSchemaGenerator, parameters)
		val actualDefinitionNode = definitions.get(definitionName)
		if (expectedNode === null) {
			val message = "A definition for " + expectedNode +" should not be generated, but it is:\n" + actualDefinitionNode;
			assertTrue(message, actualDefinitionNode === null || actualDefinitionNode.isMissingNode);
			return;
		}
		assertNotNull("Node with this name does not exist: " + definitionName + ".\nExisting definitions: " +
			Lists.newArrayList(definitions.fieldNames), actualDefinitionNode)
		assertEquals("Definition is not what we expect: ",
			new ObjectMapper(new YAMLFactory()).readTree(expectedNode), actualDefinitionNode)

	}

	def ZenModel loadAndGenerateImplicitValues() {
		val ZenModel rapidModel = if (rapid_model_uri !== null) {
				new ZenModelLoader(resourceSet).loadAndValidateModel(rapid_model_uri)
			} else {
				parser.parse(rapid_model)
			}
		new ZenModelNormalizer().normalize(rapidModel)
		rapidModel
	}
	
	def URI rapid_model_uri() {
		return null;
	}
	
	def requireJsonSchemaDefinitions(XGenerateJsonSchema jsonSchemaGenerator, Map<String, Object> parameters) {
		val zenModel = loadAndGenerateImplicitValues
		val Map<String, Object> defaultParameters = 
				#{Options::ALLOW_EMPTY_OBJECT -> false, Options::ALLOW_EMPTY_ARRAY -> false,
					Options::ALLOW_EMPTY_STRING -> false}

		jsonSchemaGenerator.init(
			new FakeGenTemplateContext(if (parameters !== null) parameters else defaultParameters))
		val String generated = jsonSchemaGenerator.generate(zenModel).toString;
		println("Generated: \n" + generated)
		val jsonSchemasNode = new ObjectMapper().readTree(generated)

		val definitions = jsonSchemasNode.get("definitions")
		assertNotNull(definitions)
		return definitions
	}

}
