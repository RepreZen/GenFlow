package com.reprezen.genflow.api.normal.openapi;

import java.net.URL;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Load a model spec and any other JSON documents required to assemble the
 * overall spec and satisfy object references.
 * <p>
 * Assembly means creating a complete spec from parts, by inlining those parts
 * to replace non-object references.
 * <p>
 * Assembly does not inline model object references (i.e.
 * definition/parameter/path/response or component/* references); those
 * references are handled by OpenApiInliner based on options and other factors.
 * The effect of assembly is to ensure that OpenApiInliner will be presented
 * with complete model specs, including both the top-level spec and all
 * component specs in a multi-file scenario.
 * <p>
 * An external model object reference encountered in loaded content is not
 * inlined (per above remarks), but it does trigger request to the content
 * manager to load and assemble the containing spec.
 */
public class ContentLoader {

	private final ContentManager contentManager;
	private final DocLoader docLoader;
	private Reference base;

	public ContentLoader(ContentManager contentManager) {
		this.contentManager = contentManager;
		this.docLoader = new DocLoader(contentManager.getModelVersion());
	}

	public Content load(URL url, JsonNode tree, boolean rewriteSimpleRefs) {
		this.base = new Reference(url, contentManager.getModelVersion());
		if (tree == null) {
			Content content = docLoader.load(url);
			if (content.isValid()) {
				tree = content.getTree();
			} else {
				return content;
			}
		}
		if (rewriteSimpleRefs) {
			SimpleRefFixer.fixSimpleRefs(tree);
		}
		return assemble(tree, base);
	}

	private Content assemble(JsonNode tree, Reference base) {
		Set<Reference> visited = new HashSet<Reference>();
		JsonNode assembly = assembleNode(tree, base, visited);
		return Content.getContentItem(base, assembly);
	}

	/**
	 * Recursive method to search out and handle all references in this loaded spec
	 */
	private JsonNode assembleNode(JsonNode node, Reference base, Set<Reference> visited) {
		if (Util.isRef(node)) {
			Reference ref = Reference.fromNode(node, base, contentManager.getModelVersion());
			return assembleRef(ref, base, visited);
		} else {
			assembleObjectProperties(node, base, visited);
			assembleArrayElements(node, base, visited);
			return node;
		}
	}

	/**
	 * Handle an embedded reference node
	 */
	private JsonNode assembleRef(Reference ref, Reference base, Set<Reference> visited) {
		String invalidReason = null;
		JsonNode tree = null;
		if (ref.isModelObjectRef()) {
			// object ref encountered - make sure we assemble the containing spec
			contentManager.requestDocument(ref.getUrl());
			tree = ref.getRefNode();
		} else if (visited.contains(ref)) {
			invalidReason = "Cannot inline non-conforming reference because it is recursive";
		} else {
			// non-object ref - load the containing document and inline the referenced
			// content
			visited.add(ref);
			Content content = docLoader.load(ref.getUrl());
			if (content.isValid()) {
				try {
					tree = ref.getFragment() != null ? content.getTree().at(ref.getFragment()) : content.getTree();
					if (tree.isMissingNode()) {
						invalidReason = "Ref fragment does not address a value in the retrived document";
					} else {
						tree = tree.deepCopy();
						tree = assembleNode(tree, ref, visited);
					}
				} catch (IllegalArgumentException e) {
					invalidReason = "Invalid reference fragment: " + e.getMessage();
				}
			} else {
				invalidReason = content.getUnresolvedReason();
			}
		}
		if (invalidReason != null) {
			tree = ref.getBadRefNode();
		}
		return tree;

	}

	/**
	 * If this is an object node, handle any refs among its property values
	 */
	private void assembleObjectProperties(JsonNode node, Reference base, Set<Reference> visited) {
		if (node instanceof ObjectNode) {
			ObjectNode objectNode = (ObjectNode) node;
			for (Entry<String, JsonNode> field : Util.iterable(objectNode.fields())) {
				field.setValue(assembleNode(field.getValue(), base, visited));
			}
		}
	}

	/**
	 * If this is an array node, handle any refs among its element values
	 */
	private void assembleArrayElements(JsonNode node, Reference base, Set<Reference> visited) {
		if (node instanceof ArrayNode) {
			ArrayNode arrayNode = (ArrayNode) node;
			for (int i = 0; i < node.size(); i++) {
				arrayNode.set(i, assembleNode(arrayNode.get(i), base, visited));
			}
		}
	}
}
