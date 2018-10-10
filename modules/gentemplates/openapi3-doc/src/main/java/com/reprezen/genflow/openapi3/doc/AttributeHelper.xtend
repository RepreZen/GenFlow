package com.reprezen.genflow.openapi3.doc

import com.reprezen.genflow.api.normal.openapi.RepreZenVendorExtension
import com.reprezen.jsonoverlay.JsonOverlay
import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.model3.Parameter
import com.reprezen.kaizen.oasparser.model3.Schema
import java.util.List

class AttributeHelper implements Helper {

	var ParameterHelper parameterHelper
	var PropertyHelper propertyHelper
	var ModelHelper modelHelper

	override init() {
		propertyHelper = HelperHelper.propertyHelper
		parameterHelper = HelperHelper.parameterHelper
		modelHelper = HelperHelper.modelHelper
	}

	def dispatch getAttribute(Object obj, String attr) {
		null
	}

// FIXME adapt to OAS3
//	def dispatch getAttribute(Property prop, String attr) {
//		propertyHelper.getAttribute(prop, attr)
//	}

	def dispatch getAttribute(Parameter param, String attr) {		
		parameterHelper.getAttribute(param, attr)
	}

	def dispatch getAttribute(Schema model, String attr) {
		modelHelper.getAttribute(model, attr)
	}

	/* Convenience method for specific attributes */
	def getName(Object obj) {
		obj.getAttribute("name") as String
	}

	def getDefaultValue(Object obj) {
		obj.getAttribute("defaultValue")
	}

	def getTitle(Object obj) {
		obj.getAttribute("title") as String
	}

	def getRZVE(Object obj) {
		if (obj instanceof JsonOverlay<?>) {
			RepreZenVendorExtension.get(Overlay.of(obj).toJson)
		} else {
			RepreZenVendorExtension.get(obj)
		}
	}

	def getRzveTypeName(Object obj) {
		obj.RZVE?.typeName
	}

	def getMinimum(Object obj) {
		obj.getAttribute("minimum")?.toString
	}

	def getMaximum(Object obj) {
		obj.getAttribute("maximum")?.toString
	}

	def getExclusiveMinimum(Object obj) {
		Boolean.valueOf(obj.getAttribute("exclusiveMinimum") as String)
	}

	def getExclusiveMaximum(Object obj) {
		Boolean.valueOf(obj.getAttribute("exclusiveMaximum") as String)
	}

	def getMultipleOf(Object obj) {
		// defined in swagger spec, but not currently supported in swagger models
		null as Double
	}

	def getMinLength(Object obj) {
		(obj.getAttribute("minLength") as Integer) ?: 0
	}

	def getMaxLength(Object obj) {
		(obj.getAttribute("maxLength") as Integer) ?: Integer::MAX_VALUE
	}

	def getPattern(Object obj) {
		obj.getAttribute("pattern") as String
	}

	def getEnums(Object obj) {
		obj.getAttribute("enum") as List<?>
	}

	def getUniqueItems(Object obj) {
		// defined in Swagger spec but not currently supported in swagger models
		null as Boolean
	}

	def getExample(Object obj) {
		// x-example vendor extension on serializable parameters
		obj.getAttribute("x-example")?.toString
	}
}
