package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode

abstract class JsonSchemaNode<T> {

	final protected T element
	final protected JsonSchemaNodeFactory factory

	new(JsonSchemaNodeFactory factory, T element) {
		this.element = element
		this.factory = factory
	}

	def abstract ObjectNode write(ObjectNode parentNode)

}
