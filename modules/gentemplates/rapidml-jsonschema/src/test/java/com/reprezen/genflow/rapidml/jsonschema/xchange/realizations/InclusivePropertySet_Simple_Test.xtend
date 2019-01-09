package com.reprezen.genflow.rapidml.jsonschema.xchange.realizations

import org.junit.Test

class InclusivePropertySet_Simple_Test extends RealizationTestBase {

	override rapid_model_relative_path() {
		"/M1/InclusivePropertySet/InclusivePropertySet_Simple.rapid"
	}

	@Test
	def contract_TaxFilingObject() {
		testContract(
			'TaxFiling_AllProperties',
			'''
				type: object
				minProperties: 1
				description: An individual Tax Filing record, accessed by its ID
				properties:
				  filingID:
				    type: string
				    minLength: 1
				  jurisdiction:
				    type: string
				    minLength: 1
				  year:
				    type: string
				    minLength: 1
				  period:
				    type: integer
				  currency:
				    type: string
				    minLength: 1
				  grossIncome:
				    type: number
				  taxLiability:
				    type: number
				  taxpayer:
				    "$ref": "#/definitions/Person_AllProperties"
				required: 
				  - filingID
			'''
		)
	}

	@Test
	def contract_Person() {
		testContract(
			'Person_AllProperties',
			'''
				type: object
				minProperties: 1
				description: A TaxBlaster user.
				properties:
				  taxpayerID:
				    type: string
				    minLength: 1
				  lastName:
				    type: string
				    minLength: 1
				  firstName:
				    type: string
				    minLength: 1
				  otherNames:
				    type: array
				    minItems: 1
				    items:
				      type: string
				      minLength: 1
				required: 
				  - taxpayerID
			'''
		)
	}

}
