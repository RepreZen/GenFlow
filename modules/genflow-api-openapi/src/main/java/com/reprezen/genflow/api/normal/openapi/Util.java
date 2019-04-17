package com.reprezen.genflow.api.normal.openapi;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.models.Path;

public class Util {

	/**
	 * Convenience to turn any iterator into an iterable
	 */
	public static <T> Iterable<T> iterable(final Iterator<T> iterator) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}

	public static Optional<String> getRefString(JsonNode node) {
		if (node instanceof ObjectNode) {
			ObjectNode objNode = (ObjectNode) node;
			if (objNode.has("$ref") && objNode.get("$ref").isTextual()) {
				return Optional.of(objNode.get("$ref").asText());
			}
		}
		return Optional.empty();
	}

	public static boolean isRef(JsonNode node) {
		return getRefString(node).isPresent();
	}

	public static JsonNode mkRefNode(String refString) {
		ObjectNode result = JsonNodeFactory.instance.objectNode();
		result.put("$ref", refString);
		return result;
	}

	public static final List<String> swaggerMethodOrder = Arrays
			.asList(Path.class.getAnnotation(JsonPropertyOrder.class).value());

	/**
	 * Quick approximate test to see whether a JSON document is a Swagger spec
	 */
	public static boolean isSwaggerSpec(JsonNode tree) {
		if (!tree.isObject()) {
			return false;
		}
		JsonNode swagger = tree.path("swagger");
		if (!swagger.isTextual()) {
			return false;
		}
		return swagger.asText().equals("2.0");
	}

	public static JsonNode safeDeepCopy(JsonNode node) {
		IdentityHashMap<JsonNode, JsonNode> copies = new IdentityHashMap<>();
		return deepCopy(node, copies);
	}

	private static JsonNode deepCopy(JsonNode node, IdentityHashMap<JsonNode, JsonNode> copies) {
		if (copies.containsKey(node)) {
			return copies.get(node);
		}
		if (node.isObject()) {
			return deepCopyObject((ObjectNode) node, copies);
		} else if (node.isArray()) {
			return deepCopyArray((ArrayNode) node, copies);
		} else {
			return node.deepCopy(); // all other node types are already safe
		}
	}

	private static JsonNode deepCopyObject(ObjectNode node, IdentityHashMap<JsonNode, JsonNode> copies) {
		ObjectNode copy = node.objectNode();
		copies.put(node, copy);
		for (Entry<String, JsonNode> field : iterable(node.fields())) {
			copy.set(field.getKey(), deepCopy(field.getValue(), copies));
		}
		return copy;
	}

	private static JsonNode deepCopyArray(ArrayNode node, IdentityHashMap<JsonNode, JsonNode> copies) {
		ArrayNode copy = node.arrayNode();
		copies.put(node, copy);
		for (JsonNode element : iterable(node.elements())) {
			copy.add(deepCopy(element, copies));
		}
		return copy;
	}
}
