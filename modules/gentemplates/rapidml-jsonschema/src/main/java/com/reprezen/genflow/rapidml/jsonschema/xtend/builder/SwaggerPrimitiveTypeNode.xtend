package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.rapidml.PrimitiveType

class SwaggerPrimitiveTypeNode extends PrimitiveTypeNode {

	new(JsonSchemaNodeFactory factory, PrimitiveType element) {
		super(factory, element)
	}

	override write(ObjectNode node) {
		super.writeTypeAndFormat(node)
		// do NOT call writeBase64BinaryEncoding because it's not supported by Swagger
	}

}
