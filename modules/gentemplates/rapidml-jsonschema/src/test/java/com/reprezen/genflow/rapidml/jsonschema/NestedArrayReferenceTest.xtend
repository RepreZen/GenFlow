package com.reprezen.genflow.rapidml.jsonschema

import com.reprezen.genflow.rapidml.jsonschema.xchange.XChangeSchemaTestBase
import org.junit.Test

class NestedArrayReferenceTest extends XChangeSchemaTestBase {

	override String rapid_model() {'''
		rapidModel Realizations
			resourceAPI RealizationsAPI baseURI "http://my-namespace.com"
		
				objectResource DataType1Object type DataType1
					URI /uri
		
					referenceEmbed > ref1
						referenceEmbed > ref2
							targetProperties
								nested2_prop1
						referenceEmbed > ref3
							targetProperties
								nested2_prop2
		
			dataModel RealizationsDataModel
				structure DataType1
					id : string
					prop1: string
					ref1: reference to Nested1
					
				structure Nested1
					nested1_prop1: string
					ref2: reference to Nested2*
					ref3: reference to Nested2
					
				structure Nested2
					nested2_prop1: string
					nested2_prop2: string'''}

	@Test
	def test_legacy_topLevelStructure() {
		testLegacy('DataType1Object', '''
			type: object
			properties:
			  id:
			    type: string
			  prop1:
			    type: string
			  ref1:
			    "$ref": "#/definitions/DataType1Object_ref1"''')
	}

	@Test
	def test_legacy_firstLevel_link() {
		testLegacy('DataType1Object_ref1', '''
			type: object
			properties:
			  nested1_prop1:
			    type: string
			  ref2:
			    "$ref": "#/definitions/DataType1Object_ref1_ref2"
			  ref3:
			    "$ref": "#/definitions/DataType1Object_ref1_ref3"''')
	}

	@Test
	def test_legacy_secondLevel_singlevalued() {
		testLegacy('DataType1Object_ref1_ref3', '''
			  type: object
			  properties:
			    nested2_prop2:
			      type: string''')
	}

	@Test
	def test_legacy_secondLevel_array() {
		testLegacy('DataType1Object_ref1_ref2', '''
			type: array
			items:
			  "$ref": "#/definitions/DataType1Object_ref1_ref2_item"''')
	}
	
	@Test
	def test_legacy_secondLevel_array_item() {
		testLegacy('DataType1Object_ref1_ref2_item', '''
			type: object
			properties:
			  nested2_prop1:
			    type: string''')
			
	}

}
