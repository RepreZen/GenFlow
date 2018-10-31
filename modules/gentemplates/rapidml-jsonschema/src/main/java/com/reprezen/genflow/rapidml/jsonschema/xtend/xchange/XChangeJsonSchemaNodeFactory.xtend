package com.reprezen.genflow.rapidml.jsonschema.xtend.xchange

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.rapidml.jsonschema.xtend.JsonSchemaFormat
import com.reprezen.genflow.rapidml.jsonschema.xtend.builder.JsonSchemaNodeFactory
import com.reprezen.genflow.rapidml.jsonschema.xtend.builder.SwaggerPrimitiveTypeNode
import com.reprezen.rapidml.PrimitiveType

class XChangeJsonSchemaNodeFactory extends JsonSchemaNodeFactory {

	new() {
		super(JsonSchemaFormat.SWAGGER)
	}

	override createPrimitiveTypeNode(PrimitiveType element) {

		return new SwaggerPrimitiveTypeNode(this, element) {

			override write(ObjectNode node) {
				super.write(node)
				if (!options.allowEmptyString) {
					if ("string".equals(getType())) {
						node.put("minLength", 1)
					}
				}
			}
		}
	}

}
