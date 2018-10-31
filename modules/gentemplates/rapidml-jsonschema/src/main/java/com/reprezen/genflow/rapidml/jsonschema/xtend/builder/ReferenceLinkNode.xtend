package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.Lists
import com.reprezen.rapidml.CollectionRealizationLevelEnum
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.util.TagUtils
import java.util.List

class ReferenceLinkNode extends ReferenceTreatmentNode<ReferenceLink> {
	new(JsonSchemaNodeFactory director, ReferenceLink element) {
		super(director, element)
	}
	
	override void writeType(ObjectNode node) {
		if (TagUtils.getTagWithName(element, TagUtils.WITH_DEFAULT_OBJECT_REALIZATION).isPresent) {
			val resourceLinkNode = factory.createDefaultLinkNode(element)
			factory.definitionsNode.addReferenceToDefinition(node, resourceLinkNode)
		} else {
			setObjectAsType(node)
			node.addVendorExtensions(getRapidExtensions(element))
			val propertiesNode = node.putObject("properties")
			val List<String> requiredProperties = Lists.newArrayList()
			requiredProperties.addAll(getRequiredPropertyNames(element.referenceRealization))
			
			val rapidLink = factory.addRapidLink(propertiesNode, requiredProperties, element);
			
			node.writeRequiredProperties(requiredProperties)
			for (Feature prop : getIncludedPrimitiveProperties(element)) {
				factory.createFeatureNode(prop).write(propertiesNode)
			}
		}
	}
	
	override isArrayProperty() {
		isMultiValued && (element.collectionRealizationLevel == CollectionRealizationLevelEnum.ITEM_LEVEL //
		// ZEN-3978 - don't generate a link to a collection resource with item-level properties, only generate item-level properties
		|| isLinkToCollectionWithItemBasedProperties(element))
	}

	def static isLinkToCollectionWithItemBasedProperties(ReferenceLink element) {
		element.collectionRealizationLevel == CollectionRealizationLevelEnum.COLLECTION_LEVEL &&
			!element.getReferenceRealization().getProperties().getAllIncludedProperties().isEmpty()
	}

}
