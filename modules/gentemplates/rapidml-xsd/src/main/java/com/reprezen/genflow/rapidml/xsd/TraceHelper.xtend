package com.reprezen.genflow.rapidml.xsd

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.api.trace.GenTemplateTraceBuilder.GenTemplateTraceItemBuilder
import com.reprezen.genflow.api.zenmodel.ZenModelLocator
import com.reprezen.genflow.api.zenmodel.util.CommonServices
import com.reprezen.rapidml.DataModel
import com.reprezen.rapidml.EnumConstant
import com.reprezen.rapidml.Enumeration
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.ReferenceElement
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.TypedRequest
import com.reprezen.rapidml.TypedResponse
import com.reprezen.rapidml.UserDefinedType
import com.reprezen.rapidml.ZenModel
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.EObject

class TraceHelper {

	val IGenTemplateContext context
	val Map<String, GenTemplateTraceItemBuilder> traceItems = Maps.newHashMap
	val ZenModelLocator locator
	extension ParamsHelper paramsHelper

	new(Helpers helpers) {
		this.context = helpers.context
		this.locator = helpers.zenModelLocator
		this.paramsHelper = helpers.paramsHelper
	}

	def traceForApi(ResourceAPI api) {
		api.getTrace('''resourceAPI:«locator.locate(api)»''')
	}

	def traceForComplexType(ResourceDefinition resource) {
		resource.getTrace('''complexType:«locator.locate(resource)»''')
	}

	def setTypeNameInTrace(EObject obj, String name) {
		obj.getTrace('''namesInSchema:«locator.locate(obj)»''', true).withProperty("typeName", name)
	}

	def setRootElementInTrace(EObject obj, String name) {
		obj.getTrace('''namesInSchema:«locator.locate(obj)»''', true).withProperty("rootElementName", name)
	}

	def setElementNameInTrace(EObject obj, String name) {
		obj.getTrace('''namesInSchema:«locator.locate(obj)»''', true).withProperty("elementName", name)
	}

	def setListElementInTrace(EObject obj, String name) {
		obj.getTrace('''namesInSchema:«locator.locate(obj)»''', true).withProperty("listElementName", name)
	}

	def setListItemElementInTrace(EObject obj, String name) {
		obj.getTrace('''namesInSchema:«locator.locate(obj)»''', true).withProperty("listItemElementName", name)
	}

	def setAttributeNameInTrace(EObject obj, String name) {
		obj.getTrace('''namesInSchema:«locator.locate(obj)»''', true).withProperty("attributeName", name)
	}

	def setIdInTrace(EObject obj) {
		obj.getTrace('''namesInSchema:«locator.locate(obj)»''', true)
	}

	def private getTrace(EObject obj, String key) {
		obj.getTrace(key, false)
	}

	def private getTrace(EObject obj, String key, boolean withId) {
		if (!traceItems.containsKey(key)) {
			val type = key.split(':').head
			val item = context.addTraceItem(type).withLocator(locator.locate(obj))
			traceItems.put(key, item)
		}
		val item = traceItems.get(key)
		if (withId) {
			item.withProperty('id', obj.id)
		}
		return item
	}

	val Map<String, EObject> idMap = Maps.newHashMap

	def private getId(EObject obj) {
		var id = switch (typeNamingMethod) {
			case SIMPLE_NAME: obj.simpleId
			case FULLY_QUALIFIED_NAME: obj.fqName
		}
		if (id.isValidIdFor(obj)) {
			return id
		} else {
			for (var i = 1;; i++) {
				val disamb = id + "_" + i
				if (disamb.isValidIdFor(obj)) {
					return disamb
				}
			}
		}
	}

	def private isValidIdFor(String id, EObject obj) {
		if (!idMap.containsKey(id)) {
			idMap.put(id, obj)
			return true
		} else if (obj === idMap.get(id)) {
			return true
		} else {
			return false
		}
	}

	def String getSimpleId(EObject obj) {
		switch (obj) {
			ResourceAPI: obj.name
			ResourceDefinition: obj.name
			TypedMessage: CommonServices.getMessageTypeName(obj)
			DataModel: obj.name
			Structure: obj.name
			Enumeration: obj.name
			UserDefinedType: obj.name
			Feature: obj.name
			ReferenceElement: obj.name
			PropertyRealization: obj.baseProperty.simpleId
			Method: obj.name
		}
	}

	def getFqName(EObject obj) {
		obj.fqNameComponents.filter[it !== null].join(".")
	}

	def Iterable<String> getFqNameComponents(EObject obj) {
		val List<String> parts = Lists.newArrayList
		var current = obj
		while (current !== null) {
			switch current {
				ZenModel: {
					parts.add(current.name)
					parts.add(current.namespace)
				}
				ResourceAPI:
					parts.add(current.name)
				ServiceDataResource:
					parts.add(current.name)
				Method:
					parts.add(current.httpMethod.getName())
				TypedRequest:
					parts.add("Request")
				TypedResponse:
					parts.add(current.statusCode.toString)
				DataModel:
					parts.add(current.name)
				Structure:
					parts.add(current.name)
				Feature:
					parts.add(current.name)
				ReferenceElement:
					parts.add(current.name)
				Enumeration:
					parts.add(current.name)
				EnumConstant:
					parts.add(current.name)
				UserDefinedType:
					parts.add(current.name)
			}
			current = current.eContainer
		}
		parts.reverse
	}
}
