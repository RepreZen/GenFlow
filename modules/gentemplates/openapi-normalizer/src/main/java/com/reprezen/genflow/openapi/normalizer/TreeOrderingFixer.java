package com.reprezen.genflow.openapi.normalizer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.normal.openapi.ObjectType;
import com.reprezen.genflow.api.normal.openapi.OpenApiMarkers;
import com.reprezen.genflow.api.normal.openapi.Option;
import com.reprezen.genflow.api.normal.openapi.Options;
import com.reprezen.genflow.api.normal.openapi.RepreZenVendorExtension;
import com.reprezen.genflow.api.normal.openapi.Util;

public class TreeOrderingFixer {

	private Integer modelVersion;

	public TreeOrderingFixer(Integer modelVersion) {
		this.modelVersion = modelVersion;
	}

	public JsonNode reorder(JsonNode tree) throws GenerationException {
		reorderTree(tree);
		addPathsIfNeeded(tree);
		OpenApiMarkers.removeMarkers(tree, new Options(modelVersion, Option.ORDERING_AS_DECLARED));
		return tree;
	}

	private void reorderTree(JsonNode tree) {
		for (ObjectType sectionType : ObjectType.getTypesForVersion(modelVersion)) {
			JsonNode section = sectionType.getFromNode(tree, modelVersion);
			if (!section.isMissingNode()) {
				reorderPropertiesInObject((ObjectNode) section);
				if (sectionType == ObjectType.PATH) {
					for (Entry<String, JsonNode> path : Util.iterable(section.fields())) {
						reorderInPath((ObjectNode) path.getValue());
					}
				}
			}
		}
	}

	private void reorderInPath(ObjectNode path) {
		reorderPropertiesInObject(path);
		for (String methodName : Util.swaggerMethodOrder) {
			ObjectNode method = (ObjectNode) path.get(methodName);
			if (method != null) {
				ObjectNode responses = (ObjectNode) method.get("responses");
				if (responses != null) {
					reorderPropertiesInObject(responses);
				}
			}
		}
	}

	private void reorderPropertiesInObject(final ObjectNode obj) {
		List<String> names = Lists.newArrayList(obj.fieldNames());
		Collections.sort(names, new Comparator<String>() {
			@Override
			public int compare(String name1, String name2) {
				int p1 = getPosition(obj.get(name1));
				int p2 = getPosition(obj.get(name2));
				return p1 - p2;
			}
		});
		for (String name : names) {
			obj.set(name, obj.remove(name));
		}
	}

	private int getPosition(Object obj) {
		Integer position = null;
		RepreZenVendorExtension rzve = RepreZenVendorExtension.get(obj);
		if (rzve != null) {
			position = rzve.getPosition();
		}
		return position != null ? position : Integer.MAX_VALUE;
	}

	private void addPathsIfNeeded(JsonNode tree) {
		if (ObjectType.PATH.getFromNode(tree, modelVersion).isMissingNode()) {
			ObjectType.PATH.setInNode(tree, JsonNodeFactory.instance.objectNode(), modelVersion);
		}
	}
}
