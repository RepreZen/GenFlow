package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.rapidml.ObjectResource
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.ServiceDataResource

class ObjectResourceNode extends NamedSchemaNode<ServiceDataResource> {

	NamedSchemaNode<?> delegate

	new(JsonSchemaNodeFactory  director, ServiceDataResource element) {
		super(director, element)
		if (useDefaultRealization) {
			delegate = factory.createDefaultRealizationNode(element)
		}
	}
	
	protected def useDefaultRealization() {
		return (element instanceof ObjectResource) && element.withDefaultRealization && (element.dataType != null)
	}

	override getName() {
		if (delegate != null) {
			return delegate.name
		}
		return element.name
	}

	override void writeBody(ObjectNode node) {
		if (delegate != null) {
			delegate.writeBody(node)
			return
		}
 		node.setObjectAsType()
		node.putDescription(getDocumentation(element))
		node.addVendorExtensions(getRapidExtensions(element.dataType))
		node.addVendorExtensions(getRapidExtensions(element))
		val ObjectNode propertiesNode = node.putObject("properties")
		var includedProperties = element.properties.allIncludedProperties.filter [ e |
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

}
