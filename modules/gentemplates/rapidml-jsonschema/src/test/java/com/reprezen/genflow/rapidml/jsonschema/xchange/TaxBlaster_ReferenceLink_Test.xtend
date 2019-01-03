package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class TaxBlaster_ReferenceLink_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		// Simplified and modified TaxFiling example
		'''
rapidModel TaxBlaster
	resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"

		objectResource TaxFilingObject type TaxFiling
			URI taxFilings/{id}
				/** filingID of the requested TaxFiling */
				required templateParam id property filingID

			referenceLink > taxpayer
				targetResource PersonObject 
				targetProperties
					taxpayerID
					firstName

		objectResource TaxFilingObject2 type TaxFiling
			URI taxFilings/{id}
				/** filingID of the requested TaxFiling */
				required templateParam id property filingID

			referenceEmbed > taxpayer
				targetProperties
					taxpayerID
					firstName

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
'''
	}

	@Test
	def contract_TaxFilingObjectWithReferenceLink() {
		testContract(
			'TaxFilingObject',
			'''
    type: "object"
    minProperties: 1
    properties:
      filingID:
        description: "A unique, system-assigned identifier for the tax filing."
        type: "string"
        minLength: 1
      jurisdiction:
        description: "Country, province, state or local tax authority where this is\
          \ being filed."
        type: "string"
        minLength: 1
      year:
        description: "Tax year"
        type: "string"
        minLength: 1
      period:
        description: "Period within the year, if any"
        type: "integer"
      currency:
        description: "Currency code"
        type: "string"
        minLength: 1
      grossIncome:
        description: "Total income reported on tax filing."
        type: "number"
      taxLiability:
        description: "Net tax liability"
        type: "number"
      taxpayer:
        description: "Reference to the person who owns this filing."
        type: "object"
        minProperties: 1
        properties:
          _links:
            $ref: "#/definitions/_RapidLinksMap"
          taxpayerID:
            description: "A unique, system-assigned identifier for the user."
            type: "string"
            minLength: 1
          firstName:
            description: "Legal first name."
            type: "string"
            minLength: 1
			'''
		)

	}

	@Test
	def interop_TaxFiling() {
		testInterop('TaxFiling', '''
		type: object
		minProperties: 1

		description: "A tax filing record for a given user, in a given tax jurisdiction, in a\nspecified tax year."

		# interop schema doesn't require any properties
		# because any property could be excluded from some realization

		properties:
		  filingID:
		    description: "A unique, system-assigned identifier for the tax filing."
		    type: "string"
		    minLength: 1
		  taxpayer:
		    description: Reference to the person who owns this filing.
		    $ref: "#/definitions/PersonObject_link"
		  jurisdiction:
		    description: "Country, province, state or local tax authority where this is\
		      \ being filed."
		    type: "string"
		    minLength: 1
		  year:
		    description: "Tax year"
		    type: "string"
		    minLength: 1
		  period:
		    description: "Period within the year, if any"
		    type: "integer"
		  currency:
		    description: "Currency code"
		    type: "string"
		    minLength: 1
		  grossIncome:
		    description: "Total income reported on tax filing."
		    type: "number"
		  taxLiability:
		    description: "Net tax liability"
		    type: "number"
		  _links:
		    $ref: "#/definitions/_RapidLinksMap"
		  ''')
	}

	@Test
	def interop_LinksMap() {
		testInterop('_RapidLinksMap' -> LinksMap)
		testInterop('_RapidLink' -> LinkObject)
	}

	val LinksMap = '''
		type: object
		minProperties: 1
		description: |
		  A set of hyperlinks from a domain object representation to related resources.
		  Each property maps a [link relation](https://www.iana.org/assignments/link-relations/link-relations.xhtml)
		  (the map key or property name) to a hyperlink object (the value).
		readOnly: true
		additionalProperties:
		  "$ref": "#/definitions/_RapidLink"
	'''
	
	val LinkObject = '''
		  type: object
		  description: |
		    An object representing a hyperlink to a related resource.
		    The link relation is specified as the containing property name.
		    The `href` property specifies the target URL, and additional properties may specify other metadata.
		  minProperties: 1
		  properties:
		    href:
		      type: string
	'''

}
