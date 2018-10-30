package com.reprezen.genflow.api.normal.openapi;

import static com.reprezen.genflow.api.normal.openapi.NormalizerDebug.debug;
import static com.reprezen.genflow.api.normal.openapi.NormalizerDebug.Option.SIMPLE_REFS;
import static com.reprezen.genflow.api.normal.openapi.Util.getRefString;
import static com.reprezen.genflow.api.normal.openapi.Util.iterable;

import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SimpleRefFixer {

	public static void fixSimpleRefs(JsonNode root) {
		for (JsonNode param : iterable(root.path("parameters").elements())) {
			fixParameterSimpleRefs(param);
		}
		for (JsonNode path : iterable(root.path("paths").elements())) {
			fixSimpleParameterRefs(path.path("parameters"));
			for (String method : Util.swaggerMethodOrder) {
				JsonNode operation = path.path(method);
				fixSimpleParameterRefs(operation.path("parameters"));
				for (JsonNode parameter : iterable(operation.path("parameters").elements())) {
					if (parameter.path("in").asText().equals("body")) {
						fixSchemaSimpleRefs(parameter.path("schema"));
					}
				}
				for (JsonNode response : iterable(operation.path("responses").elements())) {
					fixResponseSimpleRefs(response);
				}
			}
		}
		for (JsonNode response : iterable(root.path("responses").elements())) {
			fixResponseSimpleRefs(response);
		}
		for (JsonNode definition : iterable(root.path("definitions").elements())) {
			fixSchemaSimpleRefs(definition);
		}
	}

	private static void fixSimpleParameterRefs(JsonNode node) {
		if (node.isMissingNode()) {
			return;
		}
		if (node instanceof ArrayNode) {
			ArrayNode params = (ArrayNode) node;
			for (int i = 0; i < params.size(); i++) {
				fixParameterSimpleRefs(params.get(i));
			}
		}
	}

	private static void fixParameterSimpleRefs(JsonNode parameter) {
		if (parameter.isMissingNode()) {
			return;
		}
		fixSimpleRef(parameter, "parameters");
		if (parameter.path("in").asText().equals("body")) {
			fixSchemaSimpleRefs(parameter.path("schema"));
		}
	}

	private static void fixResponseSimpleRefs(JsonNode response) {
		if (response.isMissingNode()) {
			return;
		}
		fixSimpleRef(response, "responses");
		fixSchemaSimpleRefs(response.path("schema"));
	}

	private static void fixSchemaSimpleRefs(JsonNode schema) {
		if (schema.isMissingNode()) {
			return;
		}
		fixSimpleRef(schema, "definitions");
		fixSchemaSimpleRefs(schema.path("items"));
		fixSchemaSimpleRefs(schema.path("additionalProperties"));
		for (JsonNode property : iterable(schema.path("properties").elements())) {
			fixSchemaSimpleRefs(property);
		}
		for (JsonNode allOf : iterable(schema.path("allOf").elements())) {
			fixSchemaSimpleRefs(allOf);
		}
	}

	private static void fixSimpleRef(JsonNode node, String container) {
		String before = node.path("$ref").asText();
		if (isSimpleRef(node)) {
			String after = String.format("#/%s/%s", container, before);
			debug(SIMPLE_REFS, String.format("Simple Ref: %s => %s", before, after));
			((ObjectNode) node).put("$ref", after);
		} else if (isFauxSimpleRef(node)) {
			// we fix up most ref strings that Swagger20Parser would incorrectly treat as
			// simple refs, by prefixing them
			// with "./". This matters only for unresolvable references, since other
			// references will be replaced as a
			// side-effect of resolution.
			String after = "./" + before;
			debug(SIMPLE_REFS, String.format("Faux Simple Ref: %s => %s", before, after));
			((ObjectNode) node).put("$ref", after);
		}
	}

	// simple ref starts with alpha or underscore, ends with alphanum or underscore,
	// and has alphanums, underscores and
	// hyphens within.
	private static Pattern SIMPLE_REF_PAT = Pattern.compile("[_A-Za-z]([-A-Za-z0-9_]*[_A-Za-z0-9])?");

	private static boolean isSimpleRef(JsonNode node) {
		Optional<String> ref = getRefString(node);
		return ref.isPresent() ? isSimpleRef(ref.get()) : false;
	}

	// ref string that Swagger20Parser does not incorrectly treat as simple ref (as
	// of parser v1.0.19)
	private static Pattern NOT_FAUX_SIMPLE_REF_PAT = Pattern.compile("^(http|\\.|/|#/)");
	private static Pattern SCHEME_PAT = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*:");

	private static boolean isFauxSimpleRef(JsonNode node) {
		Optional<String> ref = getRefString(node);
		if (ref.isPresent()) {
			if (NOT_FAUX_SIMPLE_REF_PAT.matcher(ref.get()).find()) {
				return false;
			}
			// OK, Swagger20Parser will get this wrong, but our fix will be to prepend"./"
			// to it, which is wrong
			// if this is a URL with a scheme. So is what Swagger20Parser will do to it, but
			// at least it's their crap
			// and not ours
			if (SCHEME_PAT.matcher(ref.get()).find()) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	private static boolean isSimpleRef(String ref) {
		return SIMPLE_REF_PAT.matcher(ref).matches();
	}
}
