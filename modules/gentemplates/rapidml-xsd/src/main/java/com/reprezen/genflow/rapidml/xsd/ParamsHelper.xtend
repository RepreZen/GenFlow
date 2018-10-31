package com.reprezen.genflow.rapidml.xsd

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.reprezen.genflow.rapidml.xsd.XMLSchemaGenTemplate.Config
import com.reprezen.genflow.rapidml.xsd.XMLSchemaGenTemplate.ValueForm

class ParamsHelper {

	val Config config

	new(Helpers helpers) {
		this.config = new ObjectMapper(new YAMLFactory()).convertValue(helpers.context.genTargetParameters, Config)
	}

	def eltStyle() {
		config.valueForm == ValueForm.ELEMENT
	}

	def attrStyle() {
		return config.valueForm == ValueForm.ATTRIBUTE
	}

	def getListItemElementName() {
		return config.listItemElementName
	}

	def allowEmptyLists() {
		config.allowEmptyLists
	}

	def typeNamingMethod() {
		config.typeNamingMethod
	}
}
