package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.base.Strings
import com.reprezen.rapidml.ConstrainableType
import com.reprezen.rapidml.Enumeration
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.SingleValueType

abstract class PropertyNode<T> extends TypedNode<T> {

	new(JsonSchemaNodeFactory director, T element) {
		super(director, element)
	}

	override writeBody(ObjectNode bodyNode) {
		if (isArrayProperty) {
			writeArray(bodyNode)
		} else {
			writeObject(bodyNode)
		}
		bodyNode.addVendorExtensions(baseFeature?.getRapidExtensions())
	}

	def void writeArray(ObjectNode body) {
		body.putDescription(getPropertyDocumentation())
		body.put("type", "array")
		
		var minOccursValue = getMinOccurs() 
		if (!factory.options.allowEmptyArray) {
			minOccursValue = Math.max(1, minOccursValue);
		}
		if (minOccursValue > 0) {
			body.put("minItems", minOccursValue)
		}
		
		if (getMaxOccurs() > 0) {
			body.put("maxItems", getMaxOccurs())
		}
		val items = body.putObject("items")
		writeType(items)
	}

	def void writeObject(ObjectNode body) {
		val doc = getPropertyDocumentation()
		if (!Strings.isNullOrEmpty(doc)) {
			body.put("description", doc)
		}
		writeType(body)
	}

	def protected ObjectNode writeConstrainableType(ObjectNode node, ConstrainableType property, SingleValueType type) {
		if (type instanceof Enumeration) {
			return writeEnum(node, type as Enumeration)
		}
		node.writePropertyType(type)		
		if (property.isReadOnly) {			
			node.put("readOnly", true)			
		}
		for (constraint : property.allConstraints) {
			factory.createConstraintNode(constraint).write(node)
		}
		return node
	}

	def private ObjectNode writeEnum(ObjectNode node, Enumeration enumType) {
		val enumerationBuilder = factory.createEnumerationNode(enumType)
		if (factory.schemaFormat.inlineSimpleTypes) {
			enumerationBuilder.write(node)
		} else {
			factory.definitionsNode.addReferenceToDefinition(node, enumerationBuilder)
			return node
		}
	}

	def String getPropertyDocumentation() {
		getDocumentation(baseFeature)
	}
	
	def abstract void writeType(ObjectNode body);

	def isArrayProperty() {
		isMultiValued
	}
	
	def isMultiValued() {
		(1 < maxOccurs) || (-1 == maxOccurs)
	}
	
	def abstract int getMinOccurs();

	def abstract int getMaxOccurs();
	
	def abstract Feature getBaseFeature();

	def dispatch private isReadOnly(ConstrainableType property) { 
		return false
	}

	def dispatch private isReadOnly(PropertyRealization property) { 
		return property.baseProperty.readOnly
	}
	
	def dispatch private isReadOnly(PrimitiveProperty property) { 
		return property.readOnly
	}

}
