package com.reprezen.genflow.swagger.doc

import com.google.common.collect.Lists

class ArrayHelper implements Helper {

	extension AttributeHelper attributeHelper
	extension RefHelper refHelper

	override init() {
		attributeHelper = HelperHelper.attributeHelper
		refHelper = HelperHelper.refHelper
	}

	def Object getElementType(Object obj) {
		obj.resolve.collectItemTypes(true).last
	}

	def String getArrayTypeSpec(Object obj) {
		'''«obj.elementTypeName»«obj.arrayShape»'''
	}

	def String getElementTypeName(Object obj) {
		val elementType = obj.elementType
		#[elementType?.type, elementType.rzveTypeName].filterNull.last
	}

	def collectItemTypes(Object obj, boolean includeFinal) {
		val result = Lists.<Object>newArrayList
		var current = obj
		while (current.type == "array") {
			result.add(current)
			val item = current.items?.resolve
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

	def private String getArrayShape(Object obj) {
		obj.collectItemTypes(false).map[it.arrayBounds].join
	}

	def private String getArrayBounds(Object obj) {
		val max = obj.maxItems
		val min = obj.minItems
		switch null {
			case (min === null || min === 0) && (max === null || max === 0):
				"[*]"
			case (min === null || min === 0) && max === 1:
				"[?]"
			case (min !== null && min === 1) && (max === null || max === 0):
				"[+]"
			case min !== null && max === null: '''[«min»+]'''
			case min === null && max !== null: '''[..«max»]'''
			case min == max: '''[«min»]'''
			default: '''[«min»..«max»]'''
		}
	}

}

class BadArrayException extends Exception {
	new(String s) {
		super(s)
	}
}
