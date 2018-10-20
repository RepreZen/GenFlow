package com.reprezen.genflow.swagger.doc

import com.reprezen.genflow.api.normal.openapi.RepreZenVendorExtension
import io.swagger.models.Model
import io.swagger.models.parameters.Parameter
import io.swagger.models.properties.Property
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

	def dispatch getAttribute(Property prop, String attr) {
		propertyHelper.getAttribute(prop, attr)
	}

	def dispatch getAttribute(Parameter param, String attr) {
		parameterHelper.getAttribute(param, attr)
	}

	def dispatch getAttribute(Model model, String attr) {
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

	def getType(Object obj) {
		obj.getAttribute("type") as String
	}

	def getFormat(Object obj) {
		obj.getAttribute("format") as String
	}

	def getItems(Object obj) {
		obj.getAttribute("items")
	}

	def getMaxItems(Object obj) {
		obj.getAttribute("maxItems") as Integer
	}

	def getMinItems(Object obj) {
		obj.getAttribute("minItems") as Integer
	}

	def getDescription(Object obj) {
		obj.getAttribute("description") as String
	}

	def getRZVE(Object obj) {
		RepreZenVendorExtension.get(obj)
	}

	def getRzveTypeName(Object obj) {
		obj.getRZVE?.typeName
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
