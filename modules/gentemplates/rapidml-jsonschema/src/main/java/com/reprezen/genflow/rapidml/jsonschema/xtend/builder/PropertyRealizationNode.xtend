package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.PropertyRealization

class PropertyRealizationNode extends PropertyNode<PropertyRealization> {

	new(JsonSchemaNodeFactory director, PropertyRealization element) {
		super(director, element)
	}

	override writeType(ObjectNode body) {
		val baseProperty = element.baseProperty
		if (baseProperty instanceof PrimitiveProperty) {
			writeConstrainableType(body, element, baseProperty.type)
		}
	}
	
	override getName() {
		element.baseProperty.name
	}

	override getMinOccurs() {
		element.minOccurs
	}

	override getMaxOccurs() {
		element.maxOccurs
	}
	
	override getBaseFeature() {
		return element.baseProperty
	}

}
