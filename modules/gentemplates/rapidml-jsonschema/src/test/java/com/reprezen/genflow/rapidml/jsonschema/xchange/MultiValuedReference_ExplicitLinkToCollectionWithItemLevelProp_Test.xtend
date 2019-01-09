package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class MultiValuedReference_ExplicitLinkToCollectionWithItemLevelProp_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
		rapidModel TaxBlaster
			resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"
		
				default collectionResource TaxFilingCollection type TaxFiling
					URI /taxFilings
		
				objectResource PersonObject type Person
					URI people/{id}
						/** taxpayerID of the requested Person */
						required templateParam id property taxpayerID
					referenceLink > taxFilings
						targetResource TaxFilingCollection
						targetProperties
							filingID, currency
		
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
					taxFilings : reference to TaxFiling*'''
	}

	@Test
	def contract_PersonObject() {
		testContract('PersonObject', '''
type: object
minProperties: 1
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
    type: array
    minItems: 1
    items:
      type: object
      minProperties: 1
      properties:
        filingID:
          description: A unique, system-assigned identifier for the tax filing.
          type: string
          minLength: 1
        currency:
          description: Currency code
          type: string
          minLength: 1''');
	}

}
