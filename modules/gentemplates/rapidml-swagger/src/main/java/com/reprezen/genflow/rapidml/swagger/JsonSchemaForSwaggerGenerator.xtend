package com.reprezen.genflow.rapidml.swagger

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.common.jsonschema.JsonSchemaFormat
import com.reprezen.genflow.common.jsonschema.Options
import com.reprezen.genflow.common.jsonschema.builder.JsonSchemaNodeFactory
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.ZenModel
import java.util.Map

class JsonSchemaForSwaggerGenerator {
	val JsonSchemaNodeFactory jsonSchemaNodeFactory

	new() {
		this(new JsonSchemaNodeFactory(JsonSchemaFormat.SWAGGER))
	}

	new(JsonSchemaNodeFactory jsonSchemaNodeFactory) {
		this.jsonSchemaNodeFactory = jsonSchemaNodeFactory
	}

	def init(Options options) {
		jsonSchemaNodeFactory.options = options
	}

	/*
	 * Convenience method for Swagger generator
	 */
	def ObjectNode generateDefinitionsNode(ZenModel model, Map<String, Object> templateParams) {
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
