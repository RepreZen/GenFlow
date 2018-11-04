package com.reprezen.genflow.common.jsonschema.builder.xchange

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.Lists
import com.reprezen.genflow.common.jsonschema.builder.JsonSchemaNodeFactory
import com.reprezen.genflow.common.jsonschema.builder.NamedSchemaNode
import com.reprezen.genflow.common.jsonschema.builder.PropertyRealizationNode
import com.reprezen.rapidml.NamedLinkDescriptor
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.ServiceDataResource
import java.util.List

class DefaultLinkNode extends NamedSchemaNode<ReferenceLink> {

	new(JsonSchemaNodeFactory director, ReferenceLink element) {
		super(director, element)
	}

	override getName() {
		if (element.referenceRealization.getRealizationName() !== null) {
			// read from Ecore model
			return element.referenceRealization.getRealizationName()
		}
		val link = element.targetResource.name + "_link";
		if (element.targetResource instanceof ServiceDataResource &&
			((element.targetResource as ServiceDataResource).defaultLinkDescriptor != null)) {
			return ((element.targetResource as ServiceDataResource).defaultLinkDescriptor as NamedLinkDescriptor).name;
		}
		return link;
	}

	override void writeBody(ObjectNode node) {
		node.setObjectAsType()
		val realization = element.referenceRealization
		if (element.referenceElement instanceof ReferenceProperty) {
			node.addVendorExtensions(getRapidExtensions(element.referenceElement as ReferenceProperty))
		}
		node.addVendorExtensions(getRapidExtensions(element))
		val ObjectNode propertiesNode = node.putObject("properties")
		val includedProperties = realization.properties.allIncludedProperties.filter [ e |
			!hasReferenceTreatment(realization, e.baseProperty)
		]
		for (PropertyRealization includedProperty : includedProperties) {
			new PropertyRealizationNode(factory, includedProperty).write(propertiesNode)
		}
		for (ReferenceTreatment referenceTreatment : realization.referenceTreatments) {
			factory.createReferenceTreatmentNode(referenceTreatment).inlineOrAddTopLevelNode(propertiesNode)
		}
		
		val List<String> requiredProperties = Lists.newArrayList(getRequiredPropertyNames(realization))

		val rapidLink = factory.addRapidLink(propertiesNode, requiredProperties, element);
		
		node.writeRequiredProperties(requiredProperties)
	}

}
