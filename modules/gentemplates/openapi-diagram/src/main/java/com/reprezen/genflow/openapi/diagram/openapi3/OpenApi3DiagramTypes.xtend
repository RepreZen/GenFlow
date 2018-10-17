/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.openapi3

import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Parameter
import com.reprezen.kaizen.oasparser.model3.RequestBody
import com.reprezen.kaizen.oasparser.model3.Schema

class OpenApi3DiagramTypes {
	static final String UNRESOLVED = "<unresolved>"
	static final String UNKNOWN = "UNKNOWN"

	extension KaiZenParserHelper = new KaiZenParserHelper

	def String parameterLabel(Parameter param) {
		return formatParameterLabel(param.name, computeType(param))
	}

	def String computeType(RequestBody bodyParam) {
		val schema = bodyParam.contentMediaTypes.values.head?.schema
		if (schema === null) {
			return UNRESOLVED
		}
		return schemaLabel("Request", schema)
	}

	def String computeType(Parameter param) {
		val result = propertyTypeLabel(param.schema.type, param.schema)
		return if(result.nullOrEmpty) UNKNOWN else result
	}

	def String schemaLabel(String context, OpenApi3 model) {
		val result = model.info.title
		return result
	}

	def String schemaLabel(String context, Schema model) {
		// no minItems for ArrayModel 
		// val atLeastOne = false
		return context
	// FIXME
	// return arrayTypeLabel(context, model.items, atLeastOne)
	}

	def String propertyTypeLabel(String context, Schema type) {
		switch (type) {
			case null:
				return ""
			case type.type == "array": {
				return propertyTypeLabel(context, type.itemsSchema) +
					cardinalitySuffix(type.minItems !== null && type.minItems > 0)
			}
			case #{"boolean", "integer", "null", "number", "string"}.contains(type.type): {
				return primitiveTypeLabel(type.type, type.format)
			}
			default: {
				return type.schemaTitle;
			}
		}
	}

	def primitiveTypeLabel(String type, String format) {
		return if(format.nullOrEmpty) type else format
	}

	def formatParameterLabel(String name, String type) {
		return '''«name» : «type»'''
	}

	def cardinalitySuffix(boolean atLeastOne) {
		if(atLeastOne) "+" else "*"
	}

}
