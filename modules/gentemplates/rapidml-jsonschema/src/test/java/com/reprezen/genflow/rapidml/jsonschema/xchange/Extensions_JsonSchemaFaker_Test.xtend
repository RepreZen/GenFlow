package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class Extensions_JsonSchemaFaker_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
rapidModel Extensions
	resourceAPI ExtensionsInterface baseURI "http://taxblaster.com/api"

		objectResource PersonObject type Person
			URI people/{id}

		objectResource PersonObjectWithRealization type PersonToTestRealization
			URI people_custom/{id}
	
			with all properties including
				name!
			extensions
				x-resourceExt: "res-extension"
	
	dataModel ExtensionsDataModel
		structure Person
			name: string!
				extensions
					x-faker: "name.findName"
			email: string!
				extensions
					x-faker: "internet.email"	
			enumProp: MyEnum
				extensions
					x-faker: "enum"
			simpleProp: MySimpleType
				extensions
					x-faker: "simple"
			ref: reference to DataType2
				extensions
					x-faker: "ref.extension"	
			extensions
				x-foo: "datatype-extension"

		structure PersonToTestRealization
			name: string!
				extensions
					x-faker: "name.findName"
			email: string!
				extensions
					x-faker: "internet.email"	
			enumProp: MyEnum
				extensions
					x-faker: "enum"
			simpleProp: MySimpleType
				extensions
					x-faker: "simple"
			ref: reference to DataType2
				extensions
					x-faker: "ref.extension"	
			extensions
				x-foo: "datatype-extension"
				x-faker: "faker"
		

		structure DataType2
			name: string
			
		enum int MyEnum
			ITEM1
			ITEM2
		
		simpleType MySimpleType defined as string
			of length from 3 to 10
'''
	}

	@Test
	def legacy_Person() {
		testLegacy('Person', '''
			type: "object"
			x-foo: "datatype-extension"
			properties:
			  name:
			    type: "string"
			    x-faker: "name.findName"
			  email:
			    type: "string"
			    x-faker: "internet.email"
			  enumProp:
			    $ref: "#/definitions/MyEnum"
			    x-faker: "enum"
			  simpleProp:
			    type: "string"
			    minLength: 3
			    maxLength: 10
			    x-faker: "simple"
			  ref:
			    $ref: "#/definitions/DataType2"
			    x-faker: "ref.extension"
			required:
			- "name"
			- "email"
		''')
	}

	@Test
	def contract_Person() {
		testContract('Person', '''
			type: object
			minProperties: 1
			x-foo: datatype-extension
			properties:
			  name:
			    type: string
			    minLength: 1
			    x-faker: name.findName
			  email:
			    type: string
			    minLength: 1
			    x-faker: internet.email
			  enumProp:
			    "$ref": "#/definitions/MyEnum"
			    x-faker: enum
			  simpleProp:
			    type: string
			    minLength: 3
			    maxLength: 10
			    x-faker: simple
			  ref:
			    "$ref": "#/definitions/DataType2"
			    x-faker: ref.extension
			required:
			- name
			- email
		''')
	}

	@Test
	def interop_Person() {
		testInterop('Person', '''
			type: object
			minProperties: 1
			x-foo: datatype-extension
			properties:
			  name:
			    type: string
			    minLength: 1
			    x-faker: name.findName
			  email:
			    type: string
			    minLength: 1
			    x-faker: internet.email
			  enumProp:
			    "$ref": "#/definitions/MyEnum"
			    x-faker: enum
			  simpleProp:
			    type: string
			    minLength: 3
			    maxLength: 10
			    x-faker: simple
			  ref:
			    "$ref": "#/definitions/DataType2"
			    x-faker: ref.extension
			  _links:
			    "$ref": "#/definitions/_RapidLinksMap"
		''')
	}
	@Test
	def legacy_PersonRealization() {
		testLegacy('PersonObjectWithRealization', '''
			type: object
			x-foo: datatype-extension
			x-faker: faker
			x-resourceExt: res-extension
			properties:
			  name:
			    type: string
			    x-faker: name.findName
			  email:
			    type: string
			    x-faker: internet.email
			  enumProp:
			    "$ref": "#/definitions/MyEnum"
			    x-faker: enum
			  simpleProp:
			    type: string
			    minLength: 3
			    maxLength: 10
			    x-faker: simple
			  ref:
			    "$ref": "#/definitions/DataType2"
			    x-faker: ref.extension
			required:
			- name
			- email
		''')
	}

	@Test
	def contract_PersonRealization() {
		testContract('PersonObjectWithRealization', '''
			type: object
			minProperties: 1
			x-foo: datatype-extension
			x-faker: faker
			x-resourceExt: res-extension
			properties:
			  name:
			    type: string
			    minLength: 1
			    x-faker: name.findName
			  email:
			    type: string
			    minLength: 1
			    x-faker: internet.email
			  enumProp:
			    "$ref": "#/definitions/MyEnum"
			    x-faker: enum
			  simpleProp:
			    type: string
			    minLength: 3
			    maxLength: 10
			    x-faker: simple
			  ref:
			    "$ref": "#/definitions/DataType2"
			    x-faker: ref.extension
			required:
			- name
			- email
		''')
	}

	@Test
	def interop_PersonRealization() {
		testInterop('PersonToTestRealization', '''
			type: object
			minProperties: 1
			x-foo: datatype-extension
			x-faker: faker
			properties:
			  name:
			    type: string
			    minLength: 1
			    x-faker: name.findName
			  email:
			    type: string
			    minLength: 1
			    x-faker: internet.email
			  enumProp:
			    "$ref": "#/definitions/MyEnum"
			    x-faker: enum
			  simpleProp:
			    type: string
			    minLength: 3
			    maxLength: 10
			    x-faker: simple
			  ref:
			    "$ref": "#/definitions/DataType2"
			    x-faker: ref.extension
			  _links:
			    "$ref": "#/definitions/_RapidLinksMap"
		''')
	}
}
