package com.reprezen.genflow.common.jsonschema.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.Lists
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.RealizationContainer
import com.reprezen.rapidml.ReferenceEmbed
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.util.TagUtils
import java.util.List

class ReferenceEmbedNode extends ReferenceTreatmentNode<ReferenceEmbed> {

	final NamedSchemaNode<?> defaultRealizationNode;

	new(JsonSchemaNodeFactory director, ReferenceEmbed element) {
		super(director, element)
		if (useDefaultRealization) {
			defaultRealizationNode = factory.createDefaultRealizationNode(element.referenceRealization)
		} else {
			defaultRealizationNode = null
		}
	}

	override inlineOrAddTopLevelNode(ObjectNode parentNode) {
		if (factory.options.inlineReferenceEmbeds || defaultRealizationNode !== null) {
			return write(parentNode)
		} else {
			val referenceTreatmentPropertyNode = parentNode.putObject(element.referenceElement.name)
			factory.definitionsNode.addReferenceToDefinition(referenceTreatmentPropertyNode, this)
			return referenceTreatmentPropertyNode
		}
	}

	override void writeType(ObjectNode node) {
		if (defaultRealizationNode !== null) {
			factory.definitionsNode.addReferenceToDefinition(node, defaultRealizationNode)
		} else {
			val isArray = isArrayProperty
			val nestedReferenceEmbedNode = new ReferenceEmbedItemNode(factory, element, isArray)
			if (isArray && !factory.options.inlineArrayItems) {
				factory.definitionsNode.addReferenceToDefinition(node, nestedReferenceEmbedNode)
			} else {
				nestedReferenceEmbedNode.writeBody(node)
			}
		}
	}

	def protected useDefaultRealization() {
		TagUtils.getTagWithName(element, TagUtils.WITH_DEFAULT_OBJECT_REALIZATION).isPresent
	}

	override getName() {
		if (defaultRealizationNode !== null) {
			return element.referenceElement.name
		}
		val nameSegments = newArrayList()
		nameSegments.add(containerName)
		nameSegments.addAll(element.embedHierarchy.map[it.referenceElement.name])
		nameSegments.add(element.referenceElement.name)
		return nameSegments.join('_')
	}

	def private String getContainerName() {
		val embedHierarchy = element.embedHierarchy
		val topLevelEmbed = if (!embedHierarchy.empty) {
				embedHierarchy.findFirst[true]
			} else {
				element
			}
		// TODO potential class cast exception here when we switch to realization graph
		val parent = topLevelEmbed.eContainer as RealizationContainer
		val containerName = switch (parent) {
			ResourceDefinition: parent.name
			TypedMessage: TypedMessageNode::getMessageDefinitionName(parent)
		}
		return containerName;

	}

	private static class ReferenceEmbedItemNode extends ReferenceEmbedNode {
		val boolean isArray

		new(JsonSchemaNodeFactory director, ReferenceEmbed element, boolean isArray) {
			super(director, element)
			this.isArray = isArray;
		}

		override writeBody(ObjectNode node) {
			node.put("type", "object")
			setObjectAsType(node)
			node.addVendorExtensions(getRapidExtensions(element))
			val propertiesNode = node.putObject("properties")

			val List<String> requiredProperties = Lists.newArrayList()
			requiredProperties.addAll(getRequiredPropertyNames(element.referenceRealization))
			node.writeRequiredProperties(requiredProperties)

			for (Feature prop : getIncludedPrimitiveProperties(element)) {
				factory.createFeatureNode(prop).write(propertiesNode)
			}
			for (referenceTreatment : element.nestedReferenceTreatments) {
				val nestedReferenceTreatmentNode = factory.createReferenceTreatmentNode(referenceTreatment)
				nestedReferenceTreatmentNode.inlineOrAddTopLevelNode(propertiesNode)
			// factory.definitionsNode.addReferenceToDefinition(propertiesNode, nestedReferenceTreatmentNode)
			}
		}

		override getName() {
			if(isArray) super.name + '_item' else super.name
		}

	}
}
