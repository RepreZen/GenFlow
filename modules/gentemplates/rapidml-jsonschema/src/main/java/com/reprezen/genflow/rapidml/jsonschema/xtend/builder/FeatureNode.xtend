package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.Structure

class FeatureNode extends PropertyNode<Feature> {

	new(JsonSchemaNodeFactory director, Feature element) {
		super(director, element)
	}
	
	override getName() {
		element.name
	}
	
	override writeType(ObjectNode node) {
		switch element {
			ReferenceProperty: {
				val type = element.type
				factory.definitionsNode.addReferenceToDefinition(node,
					createReferenceToStructureNode(type, isMultiValued))
			}
			PrimitiveProperty:
				writeConstrainableType(node, element, element.type)
		}
	}
	
    def NamedSchemaNode<?> createReferenceToStructureNode(Structure structure, boolean isMultiValued) {
		val ServiceDataResource defaultResource = factory.getDefaultResource(structure, isMultiValued);
		if (defaultResource != null) {
			return new ResourceLinkNode(factory, defaultResource);
		}
		return factory.createStructureNode(structure);
	}
    
	override isArrayProperty() {
		isMultiValued && !isMultiValuedReferenceWithCollectionResource
	}
	
	def isMultiValuedReferenceWithCollectionResource() {
		isMultiValued //
		&& element instanceof ReferenceProperty //
		&& factory.getDefaultResource((element as ReferenceProperty).type, true) != null
	}

	override getMinOccurs() {
		element.minOccurs
	}

	override getMaxOccurs() {
		element.maxOccurs
	}
	
	override getBaseFeature() {
		return element
	}

}
