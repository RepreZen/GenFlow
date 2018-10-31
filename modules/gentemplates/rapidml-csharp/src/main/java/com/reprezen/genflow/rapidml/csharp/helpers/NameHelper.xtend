package com.reprezen.genflow.rapidml.csharp.helpers

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.Maps
import com.reprezen.rapidml.Enumeration
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.ZenModel
import java.util.Map

class NameHelper {
	val static Map<String, NameHelper> helpers = Maps.newHashMap()

	def static forModel(ZenModel model) {
		val qname = model.namespace + "." + model.name
		helpers.get(qname) ?: {
			val helper = new NameHelper
			helpers.put(qname, helper)
			helper
		}
	}

	val BiMap<String, String> namespaceNames = HashBiMap.create

	def getNamespaceName(String... components) {
		var orig = components.get(0)
		var namespace = components.get(0).initialUpper.resolve(orig, namespaceNames)
		for (var i = 1; i < components.length; i++) {
			orig += "." + components.get(i)
			namespace = (namespace + "." + components.get(i).initialUpper).resolve(orig, namespaceNames)
		}
		namespace
	}

	val BiMap<String, String> typeInterfaceNames = HashBiMap.create

	def getTypeInterfaceName(Structure type) {
		val name = type.name
		val suggested = "I" + name.initialUpper
		suggested.resolve(name, typeInterfaceNames)
	}

	val BiMap<String, String> typePocoNames = HashBiMap.create

	def getTypePocoName(Structure type) {
		val name = type.name
		val suggested = name.initialUpper
		suggested.resolve(name, typePocoNames)
	}

	val BiMap<String, String> enumerationNames = HashBiMap.create

	def getEnumerationName(Enumeration enumeration) {
		val name = enumeration.name
		val suggested = name.initialUpper
		suggested.resolve(name, enumerationNames)
	}

	val BiMap<String, String> resourceNames = HashBiMap.create

	def getResourceName(ResourceDefinition resource) {
		val name = resource.name
		val suggested = name.initialUpper
		suggested.resolve(name, resourceNames)
	}

	def initialUpper(String s) {
		s.substring(0, 1).toUpperCase + s.substring(1)
	}

	def private resolve(String suggestedName, String originalName, BiMap<String, String> nameMap) {
		if (nameMap.containsKey(originalName)) {
			return nameMap.get(originalName)
		}
		val name = suggestedName.disambiguate(nameMap)
		nameMap.put(originalName, name)
		name
	}

	def private disambiguate(String name, BiMap<String, String> nameMap) {
		var result = name
		var i = 1
		while (nameMap.containsValue(result)) {
			result = name + "_" + i++
		}
		result
	}

}
