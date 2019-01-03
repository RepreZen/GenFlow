package com.reprezen.genflow.rapidml.jsonschema.xchange.realizations

import org.junit.Test

class InclusivePropertySet_ExcludeReference_Test extends RealizationTestBase {

	override rapid_model_relative_path() {
		"/M1/InclusivePropertySet/InclusivePropertySet_ExcludeReference.rapid"
	}

	@Test
	def contract_TaxFilingObject() {
		testContract(
			'TaxFiling_AllExceptReference',
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
				required: 
				  - filingID
				'''
		)

	}

}
