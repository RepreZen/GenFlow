package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.Structure

class StructureNode extends NamedSchemaNode<Structure> {

	new(JsonSchemaNodeFactory director, Structure element) {
		super(director, element)
	}

	override writeBody(ObjectNode body) {
		writeBasicObject(body)
		writeProperties(body)
		writeRequiredProperties(body)
	}

	def protected writeBasicObject(ObjectNode body) {
		setObjectAsType(body)
		body.putDescription(getDocumentation(element))
		body.addVendorExtensions(getRapidExtensions(element))
	}

	def protected writeProperties(ObjectNode body) {
		val propertiesNode = body.putObject("properties")
		for (Feature includedProperty : element.ownedFeatures) {
			factory.createFeatureNode(includedProperty).write(propertiesNode)
		}
		return propertiesNode
	}

	def protected writeRequiredProperties(ObjectNode body) {
		body.writeRequiredProperties(getRequiredPropertyNames(element))
	}

	override getName() {
		element.getName()
	}

}
