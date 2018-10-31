package com.reprezen.genflow.rapidml.jsonschema.xtend

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.rapidml.jsonschema.xtend.builder.JsonSchemaNodeFactory
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.Structure

class JsonSchemaForRamlGenerator {

	final JsonSchemaNodeFactory jsonSchemaNodeFactory

	new() {
		jsonSchemaNodeFactory = new JsonSchemaNodeFactory(JsonSchemaFormat.RAML)
	}

	def public ObjectNode buildDefinitionNode(ObjectNode node, ServiceDataResource resource) {
		jsonSchemaNodeFactory.createResourceNode(resource).writeBody(node)
		return node
	}

	def public ObjectNode buildDefinitionNode(ObjectNode node, Structure structure) {
		jsonSchemaNodeFactory.createStructureNode(structure).writeBody(node)
		return node
	}
}
