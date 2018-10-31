package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.NamedLinkDescriptor
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource

class ResourceLinkNode extends NamedSchemaNode<ResourceDefinition> {

	new(JsonSchemaNodeFactory director, ResourceDefinition element) {
		super(director, element)
	}

	override getName() {
		val link = element.name + "_link";
		if (element instanceof ServiceDataResource &&
			((element as ServiceDataResource).defaultLinkDescriptor != null)) {
			return ((element as ServiceDataResource).defaultLinkDescriptor as NamedLinkDescriptor).name;
		}
		return link;
	}

	override writeBody(ObjectNode node) {
		setObjectAsType(node)
		node.addVendorExtensions(getRapidExtensions(element))
		val propertiesNode = node.putObject("properties")
		
		val rapidLink = propertiesNode.putObject(factory.rapidLinkPropertyName)
		val rapidLinkNode = factory.createRapidLinkNode()
		factory.definitionsNode.addReferenceToDefinition(rapidLink, rapidLinkNode)
		
		if (element instanceof ServiceDataResource && (element as ServiceDataResource).defaultLinkDescriptor != null) {
			for (Feature prop : (element as ServiceDataResource).defaultLinkDescriptor.allIncludedProperties.map[baseProperty]) {
				factory.createFeatureNode(prop).write(propertiesNode)
			}
		}
	}

}
