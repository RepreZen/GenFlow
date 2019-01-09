package com.reprezen.genflow.rapidml.jsonschema.xchange.realizations

import org.junit.Test

class LinkSpec_IncludeAutoLinkableToOR_Test extends RealizationTestBase {

	override rapid_model_relative_path() {
		"/M2/LinkSpec/LinkSpec_IncludeAutoLinkableToOR.rapid"
	}

	@Test
	def contract_BalanceSheet_Root() {
		// Root objects will have key properties, and _only_ references to data types that 
		// have an auto-link objectResource.
		testContract(
			'BalanceSheet_Root',
			'''
			type: object
			minProperties: 1
			properties:
			  balanceSheetID:
			    type: string
			    minLength: 1
			    readOnly: true
			  statement:
			    "$ref": "#/definitions/FinancialStatement_Ref"
			required:
			- balanceSheetID'''
		)
	}

	@Test
	def contract_BalanceSheet_Ref() {
		// Reference objects will have only key properties, and will have objectResource auto-links.
		// no reference properties
		testContract(
			'BalanceSheet_Ref',
			'''
			type: object
			minProperties: 1
			properties:
			  balanceSheetID:
			    type: string
			    minLength: 1
			    readOnly: true
			  _links:
			    "$ref": "#/definitions/_RapidLinksMap"
			required:
			- balanceSheetID'''
		)
	}

	@Test
	def contract_FinancialStatement_Root() {
		testContract(
			'FinancialStatement_Root',
			'''
			type: object
			minProperties: 1
			properties:
			  statementID:
			    type: string
			    minLength: 1
			    readOnly: true
			  balanceSheet:
			    "$ref": "#/definitions/BalanceSheet_Ref"
			required:
			- statementID'''
		)
	}

	@Test
	def contract_FinancialStatement_Ref() {
		testContract(
			'FinancialStatement_Ref',
			'''
			type: object
			minProperties: 1
			properties:
			  statementID:
			    type: string
			    minLength: 1
			    readOnly: true
			  _links:
			    "$ref": "#/definitions/_RapidLinksMap"
			required:
			- statementID'''
		)
	}

}
