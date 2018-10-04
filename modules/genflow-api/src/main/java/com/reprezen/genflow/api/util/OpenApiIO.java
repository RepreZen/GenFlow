package com.reprezen.genflow.api.util;

import java.io.IOException;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.reprezen.genflow.api.GenerationException;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

public class OpenApiIO {

	private static ObjectMapper mapper = getNullKeyHandlingMapper();

	public static Swagger loadSwagger(String data) throws GenerationException {
		return toSwagger(loadSwaggerTree(data, true));
	}

	public static JsonNode loadSwaggerTree(String data) throws GenerationException {
		return loadSwaggerTree(data, true);
	}

	public static JsonNode loadSwaggerTree(String data, boolean validate) throws GenerationException {
		JsonNode tree = loadTree(data);
		if (!validate || tree.has("swagger")) {
			return tree;
		} else {
			throw new GenerationException("Invalid Swagger model: does not contain 'swagger' property");
		}
	}

	public static JsonNode loadOpenApi3Tree(String data) throws GenerationException {
		return loadOpenApi3Tree(data, true);
	}

	public static JsonNode loadOpenApi3Tree(String data, boolean validate) throws GenerationException {
		JsonNode tree = loadTree(data);
		if (!validate || tree.has("openapi")) {
			return tree;
		} else {
			throw new GenerationException("Invalid OpenAPI3 model:does not contain 'openapi' property");
		}
	}

	private static JsonNode loadTree(String data) throws GenerationException {
		JsonNode tree = null;
		if (data != null) {
			String trimmed = data.trim();
			tree = trimmed.startsWith("{") ? parseJson(data) : parseYaml(data);
		}
		return tree;
	}

	private static Swagger toSwagger(JsonNode tree) {
		return new SwaggerParser().read(tree, true);
	}

	private static JsonNode parseJson(String json) throws GenerationException {
		try {
			return mapper.readTree(json);
		} catch (IOException e) {
			throw new GenerationException("Failed to parse JSON file", e);
		}
	}

	private static JsonNode parseYaml(String yaml) {
		// two-step process handles aliases and refs, which Jackson's YAML mapper alone
		// does not
		Object parsed = new Yaml().load(yaml);
		return mapper.convertValue(parsed, JsonNode.class);
	}

	private static ObjectMapper getNullKeyHandlingMapper() {
		// standard provider chokes on null key values, but we'd rather incorporate them
		// as "null"
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializerProvider().setNullKeySerializer(new JsonSerializer<Object>() {
			@Override
			public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
					throws IOException, JsonProcessingException {
				gen.writeFieldName("null");
			}
		});
		return mapper;
	}
}
