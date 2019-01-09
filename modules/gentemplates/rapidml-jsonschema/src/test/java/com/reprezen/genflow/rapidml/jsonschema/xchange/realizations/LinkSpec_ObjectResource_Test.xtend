package com.reprezen.genflow.rapidml.jsonschema.xchange.realizations

import org.junit.Test

class LinkSpec_ObjectResource_Test extends RealizationTestBase {

	override rapid_model_relative_path() {
		"/M2/LinkSpec/LinkSpec_ObjectResource.rapid"
	}

	@Test
	def contract_FinancialStatementObject() {
		testContract(
			'FinancialStatement_Root',
			'''
				type: "object"
				minProperties: 1
				properties:
				  statementID:
				    type: "string"
				    minLength: 1
				    readOnly: true
				  company:
				    $ref: "#/definitions/Company_Ref"
				  balanceSheet:
				    $ref: "#/definitions/BalanceSheet_Ref"
				  incomeStatement:
				    $ref: "#/definitions/IncomeStatement_Ref"
				  cashFlowStatement:
				    $ref: "#/definitions/CashFlowStatement_Ref"
				required:
				- "statementID"
			'''
		)
	}

	@Test
	def contract_BalanceSheet_Ref() {
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
				  statement:
				    "$ref": "#/definitions/FinancialStatement_Ref"
				  accountingMethod:
				    "$ref": "#/definitions/AccountingStandard_Ref"
				  comments:
				    type: array
				    minItems: 1
				    items:
				      "$ref": "#/definitions/Comment_Ref"
				  _links:
				    "$ref": "#/definitions/_RapidLinksMap"
				required:
				- balanceSheetID
			'''
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
				  company:
				    "$ref": "#/definitions/Company_Ref"
				  balanceSheet:
				    "$ref": "#/definitions/BalanceSheet_Ref"
				  incomeStatement:
				    "$ref": "#/definitions/IncomeStatement_Ref"
				  cashFlowStatement:
				    "$ref": "#/definitions/CashFlowStatement_Ref"
				  _links:
				    "$ref": "#/definitions/_RapidLinksMap"
				required:
				- statementID
			'''
		)
	}

	@Test
	def contract_Company_Ref() {
		testContract(
			'Company_Ref',
			'''
				type: object
				minProperties: 1
				properties:
				  companyID:
				    type: string
				    minLength: 1
				    readOnly: true
				required:
				- companyID
			'''
		)
	}

	@Test
	def contract_AccountingStandard_Ref() {
		testContract(
			'AccountingStandard_Ref',
			'''
			type: object
			minProperties: 1
			properties:
			  accountingStandardID:
			    type: string
			    minLength: 1
			    readOnly: true
			required:
			- accountingStandardID'''
		)
	}

	@Test
	def contract_Comment_Ref() {
		testContract(
			'Comment_Ref',
			'''
			type: object
			minProperties: 1
			properties:
			  commentID:
			    type: string
			    minLength: 1
			    readOnly: true
			required:
			- commentID'''
		)
	}

	@Test
	def contract_CashFlowStatement_Ref() {
		testContract(
			'CashFlowStatement_Ref',
			'''
			type: object
			minProperties: 1
			properties:
			  cashFlowStatementID:
			    type: string
			    minLength: 1
			    readOnly: true
			  statement:
			    "$ref": "#/definitions/FinancialStatement_Ref"
			  accountingMethod:
			    "$ref": "#/definitions/AccountingStandard_Ref"
			  comments:
			    type: array
			    minItems: 1
			    items:
			      "$ref": "#/definitions/Comment_Ref"
			required:
			- cashFlowStatementID			'''
		)
	}

	@Test
	def contract_BalanceSheet_Root() {
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
			  accountingMethod:
			    "$ref": "#/definitions/AccountingStandard_Ref"
			  comments:
			    type: array
			    minItems: 1
			    items:
			      "$ref": "#/definitions/Comment_Ref"
			required:
			- balanceSheetID'''
		)
	}

}
