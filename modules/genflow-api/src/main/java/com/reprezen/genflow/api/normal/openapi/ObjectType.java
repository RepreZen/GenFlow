package com.reprezen.genflow.api.normal.openapi;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Enum defining all the various model object types defined in OpenApi
 * specifications. These are objects with specific container nodes provided in
 * the model to make the objects reusable by reference. Normalizer manages other
 * references as well by inlining them, but object references may also be
 * localizezd, based on options.
 * 
 * Each value is configured with one or more container paths, defining where the
 * container appears in models. Each container path is associated with an
 * OpenApi version to which it applies.
 * 
 * @author Andy Lowry
 *
 */
public enum ObjectType {
	// v2 object types
	PATH(2, "/paths", 3, "/paths"), //
	DEFINITION(2, "/definitions"), //
	RESPONSE(2, "/responses", 3, "/components/responses"), //
	PARAMETER(2, "/parameters", 3, "/components/parameters"), //

	// v3 types not appearing in v2
	SCHEMA(3, "/components/schemas"), //
	EXAMPLE(3, "/components/examples"), //
	REQUEST_BODY(3, "/components/requestBodies"), //
	HEADER(3, "/components/headers"), //
	SECURITY_SCHEME(3, "/components/securitySchemes"), //
	LINK(3, "/components/links"), //
	CALLBACK(3, "/components/callbacks");

	public static final Integer SWAGGER_MODEL_VERSION = 2;
	public static final Integer OPENAPI3_MODEL_VERSION = 3;

	private static Map<Integer, Set<ObjectType>> typesByVersion = Maps.newHashMap();
	private Map<Integer, PathInfo> paths = Maps.newHashMap();

	/**
	 * Define container paths for an object.
	 * 
	 * @param pairs a model version number followed by a container path, repeated
	 *              for all relevant versions
	 */
	private ObjectType(Object... pairs) {
		if ((pairs.length % 2) != 0) {
			throw new IllegalArgumentException("Invalid ObjectType declaration");
		}
		for (int i = 0; i < pairs.length; i += 2) {
			try {
				this.paths.put((Integer) pairs[i], new PathInfo((String) pairs[i + 1]));
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Invalid ObjectType declaration", e);
			}
		}
	}

	static {
		for (ObjectType value : values()) {
			for (Integer version : value.paths.keySet()) {
				if (!typesByVersion.containsKey(version)) {
					typesByVersion.put(version, Sets.newHashSet());
				}
				typesByVersion.get(version).add(value);
			}
		}
	}

	public String getPathString(int version) {
		return paths.get(version).getPathString();
	}

	public JsonPointer getPath(int version) {
		return paths.get(version).getPath();
	}

	/**
	 * Tests whether the given $ref URL fragment could apply to an object of this
	 * type, meaning that it consists of this object type's container path followed
	 * by an object name
	 * 
	 * @param refFragment the fragment portion of a $ref node URL
	 * @param version     model version
	 * @return
	 */
	public boolean matches(String refFragment, int version) {
		return paths.get(version).matches(refFragment);
	}

	/**
	 * Extracts the object name from the given $ref URL fragment, assuming that
	 * fragment matches this model object type
	 * 
	 * @param refFragment the fragment portion of a $ref node URL
	 * @param version     model version
	 * @return the object name (i.e. property name in the container object)
	 */
	public String getObjectName(String refFragment, int version) {
		return paths.get(version).getObjectName(refFragment);
	}

	/**
	 * Determines whether the given top-level property name is the first componnet
	 * of any container paths for the given model version. This supports a simple
	 * but not entirely general method of determining whether a property appearing
	 * in the primary source model should be copied as-is into the normalized model.
	 * Assumption is that if it's a contianer prefix, then it should not be. This
	 * works for v2 models, but not quite for v3 models, where vendor extensions of
	 * the /components value need to be handled specially. A more general solution
	 * would require walking the tree to some extent to find nodes that are not
	 * within any containers, and copying them as-is as they are encountered.
	 * 
	 * @param propertyName top-level property name appearing in model
	 * @param version      model version
	 * @return true if the property is on the path to any model object container
	 */
	public static boolean isObjectContainerPrefix(String propertyName, int version) {
		return typesByVersion.get(version).stream()
				.anyMatch(type -> type.getPath(version).getMatchingProperty().equals(propertyName));
	}

	/**
	 * Obtains the value of this object container from the given tree
	 * 
	 * @param tree    the model tree
	 * @param version the model version
	 * @return container value, including all objects of this model object type
	 */
	public JsonNode getFromNode(JsonNode tree, int version) {
		return tree.at(paths.get(version).getPath());
	}

	/**
	 * Sets the given value into the given tree at the position required for this
	 * object type.
	 * 
	 * Creates object nodes and array nodes on the way as needed, and replaces any
	 * existing value.
	 * 
	 * @param tree    the tree into which the value is inserted
	 * @param value   the value to be inserted
	 * @param version model version number, controlling which container path is used
	 */
	public void setInNode(JsonNode tree, JsonNode value, int version) {
		JsonPointer p = paths.get(version).getPath();
		try {
			while (!p.matches()) {
				int i = p.getMatchingIndex();
				if (i >= 0) {
					if (p.tail().matches() || !tree.has(i)) {
						((ArrayNode) tree).set(i, buildTree(p.tail(), value));
						return;
					} else {
						tree = ((ArrayNode) tree).get(i);
					}
				} else {
					String prop = p.getMatchingProperty();
					if (p.tail().matches() || !tree.has(prop)) {
						((ObjectNode) tree).set(prop, buildTree(p.tail(), value));
						return;
					} else {
						tree = ((ObjectNode) tree).get(prop);
						p = p.tail();
					}
				}
			}
		} catch (ClassCastException e) {

		}
	}

	/**
	 * Build a JsonNode tree to contain the given value at the given JsonPointer
	 * path. Empty array and object nodes are created and nested as needed.
	 * 
	 * @param p     a JsonPointer indicating where the value is to appear in the
	 *              resulting structure
	 * @param value the value to embed in the structure
	 * @return the created structure
	 */
	private JsonNode buildTree(JsonPointer p, JsonNode value) {
		if (p.matches()) {
			return value;
		} else if (p.getMatchingIndex() >= 0) {
			ArrayNode result = JsonNodeFactory.instance.arrayNode();
			result.set(p.getMatchingIndex(), buildTree(p.tail(), value));
			return result;
		} else {
			ObjectNode result = JsonNodeFactory.instance.objectNode();
			result.set(p.getMatchingProperty(), buildTree(p.tail(), value));
			return result;
		}
	}

	/**
	 * Return all the object types for all containers defined for a given model
	 * version
	 * 
	 * @param version the model version
	 * @return
	 */
	public static Collection<ObjectType> getTypesForVersion(int version) {
		return typesByVersion.containsKey(version) ? typesByVersion.get(version) : Collections.emptySet();
	}

	/**
	 * Filter a collection of object type enums, omitting those that do not apply to
	 * the given model version
	 * 
	 * @param version model version
	 * @param types   object types to filter
	 * @return the filtered list
	 */
	public static Collection<ObjectType> getTypesForVersion(int version, Collection<ObjectType> types) {
		return types.stream().filter(type -> type.paths.containsKey(version)).collect(Collectors.toList());
	}

	/**
	 * Locate the object type, if any, that matches the given $ref URL fragment.
	 * 
	 * @param refFragment fragment of a $ref node URL
	 * @param version     model version
	 * @return matching object type, if any
	 */
	public static Optional<ObjectType> find(String refFragment, int version) {
		if (typesByVersion.containsKey(version)) {
			return typesByVersion.get(version).stream() //
					.filter(type -> type.paths.get(version).matches(refFragment)) //
					.findFirst();
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Utility class to manage a single container path (configured for a specific
	 * model version) for an object type
	 * 
	 * @author Andy Lowry
	 *
	 */
	private static class PathInfo {

		private String pathString;
		private JsonPointer path;
		private Pattern regex;

		public PathInfo(String pathString) {
			this.pathString = pathString;
			this.path = JsonPointer.compile(pathString);
			this.regex = Pattern.compile(pathString + "/([^/]+)");
		}

		public String getPathString() {
			return pathString;
		}

		public JsonPointer getPath() {
			return path;
		}

		public boolean matches(String refFragment) {
			return regex.matcher(refFragment).matches();
		}

		public String getObjectName(String refFragment) {
			Matcher m = regex.matcher(refFragment);
			return m.matches() ? m.group(1) : null;
		}
	}
}
