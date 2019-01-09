package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class MultiValuedReference_ExplicitLinkToCollection_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
			rapidModel TaxBlaster
				resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"
			
					/** The list of Tax Filings visible to the authorized user. */
					default collectionResource TaxFilingCollection type TaxFiling
						URI /taxFilings
						mediaTypes
							application/xml
						method GET getTaxFilingCollection
							request
							response TaxFilingCollection statusCode 200
						method POST updateTaxFilingCollection
							request TaxFilingCollection
							response statusCode 200
							response statusCode 400
			
					/** An individual Tax Filing record, accessed by its ID */
					objectResource TaxFilingObject type TaxFiling
						URI taxFilings/{id}
							/** filingID of the requested TaxFiling */
							required templateParam id property filingID
			
						mediaTypes
							application/xml
						method GET getTaxFiling
							request
							response TaxFilingObject statusCode 200
							response statusCode 404
			
					/** An individual user by ID. */
					objectResource PersonObject type Person
						URI people/{id}
							/** taxpayerID of the requested Person */
							required templateParam id property taxpayerID
						referenceLink > taxFilings
							targetResource TaxFilingCollection
						mediaTypes
							application/xml
						method GET getPersonObject
							request
							response PersonObject statusCode 200
			
						method PUT putPersonObject
							request PersonObject
							response statusCode 200
							response statusCode 400
			
				/** Supporting data types for the TaxBlaster API */
				dataModel TaxBlasterDataModel
					/** A tax filing record for a given user, in a given tax jurisdiction, in a 
					    specified tax year. */
					structure TaxFiling
						/** A unique, system-assigned identifier for the tax filing. */
						filingID : string
			//			/** Reference to the person who owns this filing. */
			//			taxpayer : reference to Person
						/** Country, province, state or local tax authority where this is being filed. */
						jurisdiction : string
						/** Tax year */
						year : gYear
						/** Period within the year, if any */
						period : int
						/** Currency code */
						currency : string
						/** Total income reported on tax filing. */
						grossIncome : decimal
						/** Net tax liability */
						taxLiability : decimal
			
					/** A TaxBlaster user. */
					structure Person
						/** A unique, system-assigned identifier for the user. */
						taxpayerID : string
						/** Legal family name. */
						lastName : string
						/** Legal first name. */
						firstName : string
						/** Names previously used **/
						otherNames : string*
						taxFilings : reference to TaxFiling*
			
		'''
	}

	@Test
	def contract_PersonObject() {
		testContract('PersonObject', '''
			type: object
			minProperties: 1
			description: An individual user by ID.
			properties:
			  taxpayerID:
			    description: A unique, system-assigned identifier for the user.
			    type: string
			    minLength: 1
			  lastName:
			    description: Legal family name.
			    type: string
			    minLength: 1
			  firstName:
			    description: Legal first name.
			    type: string
			    minLength: 1
			  otherNames:
			    description: Names previously used *
			    type: array
			    minItems: 1
			    items:
			      type: string
			      minLength: 1
			  taxFilings:
			    $ref: "#/definitions/TaxFilingCollection_link"''');
	}

	@Test
	def contract_TaxFilingCollection_link() {
		testContract('TaxFilingCollection_link', '''
		type: object
		minProperties: 1
		properties:
		  _links:
		    "$ref": "#/definitions/_RapidLinksMap"''');
	}

}
