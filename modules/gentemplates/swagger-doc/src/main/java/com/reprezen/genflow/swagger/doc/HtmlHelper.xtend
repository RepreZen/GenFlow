package com.reprezen.genflow.swagger.doc

import com.google.common.collect.Maps
import com.reprezen.genflow.api.normal.openapi.RepreZenVendorExtension
import java.util.Map
import org.apache.commons.text.StringEscapeUtils

class HtmlHelper implements Helper {

	override init() {}

	val private Map<Object, String> htmlIds = Maps.newHashMap
	var private nextIdNum = 1

	def String htmlEscape(String value) {
		StringEscapeUtils::escapeHtml4(value)
	}

	def String htmlEscape(NoEscapeString value) {
		value.get
	}

	def NoEscapeString noEscape(String value) {
		return new NoEscapeString(value)
	}

	def String getCode(String s) {
		if(s.empty) s else '''<code>«s»</code>'''
	}

	def String getSamp(String s) {
		if(s.empty) s else '''<samp>«s»</samp>'''
	}

	def getHtmlId(Object obj) {
		val id = htmlIds.get(obj) ?: mkId(obj)
		return id
	}

	def getHtmlId(Object obj, String role) {
		obj.htmlId + (role ?: "")
	}

	def private mkId(Object obj) {
		obj.pointer?.escapeHtmlId ?: {
			val id = String.format("_%05d", nextIdNum)
			nextIdNum = nextIdNum + 1
			htmlIds.put(obj, id)
			id
		}
	}

	def String escapeHtmlId(String jsonPointer) {
		jsonPointer.replaceAll("[^a-zA-Z0-9_]", "_")
	}

	def getPointer(Object obj) {
		RepreZenVendorExtension.get(obj)?.pointer
	}
}

package class NoEscapeString {
	val private String value

	new(String value) {
		this.value = value
	}

	def String get() {
		value
	}
}
