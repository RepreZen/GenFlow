package com.reprezen.genflow.rapidml.csharp.helpers

import com.reprezen.rapidml.ZenModel
import java.util.Map
import java.util.Set

class TypeHelper {

	val static Map<String, TypeHelper> helpers = newHashMap

	def static forModel(ZenModel model) {
		val qname = model.namespace + "." + model.name
		helpers.get(qname) ?: {
			val helper = new TypeHelper
			helpers.put(qname, helper)
			helper
		}
	}

	private new() {
	}

	def maybeNullable(String type) {
		if(type.isValueType) type + "?" else type
	}

	def private isValueType(String type) {
		// we don't check for user-defined structs because we don't generate any, but we do use a small 
		// number of standard .NET structs to represent some of the RAPID-ML built-in types.
		type.isPrimitiveValueType || type.isEnumerationType || type.isKnownStruct
	}

	val static primitiveValueTypes = #{
		"sbyte",
		"byte",
		"char",
		"short",
		"ushort",
		"int",
		"uint",
		"long",
		"ulong", //
		"float",
		"double",
		"decimal", //
		"bool"
	};

	def private isPrimitiveValueType(String type) {
		primitiveValueTypes.contains(type);
	}

	val Set<String> enumerationTypes = newHashSet

	def private isEnumerationType(String type) {
		enumerationTypes.contains(type)
	}

	def noteEnumerationType(String type) {
		enumerationTypes.add(type)
	}

	val static cSharpTypeMap = #{
		"NCName" -> "string",
		"QName" -> "string",
		"anyURI" -> "string",
		"base64Binary" -> "byte[]",
		"boolean" -> "bool",
		"date" -> "DateTime",
		"dateTime" -> "DateTime",
		"decimal" -> "decimal", // not really decimal. It's a 128-bit float!!!
		"duration" -> "TimeSpan",
		"gDay" -> "byte",
		"gMonth" -> "byte",
		"gMonthDay" -> "ushort", // month*100+day
		"gYear" -> "uint",
		"int" -> "int",
		"integer" -> "BigInteger",
		"long" -> "long",
		"string" -> "string",
		"time" -> "DateTime"
	}
	val static cSharpTypes = newHashSet(cSharpTypeMap.values)

	def getCsharpType(String type) {
		cSharpTypeMap.get(type) ?: type
	}

	def private isKnownStruct(String type) {
		cSharpTypes.contains(type)
	}

}
