package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.base.Strings
import com.reprezen.rapidml.CollectionRealizationEnum
import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ServiceDataResource

class CollectionResourceNode extends NamedSchemaNode<ServiceDataResource> {

	new(JsonSchemaNodeFactory director, ServiceDataResource element) {
		super(director, element)
	}

	override getName() {
		getCollectionResourceName(element)
	}
	
	def private static getCollectionResourceName(ServiceDataResource resource) {
		if (resource.withDefaultRealization && !Strings.isNullOrEmpty(resource.realizationName)) {
			return resource.realizationName
		}
		resource.name
	}

	override writeBody(ObjectNode node) {
		val items = node.put("type", "array").putObject("items")
		val arrayItemNode = new CollectionResourceArrayItemNode(factory, element)
		if (factory.schemaFormat.inlineSimpleTypes) {
			arrayItemNode.writeBody(items)
		} else {
			if (CollectionRealizationEnum.REFERENCE_LINK_LIST ==
				(element as CollectionResource).getResourceRealizationKind()) {
				// Process the ObjectResourceLink, 
				// cannot use the standard ObjectResourceNode#writeBody() because it wraps in it the "properties" node
				for (ReferenceLink hyperlink : element.referenceLinks) {
					factory.definitionsNode.addReferenceToDefinition(items,
						new ResourceLinkNode(factory, hyperlink.targetResource))
				}
			} else {
				factory.definitionsNode.addReferenceToDefinition(items, arrayItemNode)
			}
		}
	}

	static class CollectionResourceArrayItemNode extends ObjectResourceNode {

		new(JsonSchemaNodeFactory director, ServiceDataResource element) {
			super(director, element)
		}

		override writeBody(ObjectNode node) {
			if ((element as CollectionResource).resourceRealizationKind ==
				CollectionRealizationEnum::EMBEDDED_OBJECT_LIST) {
				super.writeBody(node)
			} else {
				// REFERENCE_LINK_LIST processed by root collection resource node
			}
		}

		override getName() {
			if (element.withDefaultRealization && Strings.isNullOrEmpty(element.realizationName)) {
				return factory.createStructureNode(element.dataType).name
			}
			return getCollectionResourceName(element) + "_item"
		}

	}

	static class ObjectResourceLinkNode extends ReferenceLinkNode {
		new(JsonSchemaNodeFactory director, ReferenceLink element) {
			super(director, element)
		}

		override isArrayProperty() {
			// the multiplicity is treated by the "array" in CollectionResourceNode 
			false
		}

	}

}
