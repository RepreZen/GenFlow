package com.reprezen.genflow.common.jsonschema.builder.xchange

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.base.Strings
import com.reprezen.genflow.common.jsonschema.builder.JsonSchemaNodeFactory
import com.reprezen.genflow.common.jsonschema.builder.NamedSchemaNode
import com.reprezen.genflow.common.jsonschema.builder.PropertyRealizationNode
import com.reprezen.genflow.common.jsonschema.builder.TypedMessageNode
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.RealizationContainer
import com.reprezen.rapidml.ReferenceRealization
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedMessage

class DefaultRealizationContainerNode extends NamedSchemaNode<RealizationContainer> {

	new(JsonSchemaNodeFactory director, RealizationContainer element) {
		super(director, element)
	}

	override getName() {
		if (element.getRealizationName() !== null) {
			// read from Ecore model
			return element.getRealizationName();
		}
		// calculate based on the element and element parent name
		return composeRealizationName(element)
	}

	override void writeBody(ObjectNode node) {
		node.setObjectAsType()
		val doc = if (element.realizationName === null) {
				// backward compatibility, the data type name is used, so use its documentation as well
				getDocumentation(element.dataType)
			} else {
				if (!Strings.isNullOrEmpty(getDocumentation(element))) {
					getDocumentation(element)
				} else {
					getDocumentation(element.dataType)
				}
			}
		node.putDescription(doc)
		node.addVendorExtensions(getRapidExtensions(element.dataType))
		node.addVendorExtensions(getRapidExtensions(element))
		val ObjectNode propertiesNode = node.putObject("properties")
		val includedProperties = element.properties.allIncludedProperties.filter [ e |
			!hasReferenceTreatment(element, e.baseProperty)
		]
		for (PropertyRealization includedProperty : includedProperties) {
			new PropertyRealizationNode(factory, includedProperty).write(propertiesNode)
		}
		for (ReferenceTreatment referenceTreatment : element.referenceTreatments) {
			factory.createReferenceTreatmentNode(referenceTreatment).inlineOrAddTopLevelNode(propertiesNode)
		}
		node.writeRequiredProperties(getRequiredPropertyNames(element))
	}

	def private dispatch String composeRealizationName(ServiceDataResource resource) {
		resource.actualType.name
	}
	
	def private dispatch String composeRealizationName(ResourceDefinition resource) {
		resource.name
	}

	def private dispatch String composeRealizationName(ReferenceTreatment reference) {
		reference.referenceElement.name
	}

	def private dispatch String composeRealizationName(ReferenceRealization reference) {
		reference.actualType.name
	}

	def private dispatch String composeRealizationName(TypedMessage message) {
		TypedMessageNode::getMessageDefinitionName(message)
	}
}
