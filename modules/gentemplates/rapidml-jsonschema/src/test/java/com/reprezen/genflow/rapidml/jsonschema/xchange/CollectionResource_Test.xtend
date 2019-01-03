package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class CollectionResource_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		// the standard TaxBlaster
		'''
			rapidModel TaxBlaster
				resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"
			
					/** The Index Resource is the entry point to the TaxBlaster API. To minimize 
					    coupling, consuming applications should start here, and follow the links to 
					    related resources. */
					objectResource IndexObject type Index
						URI index
						mediaTypes
							application/xml
						method GET getIndex
							response IndexObject statusCode 200
							response statusCode 404
			
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
			
					/** The list of TaxBlaster users.  The results will vary in membership and level 
					    of detail, depending on your access privileges. */
					default collectionResource PersonCollection type Person
						URI /people
						mediaTypes
							application/xml
						method GET getPersonCollection
							request
							response PersonCollection statusCode 200
						method POST updatePersonCollection
							request PersonCollection
							response statusCode 200
							response statusCode 400
			
					/** An individual Tax Filing record, accessed by its ID */
					objectResource TaxFilingObject type TaxFiling
						URI taxFilings/{id}
							/** filingID of the requested TaxFiling */
							required templateParam id property filingID
			
						referenceLink > taxpayer
							targetResource PersonObject 
							targetProperties
								taxpayerID
								firstName
			
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
						/** Reference to the person who owns this filing. */
						taxpayer : reference to Person
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
			
					/** The supporting data type for the Index resource.  Not meaningful as a 
					    business entity, but required to support a single point of entry. */
					structure Index
						people : reference to Person*
						taxFilings : reference to TaxFiling*
		'''
	}

	//@Test
	def interop_TaxFilingCollection() {
		testInterop('TaxFilingCollection', null);
	}

	@Test
	def interop_TaxFilingCollection_Item() {
		testInterop('TaxFilingCollection_Item', null);
	}

	@Test
	def interop_PersonObject_link() {
		testInterop('PersonObject_link', '''
type: object
minProperties: 1
properties:
  _links:
    "$ref": "#/definitions/_RapidLinksMap"''');
	}

	@Test
	def contract_TaxFilingCollection() {
		testContract('TaxFilingCollection', '''
type: array
items:
  "$ref": "#/definitions/TaxFilingObject_link"''');
	}

	@Test
	def contract_TaxFilingObject_link() {
		testContract('TaxFilingObject_link', '''
type: object
minProperties: 1
properties:
  _links:
    "$ref": "#/definitions/_RapidLinksMap"''');
	}

	@Test
	def contract_TaxFiling() {
		testContract('TaxFilingObject', '''
type: object
minProperties: 1
description: An individual Tax Filing record, accessed by its ID
properties:
  filingID:
    description: A unique, system-assigned identifier for the tax filing.
    type: string
    minLength: 1
  jurisdiction:
    description: Country, province, state or local tax authority where this is being
      filed.
    type: string
    minLength: 1
  year:
    description: Tax year
    type: string
    minLength: 1
  period:
    description: Period within the year, if any
    type: integer
  currency:
    description: Currency code
    type: string
    minLength: 1
  grossIncome:
    description: Total income reported on tax filing.
    type: number
  taxLiability:
    description: Net tax liability
    type: number
  taxpayer:
    description: Reference to the person who owns this filing.
    type: object
    minProperties: 1
    properties:
      _links:
        "$ref": "#/definitions/_RapidLinksMap"
      taxpayerID:
        description: A unique, system-assigned identifier for the user.
        type: string
        minLength: 1
      firstName:
        description: Legal first name.
        type: string
        minLength: 1''');
	}
	
	@Test
	def void contract_PersonObject_link() {
		// PersonObject_link does not have its own schema, it's inlined in TaxFilingObject
	}

}
