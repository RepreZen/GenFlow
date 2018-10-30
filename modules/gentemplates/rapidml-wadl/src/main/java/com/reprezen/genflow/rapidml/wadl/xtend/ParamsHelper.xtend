package com.reprezen.genflow.rapidml.wadl.xtend

import com.google.common.collect.Maps
import com.reprezen.genflow.api.trace.GenTemplateTrace
import java.util.Map

class ParamsHelper {
	val Map<String, String> xsdParams
	
	new(GenTemplateTrace trace) {
		this.xsdParams = trace.traceItems.findFirst[it.type == "params"]?.properties ?: Maps.newHashMap
	}
	
	def isEltStyle() {
		return valueForm == "ELEMENT"
	}

	def isAttrStyle() {
		return valueForm == "ATTRIBUTE"
	}
	
	def private getValueForm() {
		return xsdParams.get("valueForm") ?: "ATTRIBUTE"
	}
	
	def getListItemElementName() {
		return xsdParams.get("listItemElementName") ?: "item"
	}

	def isAllowEmptyLists() {
		return Boolean.valueOf(xsdParams.get("allowEmptyLists") ?: "false")
	}
	
	def getTypeNamingMethod() {
		return xsdParams.get("typeNamingMethod") ?: "SIMPLE_NAME"
	}
}
