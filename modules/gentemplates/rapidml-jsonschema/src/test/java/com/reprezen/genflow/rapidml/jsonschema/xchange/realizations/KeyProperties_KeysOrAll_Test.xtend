package com.reprezen.genflow.rapidml.jsonschema.xchange.realizations

import org.junit.Test

class KeyProperties_KeysOrAll_Test extends RealizationTestBase {

	override rapid_model_relative_path() {
		"/M1/KeyProperties/KeyProperties_KeysOrAll.rapid"
	}

	@Test
	def contract_Accountant_Root() {
		// Root: include all
//			employeeID : key string
//			lastName : string
//			firstName : string
//			officeAddress : as containing reference to Address
//			clients : reference to Person*
		testContract(
			'Accountant_Root',
			'''
				type: object
				minProperties: 1
				description: An individual Tax Filing record, accessed by its ID
				properties:
				  employeeID:
				    type: string
				    minLength: 1
				  lastName:
				    type: string
				    minLength: 1
				  firstName:
				    type: string
				    minLength: 1
				  officeAddress:
				    "$ref": "#/definitions/Address_Ref"
				  clients:
				    type: array
				    minItems: 1
				    items:
				      "$ref": "#/definitions/Person_Ref"
				required: 
				  - employeeID
				
			'''
		)
	}

	@Test
	def contract_Address() {
		// Containing ref, NO keys: Include all properties
//		structure Address
//			addressLine1 : string
//			addressLine2 : string
//			city : string
//			stateOrProvince : string
//			postalCode : string
//			country : string
//			attentionLine: string		
		testContract(
			'Address_Ref',
			'''
				type: object
				minProperties: 1
				properties:
				  addressLine1:
				    type: string
				    minLength: 1
				  addressLine2:
				    type: string
				    minLength: 1
				  city:
				    type: string
				    minLength: 1
				  stateOrProvince:
				    type: string
				    minLength: 1
				  postalCode:
				    type: string
				    minLength: 1
				  country:
				    type: string
				    minLength: 1
				  attentionLine:
				    type: string
				    minLength: 1
			'''
		)
	}

	@Test
	def contract_Person() {
		// non-containment reference, HAS keys: Include only key properties.
//		structure Person
//			taxpayerID : key string
//			lastName : string
//			firstName : string
//			otherNames : string*		
		testContract(
			'Person_Ref',
			'''
				type: object
				minProperties: 1
				description: A TaxBlaster user.
				properties:
				  taxpayerID:
				    type: string
				    minLength: 1
				required: 
				  - taxpayerID
			'''
		)
	}

}
