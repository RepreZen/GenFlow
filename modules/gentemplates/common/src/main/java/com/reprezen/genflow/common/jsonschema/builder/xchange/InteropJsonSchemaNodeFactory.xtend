package com.reprezen.genflow.common.jsonschema.builder.xchange

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.common.jsonschema.builder.ObjectResourceNode
import com.reprezen.genflow.common.jsonschema.builder.StructureNode
import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.Structure

class InteropJsonSchemaNodeFactory extends XChangeJsonSchemaNodeFactory {

	new() {
		super()
	}

	override createStructureNode(Structure element) {
		return new StructureNode(this, element) {

			override writeBody(ObjectNode body) {
				writeBasicObject(body)

				val propertiesNode = writeProperties(body)
				val links = propertiesNode.putObject(rapidLinkPropertyName);
				factory.definitionsNode.addReferenceToDefinition(links, createRapidLinkNode())
			}

		}
	}

	override <T extends ServiceDataResource> createObjectResourceNode(T element) {
		return new ObjectResourceNode(this, element) {

			override protected useDefaultRealization() {
				return !(element instanceof CollectionResource);
			}

		};
	}

}
