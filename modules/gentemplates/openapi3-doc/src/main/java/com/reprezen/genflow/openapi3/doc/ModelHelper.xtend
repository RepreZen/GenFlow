package com.reprezen.genflow.openapi3.doc

import com.reprezen.kaizen.oasparser.model3.Schema

class ModelHelper implements Helper {

	override init() {}

	/*
	 * See comments at top of PropertyHelper - everything here is analogous
	 */
	def Object getAttribute(Schema model, String attr) {
		if (model.properties.containsKey(attr))
			model.properties.get(attr)
		else
			switch attr {
				case "additionalProperties": model.getAdditionalProperties()
				case "defaultValue": model.^default
				case "description": model.description
				case "discriminator": model.discriminator
				case "enums": model.enums
				case "example": model.example
				case "format": model.format
				case "properties": model.properties
				case "type": model.type
				case "xml": model.xml
			}
	}
}
