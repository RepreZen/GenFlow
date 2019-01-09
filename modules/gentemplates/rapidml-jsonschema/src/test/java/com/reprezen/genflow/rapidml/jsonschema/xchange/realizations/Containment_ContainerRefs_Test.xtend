package com.reprezen.genflow.rapidml.jsonschema.xchange.realizations

import org.junit.Test

class Containment_ContainerRefs_Test extends RealizationTestBase {

	override rapid_model_relative_path() {
		"/M1/Containment/Containment_ContainerRefs.rapid"
	}

	@Test
	def contract_FinancialStatementObject() {
		// Root: InclusivePropertySet
//		structure FinancialStatement
//			statementID : readOnly key string
//			company : as reference to Company
//			statementDate : date
//			beginDate : date
//			endDate : date
//			fiscalYear : gYear
//			balanceSheet : as containing reference to BalanceSheet inverse statement
//			incomeStatement : as containing reference to IncomeStatement inverse statement
//			cashFlowStatement : as containing reference to CashFlowStatement inverse statement
		testContract(
			'FinancialStatement_Root',
			'''
				type: object
				minProperties: 1
				description: An individual Tax Filing record, accessed by its ID
				properties:
				  statementID:
				    type: string
				    minLength: 1
				    readOnly: true
				  statementDate:
				    type: string
				    format: date
				    minLength: 1
				  beginDate:
				    type: string
				    format: date
				    minLength: 1
				  endDate:
				    type: string
				    format: date
				    minLength: 1
				  fiscalYear:
				    type: string
				    minLength: 1
				  company:
				    "$ref": "#/definitions/Company_NonContainmentRef"
				  balanceSheet:
				    "$ref": "#/definitions/BalanceSheet_ContainingRef"
				  incomeStatement:
				    "$ref": "#/definitions/IncomeStatement_ContainingRef"
				  cashFlowStatement:
				    "$ref": "#/definitions/CashFlowStatement_ContainingRef"
				required: 
				  - statementID
			'''
		)
	}

	@Test
	def contract_FinancialStatementObject_link() {
		testContract(
			'FinancialStatement_ContainerRef',
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

	// CONTAINING REFS: InclusivePropertySet, include all
	@Test
	def contract_BalanceSheet_ContainingRef() {
		testContract(
			'BalanceSheet_ContainingRef',
			'''
				type: object
				minProperties: 1
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
				    "$ref": "#/definitions/FinancialStatement_ContainerRef"
				  accountingMethod:
				    "$ref": "#/definitions/AccountingStandard_NonContainmentRef"
				  comments:
				    type: array
				    minItems: 1
				    items:
				      "$ref": "#/definitions/Comment_ContainingRef"
				required: 
				  - balanceSheetID
			'''
		)
	}

	@Test
	def contract_Comment_ContainingRef() {
		testContract(
			'Comment_ContainingRef',
			'''
				type: object
				minProperties: 1
				properties:
				  commentID:
				    type: string
				    minLength: 1
				    readOnly: true
				  timestamp:
				    type: string
				    format: date-time
				    minLength: 1
				  comment:
				    type: string
				    minLength: 1
				required: 
				  - commentID
			'''
		)
	}

	@Test
	def contract_IncomeStatement_ContainingRef() {
		testContract(
			'IncomeStatement_ContainingRef',
			'''
				type: object
				minProperties: 1
				properties:
				  incomeStatementID:
				    type: string
				    minLength: 1
				    readOnly: true
				  income:
				    type: number
				  expenses:
				    type: number
				  netIncome:
				    type: number
				  statement:
				    "$ref": "#/definitions/FinancialStatement_ContainerRef"
				  accountingMethod:
				    "$ref": "#/definitions/AccountingStandard_NonContainmentRef"
				  comments:
				    type: array
				    minItems: 1
				    items:
				      "$ref": "#/definitions/Comment_ContainingRef"
				required: 
				  - incomeStatementID
			'''
		)
	}

	@Test
	def contract_CashFlowStatement_ContainingRef() {
		testContract(
			'CashFlowStatement_ContainingRef',
			'''
				type: object
				minProperties: 1
				properties:
				  cashFlowStatementID:
				    type: string
				    minLength: 1
				    readOnly: true
				  startingCashPosition:
				    type: number
				  endingCashPosition:
				    type: number
				  statement:
				    "$ref": "#/definitions/FinancialStatement_ContainerRef"
				  accountingMethod:
				    "$ref": "#/definitions/AccountingStandard_NonContainmentRef"
				  comments:
				    type: array
				    minItems: 1
				    items:
				      "$ref": "#/definitions/Comment_ContainingRef"
				required: 
				  - cashFlowStatementID
			'''
		)
	}

	/* NON CONTAINMENT REFS : ExclusivePropertySet
	 This is an empty-object realization. No properties included. */
	// Note: The API  has only one resource - for FinancialStatementObject, no other resources. 
	// Because there are no resources for the referenced elements, I generate reference embeds for these references. But the auto-realization rules says that it should be empty. So, we are getting weird empty objects
	@Test
	def contract_Company_NonContainmentRef() {
		testContract(
			'Company_NonContainmentRef',
			'''
				type: object
				minProperties: 1
				properties: {}
			'''
		)
	}

	@Test
	def contract_AccountingStandard_NonContainmentRef() {
		testContract(
			'AccountingStandard_NonContainmentRef',
			'''
				type: object
				minProperties: 1
				properties: {}
			'''
		)
	}

}
