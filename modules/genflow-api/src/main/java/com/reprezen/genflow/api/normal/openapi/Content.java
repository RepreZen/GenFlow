/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The content of an assembled model spec, as created by a ContentLoader
 * instance.
 */
public class Content {

	private final Reference ref;
	private final JsonNode tree;
	private final String unloadableReason;
	private final Integer version;

	private Content(Reference reference, JsonNode content, String unloadableReason) {
		this.ref = reference;
		this.tree = content;
		this.unloadableReason = unloadableReason;
		this.version = ref.getModelVersion();
	}

	/**
	 * Wrap loaded content in a Content object
	 */
	public static Content getContentItem(Reference reference, JsonNode content) {
		return new Content(reference, content, null);
	}

	/**
	 * Create a content object for a URL that cannot be loaded.
	 */
	public static Content getUnloadableContentItem(Reference reference, String unloadableReason) {
		return new Content(reference, null, unloadableReason);
	}

	public Reference getReference() {
		return ref;
	}

	public JsonNode getTree() {
		return tree;
	}

	public boolean isValid() {
		return unloadableReason == null;
	}

	public String getCanonicalRefString() {
		return ref.getCanonicalFileRefString();
	}

	public String getRefString() {
		return ref.getRefString();
	}

	public String getObjectName() {
		return ref.getObjectName();
	}

	public String getUnresolvedReason() {
		return unloadableReason;
	}

	public Optional<JsonNode> getObject(ObjectType section, String name) {
		if (isValid()) {
			JsonNode sectionNode = section.getFromNode(tree, version);
			return sectionNode.has(name) ? Optional.of(sectionNode.get(name)) : Optional.<JsonNode>empty();
		}
		return Optional.empty();
	}

	public Reference getObjectReference(ObjectType sectionObjectType, String objectName) {
		return new Reference(sectionObjectType, objectName, ref, version);
	}

	public Iterable<String> getObjectNames(ObjectType section) {
		if (isValid()) {
			JsonNode sectionNode = section.getFromNode(tree, version);
			if (sectionNode instanceof ObjectNode) {
				return Util.iterable(((ObjectNode) sectionNode).fieldNames());
			}
		}
		return Collections.emptyList();
	}
}
