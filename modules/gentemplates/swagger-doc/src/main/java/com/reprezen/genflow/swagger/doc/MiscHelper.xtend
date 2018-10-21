package com.reprezen.genflow.swagger.doc

import java.util.Collection
import java.util.Map

class MiscHelper implements Helper {

	extension AttributeHelper attributeHelper

	override init() {
		this.attributeHelper = HelperHelper.attributeHelper
	}

	def <K, V> sortByPosition(Collection<K> keys, Map<K, V> map) {
		keys.sortBy[map.get(it)?.getRZVE?.position ?: Integer::MAX_VALUE]
	}
}
