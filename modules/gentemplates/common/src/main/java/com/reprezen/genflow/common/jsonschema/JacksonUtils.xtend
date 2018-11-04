package com.reprezen.genflow.common.jsonschema

import com.fasterxml.jackson.databind.node.ObjectNode

class JacksonUtils {
	def public dispatch putNumber(ObjectNode node, String propName, Integer num) {
		node.put(propName, num)
	}

	def public dispatch putNumber(ObjectNode node, String propName, Float num) {
		node.put(propName, num)
	}

	def public dispatch putNumber(ObjectNode node, String propName, Long num) {
		node.put(propName, num)
	}

	def public dispatch putNumber(ObjectNode node, String propName, Double num) {
		node.put(propName, num)
	}

	def public dispatch putNumber(ObjectNode node, String propName, Short num) {
		node.put(propName, num)
	}

	def public dispatch putNumber(ObjectNode node, String propName, Number num) {
		node.put(propName, num.toString)
	}
}
