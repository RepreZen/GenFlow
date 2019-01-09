package com.reprezen.genflow.rapidml.jsonschema.xchange.realizations

import org.junit.Test

class ResourceType_CollectionResourceRefsOnly_Test extends RealizationTestBase {

	override rapid_model_relative_path() {
		"/M1/ResourceType/ResourceType_CollectionResourceRefsOnly.rapid"
	}

// RootCollections: namingPattern: ${TypeName}_RootColll; include only references
	@Test
	def contract_BalanceSheetCollection() {
		testContract(
			'BalanceSheet_RootColl',
			'''
				type: array
				items:
				  "$ref": "#/definitions/BalanceSheet_RootColl_item"
			'''
		)

//		structure BalanceSheet
//			balanceSheetID : readOnly key string!
//			statement : as container reference to FinancialStatement inverse balanceSheet
//			accountingMethod : as reference to AccountingStandard
//			assets : as decimal
//			liabilities : as decimal
//			equity : as decimal
//			comments : as containing reference to Comment*
			
		testContract(
			'BalanceSheet_RootColl_item',
			'''
			type: object
			minProperties: 1
			properties:
			  statement:
			    "$ref": "#/definitions/FinancialStatement_Ref"
			  accountingMethod:
			    "$ref": "#/definitions/AccountingStandard_Ref"
			  comments:
			    type: array
			    minItems: 1
			    items:
			      "$ref": "#/definitions/Comment_Ref"'''
		)
	}

// RefObjects: include only key properties
	@Test
	def contract_FinancialStatement_Ref() {
//		structure FinancialStatement
//			statementID : readOnly key string!
//			company : as reference to Company
//			statementDate : date
//			beginDate : date
//			endDate : date
//			fiscalYear : gYear
//			balanceSheet : as containing reference to BalanceSheet inverse statement
//			incomeStatement : as containing reference to IncomeStatement inverse statement
//			cashFlowStatement : as containing reference to CashFlowStatement inverse statement
		
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
				required:
				- statementID
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
				- accountingStandardID
			'''
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
				- commentID
			'''
		)
	}

// RootObjects: include all
	@Test
	def contract_BalanceSheet_RootObj() {
		testContract(
			'BalanceSheet_RootObj',
			'''
				type: object
				minProperties: 1
				description: An individual Tax Filing record, accessed by its ID
				properties:
				  balanceSheetID:
				    type: string
				    minLength: 1
				    readOnly: true
				  assets:
				    type: number
				  liabilities:
				    type: number
				  equity:
				    type: number
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
				- balanceSheetID
			'''
		)
	}

}
