package com.reprezen.genflow.api.normal.openapi;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SwaggerXExamplesFixer {

	private ObjectWriter prettyPrinter = (new ObjectMapper()).writerWithDefaultPrettyPrinter();
	private JsonNode model;

	public SwaggerXExamplesFixer(JsonNode model) {
		this.model = model;
	}

	public void fixXExamples() throws IOException {
		JsonNode paths = model.path("paths");
		if (!paths.isMissingNode()) {
			for (Entry<String, JsonNode> entry : iterable(paths.fields())) {
				fixExamplesInPath(entry.getValue());
			}
		}
		JsonNode rootParams = model.path("parameters");
		fixExamplesInParameterMap(rootParams);
	}

	private void fixExamplesInPath(JsonNode path) throws IOException {
		for (Entry<String, JsonNode> entry : iterable(path.fields())) {
			fixExamplesInMethod(entry.getValue());
		}
	}

	private void fixExamplesInMethod(JsonNode method) throws IOException {
		fixExamplesInParameterList(method.path("parameters"));
	}

	private void fixExamplesInParameterList(JsonNode parameters) throws IOException {
		if (!parameters.isMissingNode()) {
			for (JsonNode param : iterable(parameters.elements())) {
				fixExamplesInParameter(param);
			}
		}
	}

	private void fixExamplesInParameterMap(JsonNode parameters) throws IOException {
		if (!parameters.isMissingNode()) {
			for (Entry<String, JsonNode> entry : iterable(parameters.fields())) {
				JsonNode param = entry.getValue();
				fixExamplesInParameter(param);
			}
		}
	}

	private void fixExamplesInParameter(JsonNode param) throws IOException {
		if (!param.isMissingNode()) {
			JsonNode examples = param.path("x-examples");
			fixExamplesInXExamples(examples);
		}
	}

	private void fixExamplesInXExamples(JsonNode examples) throws IOException {
		if (!examples.isMissingNode()) {
			ObjectNode examplesObject = (ObjectNode) examples;
			for (Entry<String, JsonNode> entry : iterable(examples.fields())) {
				examplesObject.replace(entry.getKey(), fixExample(entry.getValue()));
			}
		}
	}

	private JsonNode fixExample(JsonNode example) throws IOException {
		if (example.isTextual()) {
			return example;
		} else {
			String text;
			try {
				text = prettyPrinter.writeValueAsString(example);
				return JsonNodeFactory.instance.textNode(text);
			} catch (JsonProcessingException e) {
				throw new IOException("Failed to convert x-examples value to text: " + example.toString(), e);
			}
		}
	}

	private <T> Iterable<T> iterable(final Iterator<T> iterator) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}
}
