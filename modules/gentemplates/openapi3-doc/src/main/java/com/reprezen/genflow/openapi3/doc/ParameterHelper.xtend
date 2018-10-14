package com.reprezen.genflow.openapi3.doc

import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.model3.Parameter

class ParameterHelper implements Helper {

	override init() {}

	/*
	 * See comments at top of PropertyHelper - everything here is analogous
	 */
	def Object getAttribute(Parameter param, String attr) {
		switch attr {
			case "description": param.description
			case "in": param.in
			case "name": param.name
			case "required": !Overlay.of(param).isPresent
			case "type": param.schema.type
		}

// FIXME adapt to OAS3
//		switch param {
//			// leaf (height 1) types
//			CookieParameter:
//				param.getAttribute(attr)
//			FormParameter:
//				param.getAttribute(attr)
//			HeaderParameter:
//				param.getAttribute(attr)
//			PathParameter:
//				param.getAttribute(attr)
//			QueryParameter:
//				param.getAttribute(attr)
//			// height 2 types
//			AbstractSerializableParameter<?>:
//				param.getAttribute(attr)
//			RefParameter:
//				param.getAttribute(attr)
//			BodyParameter:
//				param.getAttribute(attr)
//		}
	}

//	def private getAttribute(CookieParameter param, String attr) {
//		return (param as AbstractSerializableParameter<CookieParameter>).getAttribute(attr)
//	}
//
//	def private getAttribute(FormParameter param, String attr) {
//		return (param as AbstractSerializableParameter<FormParameter>).getAttribute(attr)
//	}
//
//	def private getAttribute(HeaderParameter param, String attr) {
//		return (param as AbstractSerializableParameter<HeaderParameter>).getAttribute(attr)
//	}
//
//	def private getAttribute(PathParameter param, String attr) {
//		return (param as AbstractSerializableParameter<PathParameter>).getAttribute(attr)
//	}
//
//	def private getAttribute(QueryParameter param, String attr) {
//		return (param as AbstractSerializableParameter<QueryParameter>).getAttribute(attr)
//	}
//	def private getAttribute(Parameter param, String attr) {
//		val value = switch attr {
//			case "collectionFormat": param.collectionFormat
//			case "defaultValue": param.defaultValue
//			case "enum": param.enum
//			case "format": param.format
//			case "items": param.items
//			case "maxItems": param.maxItems
//			case "maxLength": param.maxLength
//			case "maximum": param.maximum
//			case "minItems": param.minItems
//			case "minLength": param.minLength
//			case "minimum": param.minimum
//			case "multipleOf": param.multipleOf
//			case "pattern": param.pattern
//			case "type": param.type
//			case "x-example": param.example
//		}
//		value ?: (param as AbstractParameter).getAttribute(attr)
//	}
//
//	def private getAttribute(BodyParameter param, String attr) {
//		val value = switch attr {
//			case "examples": param.examples
//			case "schema": param.schema
//		}
//		value ?: (param as AbstractParameter).getAttribute(attr)
//	}
//
//	def private getAttribute(RefParameter param, String attr) {
//		val value = switch attr {
//			case "$ref": param.$ref
//		}
//		value ?: (param as AbstractParameter).getAttribute(attr)
//	}
//
//	def private getAttribute(Parameter param, String attr) {
//		switch attr {
//			case "description": param.description
//			case "in": param.in
//			case "name": param.name
//			case "pattern": param.pattern
//			case "required": param.required
//		}
//	}
}
