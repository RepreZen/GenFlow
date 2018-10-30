package com.reprezen.genflow.common.jsonschema.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.common.jsonschema.JsonSchemaHelper
import com.reprezen.genflow.common.xtend.XImportHelper
import com.reprezen.rapidml.EnumConstant
import com.reprezen.rapidml.Enumeration

class EnumerationNode extends TypedNode<Enumeration> {
	protected extension JsonSchemaHelper = new JsonSchemaHelper
	val XImportHelper importHelper

	new(JsonSchemaNodeFactory director, Enumeration element, XImportHelper importHelper) {
		super(director, element)
		this.importHelper = importHelper
	}

	override getName() {
		importHelper.getQualifiedName(element)
	}

	override writeBody(ObjectNode body) {
		body.writePropertyType(element.baseType)
		body.putDescription(getDocumentation(element))
		val isNumeric = element.baseType.name.isZenNumericType
		val enumValues = element.enumConstants.map[getEnumConstant(it, isNumeric)]
		val enumNode = body.putArray("enum")
		enumValues.forEach[if(isNumeric) enumNode.add(it as Integer) else enumNode.add(it as String)]
	}

	def private Object getEnumConstant(EnumConstant constant, boolean isNumeric) {
		if (isNumeric) {
			constant.integerValue
		} else {
			constant.literalValue ?: constant.name
		}
	}

}
