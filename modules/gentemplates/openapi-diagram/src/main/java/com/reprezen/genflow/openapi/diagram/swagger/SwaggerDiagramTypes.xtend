/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.swagger

import io.swagger.models.ArrayModel
import io.swagger.models.Model
import io.swagger.models.ModelImpl
import io.swagger.models.RefModel
import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.RefParameter
import io.swagger.models.parameters.SerializableParameter
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.MapProperty
import io.swagger.models.properties.ObjectProperty
import io.swagger.models.properties.Property
import io.swagger.models.properties.RefProperty
import java.util.Map

class SwaggerDiagramTypes {
	val static String UNRESOLVED = "<unresolved>"
	val static String UNKNOWN = "UNKNOWN"

	extension XtendHelper = new XtendHelper

	val Map<String, Parameter> sharedParams

	new(Swagger model) {
		this.sharedParams = model.parameters.safe
	}

	def dispatch String parameterLabel(Parameter param) {
		return formatParameterLabel(param.name, computeType(param))
	}

	def dispatch String parameterLabel(RefParameter refParam) {
		val ref = refParam.simpleRef
		val realParam = sharedParams.get(ref)
		val type = if(realParam === null) UNRESOLVED else computeType(realParam)
		return formatParameterLabel(ref, type)
	}

	def dispatch String computeType(Parameter parameter) {
		throw new IllegalArgumentException('''Unknown parameter kind: «parameter»''')
	}

	def dispatch String computeType(RefParameter refParam) {
		throw new IllegalStateException("should never be called, see parameterLabel(RefParameter)")
	}

	def dispatch String computeType(BodyParameter bodyParam) {
		val schema = bodyParam.schema
		if (schema === null) {
			return UNRESOLVED
		}
		return schemaLabel("Request", schema)
	}

	def dispatch String computeType(SerializableParameter param) {
		val result = primitiveTypeLabel(param.type, param.format)
		return if(result.nullOrEmpty) UNKNOWN else result
	}

	def dispatch String schemaLabel(String context, Model model) {
		val result = (model as ModelImpl).type
		return result
	}

	def dispatch String schemaLabel(String context, ArrayModel model) {

		// no minItems for ArrayModel 
		val atLeastOne = false
		return arrayTypeLabel(context, model.items, atLeastOne)
	}

	def dispatch String schemaLabel(String context, RefModel refModel) {
		return refModel.simpleRef
	}

	def String propertyTypeLabel(String context, Model schema) {
		return context.spaceIfNeeded("Object (inline)");
	}

	def String propertyTypeLabel(String context, Property type) {
		switch (type) {
			case null:
				return ""
			ObjectProperty:
				return context.spaceIfNeeded("Object (inline)")
			MapProperty:
				return context.spaceIfNeeded("Object (inline)")
			ArrayProperty:
				return arrayTypeLabel(context, type.items, type.minItems !== null && type.minItems > 0)
			RefProperty:
				return type.simpleRef
			case type.format !== null || type.type !== null: {
				return primitiveTypeLabel(type.type, type.format)
			}
			default:
				return UNKNOWN
		}
	}

	def String arrayTypeLabel(String context, Property items, boolean atLeastOne) {
		switch (items) {
			case null:
				return UNRESOLVED
			ObjectProperty:
				return context.spaceIfNeeded("Array (inline)")
			MapProperty:
				return context.spaceIfNeeded("Array (inline)")
			RefProperty:
				return items.simpleRef + cardinalitySuffix(atLeastOne)
			case items.format !== null || items.type !== null: {
				return primitiveTypeLabel(items.type, items.format) + atLeastOne
			}
			default:
				return UNKNOWN
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
