package com.reprezen.genflow.rapidml.jsonschema.xtend.xchange

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.rapidml.jsonschema.xtend.builder.JsonSchemaNodeFactory
import com.reprezen.genflow.rapidml.jsonschema.xtend.builder.NamedSchemaNode
import com.reprezen.genflow.rapidml.jsonschema.xtend.builder.RapidLinkNode

class XChangeLinksMapNode extends NamedSchemaNode<Void> {

	new(JsonSchemaNodeFactory director, Void element) {
		super(director, element)
	}

	override writeBody(ObjectNode rapidLink) {
		setObjectAsType(rapidLink)
		rapidLink.put("description",
			"A set of hyperlinks from a domain object representation to related resources.\n" +
				"Each property maps a [link relation](https://www.iana.org/assignments/link-relations/link-relations.xhtml)\n" +
				"(the map key or property name) to a hyperlink object (the value).\n")

		rapidLink.put("readOnly", true)
		rapidLink.put("minProperties", 1)
		val additionalProperties = rapidLink.putObject("additionalProperties")
		factory.definitionsNode.addReferenceToDefinition(additionalProperties, new RapidLinkNode(factory, null));
	}

	override getName() {
		"_RapidLinksMap"
	}

}
