package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class Nested_ReferenceLink_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
rapidModel Realizations
	resourceAPI RealizationsAPI baseURI "http://my-namespace.com"

		objectResource DataType1Object type DataType1
			URI /uri
		
		objectResource ReferencedResource type Nested2
			URI /uri2
		
	dataModel RealizationsDataModel
		structure DataType1
			id : string
			prop1: string
			ref1: reference to Nested1
			
		structure Nested1
			nested1_prop1: string
			ref2: reference to Nested2
			
		structure Nested2
			nested2_prop1: string
'''
	}

	@Test
	def contract_DataType1Object() {
		testContract( // WIP - verify result
		'DataType1' -> '''
			type: object
			minProperties: 1
			properties:
			  id:
			    type: string
			    minLength: 1
			  prop1:
			    type: string
			    minLength: 1
			  ref1:
			    "$ref": "#/definitions/Nested1"
		''')
	}

	@Test
	def contract_Nested2() {
		testContract( // The same for CONTRACT and INTEROP
		'Nested2' -> '''
		type: "object"
		minProperties: 1
		properties:
		  nested2_prop1:
		    type: "string"
		    minLength: 1''')
	}

	@Test
	def contract_Nested1() {
		testContract(
			'Nested1' -> '''
			type: "object"
			minProperties: 1
			properties:
			  nested1_prop1:
			    type: "string"
			    minLength: 1
			  ref2:
			    $ref: "#/definitions/ReferencedResource_link"'''
		)
	}

	@Test
	def interop_DataType1() {
		testInterop( // WIP
		'DataType1' -> '''
			type: "object"
			minProperties: 1
			properties:
			  id:
			    type: "string"
			    minLength: 1
			  prop1:
			    type: "string"
			    minLength: 1
			  ref1:
			    $ref: "#/definitions/Nested1"
			  _links:
			    $ref: "#/definitions/_RapidLinksMap"
		''')
	}

	@Test
	def interop_Nested2() {
		testInterop(
			'Nested2' -> '''
				type: "object"
				minProperties: 1
				properties:
				  nested2_prop1:
				    type: "string"
				    minLength: 1
				  _links:
				    $ref: "#/definitions/_RapidLinksMap"
			  '''
		)
	}

}
