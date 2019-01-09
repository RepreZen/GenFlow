package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class EmptyValuesDisallowed_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
rapidModel TaxBlaster
	resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"

		objectResource CPAObject type CPA
			URI /uri

		// resource created to ensure that a definition for the underlying structure is generated
		objectResource Temp type StructureLengthConstraints
			URI /uri

	dataModel TaxBlasterDataModel
	
		structure CPA
			employeeID: string
			certificationID: string
			certExpiry: date
			assignedAccounts: reference to Person*
			activeTaxFilings: reference to TaxFiling+
		
		structure Person
		
		structure TaxFiling

		structure StructureLengthConstraints
			stringProp_0_MinLenth: string
				length from 0 to 10
			stringProp_10_MinLenth: string
				length from 10
'''
	}

	@Test
	def contract_CPA() {
		testContract('CPA', '''
		type: object
		minProperties: 1
		properties:
		  employeeID:
		    type: string
		    minLength: 1
		  certificationID:
		    type: string
		    minLength: 1
		  certExpiry:
		    type: string
		    format: date 
		    minLength: 1
		  assignedAccounts:
		    type: array
		    minItems: 1
		    items:
		      $ref: "#/definitions/Person"
		  activeTaxFilings:
		    type: array
		    minItems: 1
		    items:
		      $ref: "#/definitions/TaxFiling"
		required:
		  - activeTaxFilings''')
	}

	@Test
	def interop_CPA() {
		testInterop('CPA', '''
			type: object
			minProperties: 1
			properties:
			  employeeID:
			    type: string
			    minLength: 1
			  certificationID:
			    type: string
			    minLength: 1
			  certExpiry:
			    type: string
			    format: date 
			    minLength: 1
			  assignedAccounts:
			    type: array
			    minItems: 1
			    items:
			      $ref: "#/definitions/Person"
			  activeTaxFilings:
			    type: array
			    minItems: 1
			    items:
			      $ref: "#/definitions/TaxFiling"
			  _links:
			    $ref: "#/definitions/_RapidLinksMap"
		''')
	}

	@Test
	def contract_EmptyStructure() {
		testContract('Person', '''
			type: object
			minProperties: 1
			properties: {}
		''')
	}

	@Test
	def interop_EmptyStructure() {
		testInterop('Person', '''
			type: object
			minProperties: 1
			properties:
			  _links:
			    $ref: "#/definitions/_RapidLinksMap"
		''')
	}
	
	@Test
	def contract_StructureLengthConstraints() {
		testContract('StructureLengthConstraints', '''
			type: object
			minProperties: 1
			properties:
			  stringProp_0_MinLenth:
			    type: string
			    minLength: 0
			    maxLength: 10
			  stringProp_10_MinLenth:
			    type: string
			    minLength: 10

		''')
	}

	@Test
	def interop_StructureLengthConstraints() {
		testInterop('StructureLengthConstraints', '''
			type: object
			minProperties: 1
			properties:
			  stringProp_0_MinLenth:
			    type: string
			    minLength: 0
			    maxLength: 10
			  stringProp_10_MinLenth:
			    type: string
			    minLength: 10
			  _links:
			    $ref: "#/definitions/_RapidLinksMap"
		''')
	}
	

}
