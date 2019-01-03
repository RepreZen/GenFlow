package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class EmptyValues_Realizations_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
rapidModel TaxBlaster
	resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"

		objectResource ResourceWDefaultRealization type MyStructure
			URI /uri1

		objectResource ResourceWOptionalProps type MyStructure
			URI /uri2
			with all properties including
				singleValuedProp1? // keep inherited cardinality
				multiValuedProp1* // keep inherited cardinality

		objectResource ResourceWRequiredProps type MyStructure
			URI /uri3
			with all properties including
				singleValuedProp1!
				multiValuedProp1+

	dataModel TaxBlasterDataModel
	
		structure MyStructure
			singleValuedProp1: string
			multiValuedProp1: string*
			singleValuedProp2: string! //required
			multiValuedProp2: string+ // required
'''
	}

	@Test
	def contract_ResourceWDefaultRealization() {
		testContract('MyStructure', '''
		type: object
		minProperties: 1
		
		properties:
		  singleValuedProp1:
		    type: string
		    minLength: 1
		  multiValuedProp1:
		    type: array
		    minItems: 1
		    items:
		      type: string
		      minLength: 1
		  singleValuedProp2:
		    type: string
		    minLength: 1
		  multiValuedProp2:
		    type: array
		    minItems: 1
		    items:
		      type: string
		      minLength: 1

		required:
		- singleValuedProp2
		- multiValuedProp2''')
	}

	@Test
	def contract_ResourceWOptionalProps() {
		testContract('ResourceWOptionalProps', '''
		type: object
		minProperties: 1
		
		properties:
		  singleValuedProp1:
		    type: string
		    minLength: 1
		  multiValuedProp1:
		    type: array
		    minItems: 1
		    items:
		      type: string
		      minLength: 1
		  singleValuedProp2:
		    type: string
		    minLength: 1
		  multiValuedProp2:
		    type: array
		    minItems: 1
		    items:
		      type: string
		      minLength: 1

		required:
		- singleValuedProp2
		- multiValuedProp2''')
	}

	@Test
	def contract_ResourceWRequiredProps() {
		testContract('ResourceWRequiredProps', '''
		type: object
		minProperties: 1
		
		properties:
		  singleValuedProp1:
		    type: string
		    minLength: 1
		  multiValuedProp1:
		    type: array
		    minItems: 1
		    items:
		      type: string
		      minLength: 1
		  singleValuedProp2:
		    type: string
		    minLength: 1
		  multiValuedProp2:
		    type: array
		    minItems: 1
		    items:
		      type: string
		      minLength: 1
		
		required:
		- singleValuedProp1
		- multiValuedProp1
		- singleValuedProp2
		- multiValuedProp2''')
	}

	@Test
	def interop_Structure() {
		testInterop('MyStructure', '''
			type: object
			minProperties: 1
			
			properties:
			  singleValuedProp1:
			    type: string
			    minLength: 1
			  multiValuedProp1:
			    type: array
			    minItems: 1
			    items:
			      type: string
			      minLength: 1
			  singleValuedProp2:
			    type: string
			    minLength: 1
			  multiValuedProp2:
			    type: array
			    minItems: 1
			    items:
			      type: string
			      minLength: 1
			
			  _links:
			    "$ref": "#/definitions/_RapidLinksMap"
		''')
	}

}
