package com.reprezen.genflow.api.normal.openapi;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Create a title for any named schema that lacks one, copying the name
 */
public class DefinitionProcessor {

	private final ContentManager contentManager;
	private final Integer modelVersion;

	public DefinitionProcessor(ContentManager contentManager) {
		this.contentManager = contentManager;
		this.modelVersion = contentManager.getModelVersion();
	}

	public void setTypeNames(boolean setMissingTitles) {
		for (Content contentItem : contentManager.contentItems()) {
			setTypeNames(contentItem, setMissingTitles);
		}
	}

	private void setTypeNames(Content contentItem, boolean setMissingTitles) {
		ObjectType section = modelVersion == ObjectType.SWAGGER_MODEL_VERSION ? ObjectType.DEFINITION
				: modelVersion == ObjectType.OPENAPI3_MODEL_VERSION ? ObjectType.SCHEMA : null;
		if (section != null) {
			for (String name : contentItem.getObjectNames(section)) {
				ObjectNode schema = (ObjectNode) contentItem.getObject(section, name).get();
				OpenApiMarkers.markTypeName(schema, name);
				if (setMissingTitles && !schema.has("title")) {
					schema.put("title", name);
				}
			}
		}
	}
}
