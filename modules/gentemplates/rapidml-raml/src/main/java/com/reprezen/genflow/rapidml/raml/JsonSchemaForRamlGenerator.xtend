package com.reprezen.genflow.rapidml.raml

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.common.jsonschema.JsonSchemaFormat
import com.reprezen.genflow.common.jsonschema.builder.JsonSchemaNodeFactory
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.Structure

class JsonSchemaForRamlGenerator {

	final JsonSchemaNodeFactory jsonSchemaNodeFactory

	new() {
		jsonSchemaNodeFactory = new JsonSchemaNodeFactory(JsonSchemaFormat.RAML)
	}

	def ObjectNode buildDefinitionNode(ObjectNode node, ServiceDataResource resource) {
		jsonSchemaNodeFactory.createResourceNode(resource).writeBody(node)
		return node
	}

	def ObjectNode buildDefinitionNode(ObjectNode node, Structure structure) {
		jsonSchemaNodeFactory.createStructureNode(structure).writeBody(node)
		return node
	}
}
