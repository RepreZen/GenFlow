package com.reprezen.genflow.common.jsonschema.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.base.Strings
import com.reprezen.genflow.common.xtend.ExtensionsHelper
import com.reprezen.rapidml.Extension
import com.reprezen.rapidml.RealizationContainer
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.xtext.util.ZenModelHelper

abstract class NamedSchemaNode<T> extends JsonSchemaNode<T>{

	protected extension ZenModelHelper = new ZenModelHelper
	protected extension ExtensionsHelper = new ExtensionsHelper

	new(JsonSchemaNodeFactory factory, T element) {
		super(factory, element)
	}
	
	override ObjectNode write(ObjectNode parentNode) {
		val body = parentNode.putObject(getName)
		writeBody(body)
		return body
	}

	def void writeBody(ObjectNode bodyNode);
	
	def String getName();

	def protected Iterable<String> getRequiredPropertyNames(RealizationContainer realization) {
		return realization.getProperties()?.getAllIncludedProperties().filter[e|e.minOccurs > 0].map [
			baseProperty.name
		]
	}
	
	def protected Iterable<String> getRequiredPropertyNames(Structure datatype) {
		return datatype.ownedFeatures.filter[e|e.minOccurs > 0].map[name]
	}
	
	def protected ObjectNode writeRequiredProperties(ObjectNode node, Iterable<String> requiredPropNames) {
		if (!factory.schemaFormat.defineRequiredElementsInJsonSchemaV3Style && !requiredPropNames.empty) {
			val requiredNode = node.putArray("required")
			requiredPropNames.forEach[requiredNode.add(it)]
		}
		return node
	}
	
	def protected writePropertyAsRequired(ObjectNode node) {
		if (factory.schemaFormat.defineRequiredElementsInJsonSchemaV3Style) {
			node.put("required", true)
		}
		return node
	}
	
	def protected putDescription(ObjectNode body, String doc) {
		if (!Strings.isNullOrEmpty(doc)) {
			body.put("description", doc)
		}
	}
	
	def protected addVendorExtensions(ObjectNode body, Iterable<Extension> extensions) {
		extensions?.forEach[body.addVendorExtension(it.name, it.value)]
	}
	
	def protected addVendorExtension(ObjectNode body, String tag, String value) {
		body.put(tag, value)
	}
	
	def protected Description(ObjectNode body, String doc) {
		if (!Strings.isNullOrEmpty(doc)) {
			body.put("description", doc)
		}
	}	
	
	def protected setObjectAsType(ObjectNode body) {
		body.put("type", "object")
		if (!factory.options.allowEmptyObject) {
			body.put("minProperties", 1)
		}
		return body
	}
	
}
