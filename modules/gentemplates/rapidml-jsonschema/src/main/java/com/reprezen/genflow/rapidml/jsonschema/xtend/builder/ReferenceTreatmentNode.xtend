package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.rapidml.jsonschema.xtend.JsonSchemaHelper
import com.reprezen.rapidml.ReferenceElement
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.ReferenceTreatment

abstract class ReferenceTreatmentNode<T extends ReferenceTreatment> extends PropertyNode<T> {
	
	extension JsonSchemaHelper = new JsonSchemaHelper()
	
	new(JsonSchemaNodeFactory director, T element) {
		super(director, element)
	}
	
	def inlineOrAddTopLevelNode(ObjectNode parentNode) {
		return write(parentNode)
	}
	
	override getName() {
		getReferencedProperty(element).name
	}

	override String getPropertyDocumentation() {
		if (element.referenceElement instanceof ReferenceProperty) {
			return getDocumentation(element.referenceElement as ReferenceProperty)
		}
		return ""
	}

	override int getMinOccurs() {
		return getReferencedProperty().minOccurs
	}

	override int getMaxOccurs() {
		return getReferencedProperty().maxOccurs
	}

	def ReferenceElement getReferencedProperty() {
		element.referenceElement
	}
	
	override getBaseFeature() {
		if (element.referenceElement instanceof ReferenceProperty) {
			return element.referenceElement as ReferenceProperty
		}
		null
	}
	
}
