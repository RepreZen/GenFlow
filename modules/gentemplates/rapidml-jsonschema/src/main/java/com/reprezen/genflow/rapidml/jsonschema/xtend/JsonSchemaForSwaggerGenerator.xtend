package com.reprezen.genflow.rapidml.jsonschema.xtend

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.rapidml.jsonschema.xtend.builder.JsonSchemaNodeFactory
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.ZenModel
import java.util.Map

class JsonSchemaForSwaggerGenerator {
	final private JsonSchemaNodeFactory jsonSchemaNodeFactory

	new() {
		this(new JsonSchemaNodeFactory(JsonSchemaFormat.SWAGGER))
	}
	
	new(JsonSchemaNodeFactory jsonSchemaNodeFactory) {
		this.jsonSchemaNodeFactory = jsonSchemaNodeFactory
	}
	
	def public init(Options options) {
		jsonSchemaNodeFactory.options = options
	}

	/*
	 * Convenience method for Swagger generator
	 */
	def public ObjectNode generateDefinitionsNode(ZenModel model, Map<String, Object> templateParams) {
		init(Options.fromParams(templateParams));
		jsonSchemaNodeFactory.generateDefinitionsNode(model)
	}

	def String getDefinitionName(ServiceDataResource resource) {
		jsonSchemaNodeFactory.createResourceNode(resource).name
	}

	def String getDefinitionName(TypedMessage message) {
		jsonSchemaNodeFactory.createTypedMessageNode(message).name
	}
}
