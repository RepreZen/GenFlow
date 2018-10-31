package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.api.GenerationException
import com.reprezen.rapidml.PrimitiveType
import java.util.Map

class PrimitiveTypeNode extends JsonSchemaNode<PrimitiveType> {
	val Map<String, Pair<String, String>> types = #{ //
		'anyURI' -> {
			'string' -> null
		}, //
		'duration' -> {
			'string' -> null
		}, //
		'gMonth' -> {
			'string' -> null
		}, //
		'gMonthDay' -> {
			'string' -> null
		}, //
		'gDay' -> {
			'string' -> null
		}, //
		'gYearMonth' -> {
			'string' -> null
		}, //
		'gYear' -> {
			'string' -> null
		}, //
		'QName' -> {
			'string' -> null
		}, //
		'time' -> {
			'string' -> null
		}, //
		'string' -> {
			'string' -> null
		}, //
		'NCName' -> {
			'string' -> null
		}, //
		'boolean' -> {
			'boolean' -> null
		}, //
		'date' -> {
			'string' -> 'date'
		}, //
		'dateTime' -> {
			'string' -> 'date-time'
		}, //
		'decimal' -> {
			'number' -> null
		}, //
		'double' -> {
			'number' -> 'double'
		}, //
		'float' -> {
			'number' -> 'float'
		}, //
		'integer' -> {
			'integer' -> null
		}, //
		'int' -> {
			'integer' -> null
		}, //
		'long' -> {
			'integer' -> 'int64'
		}, //
		'base64Binary' -> {
			'string' -> 'byte'
		} //
	}

	new(JsonSchemaNodeFactory factory, PrimitiveType element) {
		super(factory, element)
	}

	override write(ObjectNode node) {
		writeTypeAndFormat(node)
		writeBase64BinaryEncoding(node)
	}

	def protected writeTypeAndFormat(ObjectNode node) {
		val typeValue = getType()
		if (typeValue == null) {
			throw new GenerationException("Unknown primitive type: " + element.name)
		}
		node.put("type", typeValue)
		val formatValue = getFormat()
		if (formatValue != null) {
			node.put("format", formatValue)
		}
	}

	def protected writeBase64BinaryEncoding(ObjectNode node) {
		if ("base64Binary".equals(element.name)) {
			node.putObject("media").put("binaryEncoding", "base64")
		}
	}

	def protected getType() {
		val type = types.get(element.name)
		if (type != null) {
			return type.key
		}
		return null;
	}

	def protected getFormat() {
		val type = types.get(element.name)
		if (type != null) {
			return type.value
		}
		return null;
	}

}
