package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.ZenModel
import java.util.Set

class DefinitionsNode extends JsonSchemaNode<ZenModel> {

	private final ObjectNode definitionsNode
	final Set<String> generatedDefinitionNames = newHashSet()
	
	new(JsonSchemaNodeFactory jsonSchemaNodeFactory, ZenModel element) {
		this(jsonSchemaNodeFactory, element, new ObjectMapper().createObjectNode())
	}

	new(JsonSchemaNodeFactory jsonSchemaNodeFactory, ZenModel element, ObjectNode definitionsNode) {
		super(jsonSchemaNodeFactory, element)
		this.definitionsNode = definitionsNode
	}

	override write(ObjectNode parentNode) { // this parameter is ignored, we use the final definitionsNode
		val resources = element.resourceAPIs.map[ownedResourceDefinitions].flatten.filter(ServiceDataResource)
		for (ServiceDataResource resource : resources) {
			writeTopLevelNode(factory.createResourceNode(resource))
		}
		val messages = element.eAllContents.filter(TypedMessage).filter[actualType != null].toList
		for (TypedMessage message : messages) {
			writeTopLevelNode(factory.createTypedMessageNode(message))
		}
		return definitionsNode
	}

	def public addReferenceToDefinition(ObjectNode node, NamedSchemaNode<?> builder) {
		node.put("$ref", "#/definitions/" + builder.name);
		writeTopLevelNode(builder)
	}

	def private void writeTopLevelNode(NamedSchemaNode<?> builder) {
		// Cannot use RealizationCycleDetector as the same element can be processed by several Nodes, e.g. ReferenceEmbedNode and ReferenceEmbedItemNode
		val isRecursive = generatedDefinitionNames.contains(builder.name)
		if (!isRecursive) {
			generatedDefinitionNames.add(builder.name)
			builder.write(definitionsNode)
		}
	}

}
