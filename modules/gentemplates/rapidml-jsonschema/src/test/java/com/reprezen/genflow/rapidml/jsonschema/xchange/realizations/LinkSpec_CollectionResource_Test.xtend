package com.reprezen.genflow.rapidml.jsonschema.xchange.realizations

import org.junit.Test

class LinkSpec_CollectionResource_Test extends RealizationTestBase {

	override rapid_model_relative_path() {
		"/M2/LinkSpec/LinkSpec_CollectionResource.rapid"
	}

	@Test
	def contract_IndexObject() {
		// Only include multi-valued references that are _not_ auto-linkable to a collectionResource.
		// => no TaxFilings
		testContract(
			'Index_Root',
			'''
				type: object
				minProperties: 1
				description: "The supporting data type for the Index resource.  Not meaningful
				  as a \nbusiness entity, but required to support a single point of entry."
				properties:
				  people:
				    type: array
				    minItems: 1
				    items:
				      "$ref": "#/definitions/Person_Ref"
				  accountants:
				    type: array
				    minItems: 1
				    items:
				      "$ref": "#/definitions/Accountant_Ref"
				  balanceSheets:
				    type: array
				    minItems: 1
				    items:
				      "$ref": "#/definitions/BalanceSheet_Ref"
			'''
		)
	}

	// collectionResource Root Objects (CommentCollection, TaxFilingCollection) have
	// key properties
	@Test
	def contract_TaxFilingCollection() {
		testContract(
			'TaxFiling_Root',
			'''
			type: array
			items:
			  "$ref": "#/definitions/TaxFiling_Root_item"'''
		)
		testContract(
			'TaxFiling_Root_item',
			'''
			type: object
			minProperties: 1
			properties:
			  filingID:
			    type: string
			    minLength: 1
			  year:
			    type: string
			    minLength: 1
			  period:
			    type: integer
			required:
			- filingID'''
		)
	}

	@Test
	def contract_CommentCollection() {
		testContract(
			'Comment_Root',
			'''
			type: array
			items:
			  "$ref": "#/definitions/Comment_Root_item"'''
		)
		testContract(
			'Comment_Root_item',
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
	def contract_BalanceSheet_Ref() {
		// This is a MultiRefNotLinkableToCR, Only include multi-valued references that are auto-linkable to a CR
		// Should NOT include balanceSheetID
//      L balanceSheet (type BalanceSheet_Ref)
//        L balanceSheetID
//        L comments (type Comment_Ref)
//          L commentID
//          L Link (details --> CommentCollection)		
		testContract(
			'BalanceSheet_Ref',
			'''
				type: object
				minProperties: 1
				properties:
				  comments:
				    type: array
				    minItems: 1
				    items:
				      "$ref": "#/definitions/Comment_Ref"
				'''
		)
	}

	@Test
	def contract_Person_Ref() {
		// This is a MultiRefNotLinkableToCR, Only include multi-valued references that are auto-linkable to a CR
// DO NOT include taxpayerID
//      L people (type Person_Ref)
//        L taxpayerID
		testContract(
			'Person_Ref',
			'''
				type: object
				minProperties: 1
				description: A TaxBlaster user.
				properties: {}
			'''
		)
	}

	@Test
	def contract_Accountant_Ref() {
		// This is a MultiRefNotLinkableToCR, Only include multi-valued references that are auto-linkable to a CR
		testContract(
			'Accountant_Ref',
			'''
			type: object
			minProperties: 1
			properties: {}'''
		)
	}

	@Test
	def contract_Comment_Ref() {
		// This is a MultiRefLinkableToCR: Only include key properties.
//        L comments (type Comment_Ref)
//          L commentID
//          L Link (details --> CommentCollection)
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
	
	// reference officeAddress: NoRealizationException

}
