package com.reprezen.genflow.openapi3.doc

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import com.reprezen.genflow.api.normal.openapi.RepreZenVendorExtension
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Parameter
import com.reprezen.kaizen.oasparser.model3.Path
import com.reprezen.kaizen.oasparser.model3.Response
import com.reprezen.kaizen.oasparser.model3.Schema
import java.util.Map
import org.apache.commons.text.StringEscapeUtils

class HtmlHelper implements Helper {

	override init() {}

	val private Map<Object, String> htmlIds = Maps.newHashMap
	var private nextIdNum = 1

	def String getFileUrl(Object obj) {
		obj.RZVE?.fileUrl
	}

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

	def getPointer(Object obj) {
		obj.RZVE?.pointer
	}

	val private static mapper = new ObjectMapper()

	def private RepreZenVendorExtension getRZVE(Object obj) {
		val ext = switch (obj) {
			Schema:
				obj.getExtension(RepreZenVendorExtension.EXTENSION_NAME)
			Parameter:
				obj.getExtension(RepreZenVendorExtension.EXTENSION_NAME)
			Operation:
				obj.getExtension(RepreZenVendorExtension.EXTENSION_NAME)
			Path:
				obj.getExtension(RepreZenVendorExtension.EXTENSION_NAME)
			Response:
				obj.getExtension(RepreZenVendorExtension.EXTENSION_NAME)
			OpenApi3:
				obj.getExtension(RepreZenVendorExtension.EXTENSION_NAME)
			default:
				null
		}
		if(ext !== null) mapper.convertValue(ext, RepreZenVendorExtension)
	}

	def String escapeHtmlId(String jsonPointer) {
		jsonPointer.replaceAll("[^a-zA-Z0-9_]", "_")
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
