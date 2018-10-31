package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.rapidml.Enumeration
import com.reprezen.rapidml.PrimitiveType
import com.reprezen.rapidml.UserDefinedType

abstract class TypedNode<T> extends NamedSchemaNode<T> {

	new(JsonSchemaNodeFactory director, T element) {
		super(director, element)
	}

	protected def dispatch ObjectNode writePropertyType(ObjectNode node, UserDefinedType type) {
		factory.createUserDefinedTypeNode(type).write(node)
	}

	protected def dispatch ObjectNode writePropertyType(ObjectNode node, PrimitiveType type) {
		factory.createPrimitiveTypeNode(type).write(node)
	}

	protected def dispatch ObjectNode writePropertyType(ObjectNode node, Enumeration type) {
		factory.definitionsNode.addReferenceToDefinition(node, factory.createEnumerationNode(type))
		return node
	}

}
