package com.reprezen.genflow.rapidml.jsonschema.xchange.realizations

import org.junit.Test

class InclusivePropertySet_ExcludePrimitive_Test extends RealizationTestBase {

	override rapid_model_relative_path() {
		"/M1/InclusivePropertySet/InclusivePropertySet_ExcludePrimitive.rapid"
	}

	@Test
	def contract_TaxFilingObject() {
		testContract(
			'TaxFiling_AllExceptPrimitive',
			'''
				type: object
				minProperties: 1
				description: An individual Tax Filing record, accessed by its ID
				properties:
				  taxpayer:
				    "$ref": "#/definitions/Person_AllExceptPrimitive"
			'''
		)
	}

	@Test
	def contract_Person() {
		// description is taken from the documentation of the Person data type
		testContract(
			'Person_AllExceptPrimitive',
			'''
				type: object
				minProperties: 1
				description: "A TaxBlaster user."
				properties: {}
				'''
		)
	}

}
