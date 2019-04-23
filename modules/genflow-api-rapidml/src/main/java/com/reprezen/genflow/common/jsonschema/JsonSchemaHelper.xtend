/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.jsonschema

import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.PrimitiveType
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.ReferenceElement
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.SingleValueType
import org.eclipse.emf.ecore.EObject

class JsonSchemaHelper {

	def dispatch boolean isArrayProperty(ReferenceTreatment refEmbedOrLink) {
		val referencedProperty = getReferencedProperty(refEmbedOrLink)
		return ((referencedProperty.maxOccurs == -1) || (referencedProperty.maxOccurs > 1))
	}

	def dispatch boolean isArrayProperty(PropertyRealization prop) {
		((1 < prop.maxOccurs) || (-1 == prop.maxOccurs))
	}

	def dispatch boolean isArrayProperty(Feature prop) {
		((1 < prop.maxOccurs) || (-1 == prop.maxOccurs))
	}

	def dispatch boolean isArrayProperty(EObject prop) {
		false
	}

	def ReferenceElement getReferencedProperty(ReferenceTreatment refEmbed) {
		refEmbed.referenceElement
	}

	def String getJSONSchemaTypeName(PrimitiveType type) {
		val typeName = type.name
		switch typeName {
			case #["anyURI", "duration", "gMonth", "gMonthDay", "gDay", "gYearMonth", "gYear", "QName", "time", "string", "NCName"].
				contains(typeName): "string"
			case "boolean": "boolean"
			case "base64Binary": "string"
			case "date": "string"
			case "dateTime": "string"
			case "decimal": "number"
			case "double": "number"
			case "float": "number"
			case "integer": "integer"
			case "int": "integer"
			case "long": "integer"
			default: "string"
		}
	}

	def String getJSONSchemaTypeName(SingleValueType type) {
		type.primitiveType.JSONSchemaTypeName
	}

	def String getJSONSchemaTypeFormat(PrimitiveType type) {
		switch type.name {
			case "date": "date"
			case "dateTime": "date-time"
			case "double": "double"
			case "float": "float"
			case "long": "int64"
		}
	}

	def String getJSONSchemaTypeFormat(SingleValueType type) {
		type.primitiveType.JSONSchemaTypeFormat
	}

	def isZenNumericType(String typeName) {
		#["decimal", "double", "float", "integer", "int", "long"].contains(typeName)
	}
}
