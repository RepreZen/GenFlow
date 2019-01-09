package com.reprezen.genflow.rapidml.swagger.test;

import java.io.IOException;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// A variation of com.modelsolv.reprezen.gentemplates.jsonschema.test.help.JSONSchemeGeneratorTestFixture.nodeEqualsToYaml(String)
// Cannot reuse JSONSchemeGeneratorTestFixture.nodeEqualsToYaml directly
// because this plugin and jsonschema.test plugin use different classloaders to load JsonNode class
// swagger.test plugin uses the JsonNode from swagger.test.lib which is needed to validate JSON Schema in SwaggerLoadSamplesTest
// jsonschema.test uses Jackson from Orbit to load JsonNode
public class JsonNodeEqualsToMatcher extends BaseMatcher<JsonNode> {

	public static Matcher<JsonNode> nodeEqualsToJson(final String expectedJson)
			throws JsonProcessingException, IOException {
		return new JsonNodeEqualsToMatcher(expectedJson);
	}

	final JsonNode normalizedExpectedNode;

	public JsonNodeEqualsToMatcher(String expectedYaml) throws JsonProcessingException, IOException {
		normalizedExpectedNode = new ObjectMapper().readTree(expectedYaml);
	}

	@Override
	public boolean matches(Object item) {
		JsonNode node = (JsonNode) item;
		// if (!normalizedExpectedNode.equals(node.toString())) {
		// System.out.println("Expected: for " + normalizedExpectedNode);
		// try {
		// System.out.println(new ObjectMapper(new
		// YAMLFactory()).writeValueAsString(node));
		// } catch (JsonProcessingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		return normalizedExpectedNode.equals(node);
	}

	@Override
	public void describeTo(org.hamcrest.Description description) {
		description.appendText(normalizedExpectedNode.toString());
	}
}
