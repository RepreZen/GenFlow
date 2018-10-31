package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode

class RapidLinkNode extends NamedSchemaNode<Void> {

	new(JsonSchemaNodeFactory director, Void element) {
		super(director, element)
	}

	override writeBody(ObjectNode rapidLink) {
		rapidLink.put("description",
		"An object representing a hyperlink to a related resource.\n" +
		"The link relation is specified as the containing property name.\n" +
		"The `href` property specifies the target URL, and additional properties may specify other metadata.\n")
		rapidLink.put("type", "object")
		rapidLink.put("minProperties", 1)
		rapidLink.putObject("properties")
			.putObject("href")
				.put("type", "string")
	}
	
	override getName() {
		"_RapidLink"
	}

}
