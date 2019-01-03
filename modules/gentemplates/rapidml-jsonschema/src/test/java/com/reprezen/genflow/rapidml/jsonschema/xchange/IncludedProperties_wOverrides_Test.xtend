package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class IncludedProperties_wOverrides_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
rapidModel Realizations
	resourceAPI RealizationsAPI baseURI "http://my-namespace.com"

		objectResource DataType1Object type DataType1
			URI /uri
			with all properties
			including
				prop2!
			excluding
				id
		
	dataModel RealizationsDataModel
		structure DataType1
			id : string
			prop1: string
			prop2: string
'''
	}

	@Test
	def contract_DataType1Object() {
		testContract(
			// id is excluded, there is not prop for it in contract schema
			// prop2 is required
			'DataType1Object' -> '''
			type: "object"
			minProperties: 1
			properties:
			  prop1:
			    type: "string"
			    minLength: 1
			  prop2:
			    type: "string"
			    minLength: 1
			required:
			 - "prop2"'''
		)

	}

	@Test
	def interop_DataType1() {
		testInterop(
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
			  prop2:
			    type: "string"
			    minLength: 1
			  _links:
			    $ref: "#/definitions/_RapidLinksMap"
'''
		)

	}

}
