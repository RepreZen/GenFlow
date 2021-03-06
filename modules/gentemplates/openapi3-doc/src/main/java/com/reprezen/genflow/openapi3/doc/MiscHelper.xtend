package com.reprezen.genflow.openapi3.doc

import java.util.Collection
import java.util.Map

class MiscHelper implements Helper {

	extension AttributeHelper attributeHelper

	override init() {
		this.attributeHelper = HelperHelper.attributeHelper
	}

	def <K, V> sortByPosition(Collection<K> keys, Map<K, V> map) {
		keys.sortBy[map.get(it)?.RZVE?.position ?: Integer::MAX_VALUE]
	}
}
