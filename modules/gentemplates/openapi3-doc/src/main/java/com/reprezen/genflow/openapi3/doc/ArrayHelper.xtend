package com.reprezen.genflow.openapi3.doc

import com.google.common.collect.Lists
import com.reprezen.kaizen.oasparser.model3.Schema

class ArrayHelper implements Helper {

	extension AttributeHelper attributeHelper
	extension KaiZenParserHelper = new KaiZenParserHelper

	override init() {
		attributeHelper = HelperHelper.attributeHelper
	}

	def Schema getElementType(Schema obj) {
		obj.collectItemTypes(true).last
	}

	def String getArrayTypeSpec(Schema obj) {
		val result = '''«obj.elementTypeName»«obj.arrayShape»'''
		return result
	}

	def String getElementTypeName(Schema obj) {
		val elementType = obj.elementType
		#[elementType?.kaiZenSchemaName, elementType?.type, elementType.rzveTypeName].filter[it !== null].head
	}

	def collectItemTypes(Schema obj, boolean includeFinal) {
		val result = Lists.<Schema>newArrayList
		var current = obj
		while (current.type == "array") {
			result.add(current)
			val item = current.itemsSchema
			if (item === null) {
				throw new BadArrayException("Array has no items type")
			} else if (result.contains(item)) {
				throw new BadArrayException("Array is (or is nested within) its own element type")
			} else {
				current = item
			}
		}
		if (includeFinal) {
			result.add(current)
		}
		result
	}

	def private String getArrayShape(Schema obj) {
		obj.collectItemTypes(false).map[it.arrayBounds].join
	}

	def private String getArrayBounds(Schema obj) {
		val max = obj.maxItems
		val min = obj.minItems
		switch null {
			case min === null && max === null:
				"[*]"
			case min == 0 && max == 1:
				"[?]"
			case min == 1 && max === null:
				"[+]"
			case min !== null && max === null: '''[«min»+]'''
			case min === null && max !== null: '''[..«max»]'''
			case min == max: '''[«min»]'''
			default: '''«min»..«max»'''
		}
	}

}

class BadArrayException extends Exception {
	new(String s) {
		super(s)
	}
}
