package com.reprezen.genflow.rapidml.jsonschema

import com.reprezen.genflow.rapidml.jsonschema.xchange.XChangeSchemaTestBase
import org.junit.Test

class ArrayReferenceTest extends XChangeSchemaTestBase {
	
	override String rapid_model() {
		'''
rapidModel TaxBlaster
	resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"

		objectResource PersonTaxFilingCollection type Person
			URI /people/{id}/taxFilings
			/* Subset of person properties included here. */
			only properties
				taxpayerID
				lastName
				firstName
			/* Tax filings data will be embedded within the Person representation. */
			referenceEmbed > taxFilings
				targetProperties
					currency 
					filingID
					grossIncome
					jurisdiction
					period
					status
					taxLiability
					year
			method GET getPersonTaxFilingCollection
				request
				response type Person statusCode 200
				// resource realization minus year
					referenceEmbed > taxFilings
						targetProperties
							currency 
							filingID
							grossIncome
							jurisdiction
							period
							status
							taxLiability

	/** Supporting data types for the TaxBlaster API */
	dataModel TaxBlasterDataModel
		/** A tax filing record for a given user, in a given tax jurisdiction, in a 
		    specified tax year. */
		structure TaxFiling
			/** A unique, system-assigned identifier for the tax filing. */
			filingID : string
			/** Reference to the person who owns this filing. */
			taxpayer : reference to Person inverse taxFilings
			/** Country, province, state or local tax authority where this is being filed. 
			    */
			jurisdiction : string
			/** Tax year */
			year : gYear
			/** Period within the year, if any */
			period : int
			/** Currency code */
			currency : CurrencyCodeEnum
			/** Total income reported on tax filing. */
			grossIncome : decimal
			/** Net tax liability */
			taxLiability : decimal
			/** Tax filing status */
			status : TaxFilingStatusEnum
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
			/** Net worth, if known */
			netWorth : decimal
			/** Net worth special value */
			netWorthSpecialValue : SpecialValueEnum
			/** Preferred language for communications with this person */
			preferredLanguage : string
			/** Date of birth */
			DOB : date
			/* Inverse reference to TaxFilings.  PersonObject will have 
			   a list of hyperlinks to TaxFilingObject, the inverse of the 
			   TaxFilingObject.taxpayer hyperlink. Note that the inverse 
			   keyword must also be specified on TaxFiling.taxpayer
			*/
			/** Tax filings on record for this individual */
			taxFilings : reference to TaxFiling* inverse taxpayer
			/** Postal addresses, zero or more */
			addresses : containing Address*

		/** Tax filing status enumeration, using default values */
		enum int TaxFilingStatusEnum
			DRAFT
			PENDING_CPA_REVIEW
			PENDING_CLIENT_REVIEW
			FILED
			AMENDED
			CLOSED

		/** Special Value Enum, using explicit integer values */
		enum int SpecialValueEnum
			NORMAL_VALUE : 0
			NOT_AVAILABLE : -65534
			NOT_APPLICABLE : -65533
			RESTRICTED : -65532

		/** Currency code enum, using explicit string values */
		enum string CurrencyCodeEnum
			EUR : "Euro"
			CAD : "Canadian Dollar"
			USD : "US Dollar"
			CHF : "Swiss Franc"
			JPY : "Japanese Yen"
			INR : "Indian Rupee"
			BRL : "Brazilian Real"
		/** Postal address */
		structure Address
			street1 : string
			street2 : string?
			city : string
			stateOrProvince : string
			postalCode : string
			country : string
'''
	}

	@Test
	def test_legacy_PersonTaxFilingCollection() {
		testLegacy('PersonTaxFilingCollection', '''
		type: object
		properties:
		  taxpayerID:
		    description: A unique, system-assigned identifier for the user.
		    type: string
		  lastName:
		    description: Legal family name.
		    type: string
		  firstName:
		    description: Legal first name.
		    type: string
		  taxFilings:
		    $ref: "#/definitions/PersonTaxFilingCollection_taxFilings"''')
	}

	@Test
	def test_legacy_PersonTaxFilingCollection_taxFilings() {
		testLegacy('PersonTaxFilingCollection_taxFilings', '''
		description: Tax filings on record for this individual
		type: array
		items:
		  "$ref": "#/definitions/PersonTaxFilingCollection_taxFilings_item"''')
	}

	@Test
	def test_legacy_PersonTaxFilingCollection_taxFilings_item() {
		testLegacy('PersonTaxFilingCollection_taxFilings_item', '''
		type: object
		properties:
		  currency:
		    description: Currency code
		    "$ref": "#/definitions/CurrencyCodeEnum"
		  filingID:
		    description: A unique, system-assigned identifier for the tax filing.
		    type: string
		  grossIncome:
		    description: Total income reported on tax filing.
		    type: number
		  jurisdiction:
		    description: Country, province, state or local tax authority where this
		      is being filed.
		    type: string
		  period:
		    description: Period within the year, if any
		    type: integer
		  status:
		    description: Tax filing status
		    "$ref": "#/definitions/TaxFilingStatusEnum"
		  taxLiability:
		    description: Net tax liability
		    type: number
		  year:
		    description: Tax year
		    type: string''')
	}

	@Test
	def test_legacy_methodRealization() {
		val addressesPropName = "Address"
		testLegacy('PersonTaxFilingCollection_getPersonTaxFilingCollection_response200', '''
		type: object
		properties:
		  taxpayerID:
		    description: A unique, system-assigned identifier for the user.
		    type: string
		  lastName:
		    description: Legal family name.
		    type: string
		  firstName:
		    description: Legal first name.
		    type: string
		  otherNames:
		    description: Names previously used *
		    type: array
		    items:
		      type: string
		  netWorth:
		    description: Net worth, if known
		    type: number
		  netWorthSpecialValue:
		    description: Net worth special value
		    "$ref": "#/definitions/SpecialValueEnum"
		  preferredLanguage:
		    description: Preferred language for communications with this person
		    type: string
		  DOB:
		    description: Date of birth
		    type: string
		    format: date
		  taxFilings:
		    "$ref": "#/definitions/PersonTaxFilingCollection_getPersonTaxFilingCollection_response200_taxFilings"
		  addresses:
		    description: Postal addresses, zero or more
		    type: array
		    items:
		      "$ref": "#/definitions/Address"
		    ''')

	}

	@Test
	def test_legacy_methodRealization_multivalued_referenceEmbed() {
		testLegacy('PersonTaxFilingCollection_getPersonTaxFilingCollection_response200_taxFilings', '''
		description: Tax filings on record for this individual
		type: array
		items:
		  "$ref": "#/definitions/PersonTaxFilingCollection_getPersonTaxFilingCollection_response200_taxFilings_item"''')
	}

	@Test
	def test_legacy_methodRealization_multivalued_referenceEmbed_item() {
		testLegacy('PersonTaxFilingCollection_getPersonTaxFilingCollection_response200_taxFilings_item', '''
		type: object
		properties:
		  currency:
		    description: Currency code
		    "$ref": "#/definitions/CurrencyCodeEnum"
		  filingID:
		    description: A unique, system-assigned identifier for the tax filing.
		    type: string
		  grossIncome:
		    description: Total income reported on tax filing.
		    type: number
		  jurisdiction:
		    description: Country, province, state or local tax authority where this
		      is being filed.
		    type: string
		  period:
		    description: Period within the year, if any
		    type: integer
		  status:
		    description: Tax filing status
		    "$ref": "#/definitions/TaxFilingStatusEnum"
		  taxLiability:
		    description: Net tax liability
		    type: number''')
	}

}
