package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class LinkToCollectionResourceTest extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
			rapidModel TaxBlaster
				resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"
			
					objectResource PersonObject type Person
						URI people/{id}
							required templateParam id property taxpayerID
			
						method GET getPersonObject
							request
							response PersonObject statusCode 200
			
					default collectionResource TaxFilingCollection type TaxFiling
						URI /taxFilings
						method GET getTaxFilingCollection
							request
							response TaxFilingCollection statusCode 200
			
					objectResource TaxFilingObject type TaxFiling
						URI taxFilings/{id}
							required templateParam id property filingID
			
						default linkDescriptor TaxFilingLink
							filingID
			
						linkDescriptor FancyTaxFilingLink
							filingID
							period
							jurisdiction
			
						method GET getTaxFiling
							request
							response TaxFilingObject statusCode 200
							response statusCode 404
							
					collectionResource AddressCollection type Address
						URI /addresses
						method GET getAddressCollection
							request
							response AddressCollection statusCode 200
			
					objectResource AddressObject type Address
						URI addresses/{id}
							required templateParam id property addressID
						method GET getPerson
							request
							response PersonObject statusCode 200
							response statusCode 400
			
				dataModel TaxBlasterDataModel
			
					structure Person
						taxpayerID : string
						lastName : string
						firstName : string
						otherNames : string*
						taxFilings : as reference to TaxFiling*
						/* Uncomment the following line to generate TaxFilingObject_link_TaxFilingLink  */
						// currentYearTaxFiling : as reference to TaxFiling
						addresses : as reference to Address*
						/* Uncomment the following line to generate AddressObject_link  */
						// currentAddress : as reference to Address
						
			
					structure TaxFiling
						filingID : string
						jurisdiction : string
						year : gYear
						period : int
						currency : string
						grossIncome : decimal
						taxLiability : decimal
			
					structure Address
						addressID : string
						addressLine1 : string
						addressLine2 : string
						addressLine3 : string
		'''
	}

	@Test
	def contract_AddressCollection() {
		testContract('AddressCollection', '''
		type: "array"
		items:
		  $ref: "#/definitions/AddressObject_link"''');
	}

	@Test
	def contract_AddressObject_link() {
		testContract('AddressObject_link', '''
		type: "object"
		minProperties: 1
		properties:
		  _links:
		    $ref: "#/definitions/_RapidLinksMap"''');
	}

	@Test
	def contract_TaxFilingObject_link_TaxFilingLink() {
		testContract('TaxFilingLink', '''
		type: "object"
		minProperties: 1
		properties:
		  _links:
		    $ref: "#/definitions/_RapidLinksMap"
		  filingID:
		    type: "string"
		    minLength: 1''');
	}

	@Test
	def contract_TaxFilingObject_TaxFilingCollection() {
		testContract('TaxFilingCollection', '''
		type: "array"
		items:
		  $ref: "#/definitions/TaxFilingLink"''');
	}

}
