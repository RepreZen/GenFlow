package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class ReferenceLink_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
rapidModel Realizations
	resourceAPI RealizationsAPI baseURI "http://my-namespace.com"

		objectResource DataType1Object type DataType1
			URI /uri/{id}
			referenceLink >ref1
				targetResource ReferencedResource
				targetProperties
					prop1
		
		objectResource ReferencedResource type DataType2
			URI /uri2

	dataModel RealizationsDataModel
		structure DataType1
			id : string
			prop2: string
			ref1: reference to DataType2
			
		structure DataType2
			prop1: string
			prop2: string
'''
	}

	@Test
	def contract_DataType1Object() {
		testContract(
			'DataType1Object' -> '''
			type: "object"
			minProperties: 1
			properties:
			  id:
			    type: "string"
			    minLength: 1
			  prop2:
			    type: "string"
			    minLength: 1
			  ref1:
			    type: "object"
			    minProperties: 1
			    properties:
			       _links:
			          $ref: "#/definitions/_RapidLinksMap"
			       prop1:
			          type: "string"
			          minLength: 1'''
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
			  prop2:
			    type: "string"
			    minLength: 1
			  ref1:
			    $ref: "#/definitions/ReferencedResource_link"
			  _links:
			    $ref: "#/definitions/_RapidLinksMap"'''
		)

	}

}
