package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class RequiredProperties_inReferenceTreatment_Test extends XChangeSchemaTestBase {

	var String objectResource = "";

	override String rapid_model() {
		'''
rapidModel RequiredProperties_ReferenceTreatments
	resourceAPI RequiredProperties_ReferenceTreatmentsAPI baseURI "http://my-namespace.com"
	
		«objectResource»

		objectResource Resource2 type DataType2


	dataModel DataModel
	
		structure DataType1
			DT1_required : string!
			DT1_optional : string
			DT1_ref: reference DataType2
			
		structure DataType2
			DT2_required : string!
			DT2_optional2required : string
			DT2_optional2 : string
		
		structure DataType0
			DT0_ref: reference DataType1
'''
	}

	@Test
	def contract_resource_defaultRealization() {
		val resourceName = "Resource1_DefaultRealization"
		objectResource = '''
		objectResource «resourceName» type DataType1'''

		testContract(
			'DataType1' -> '''
			type: object
			minProperties: 1
			properties:
			  DT1_required:
			    type: string
			    minLength: 1
			  DT1_optional:
			    type: string
			    minLength: 1
			  DT1_ref:
			    $ref: '#/definitions/Resource2_link'
			required:
			- DT1_required'''
		)
	}

	@Test
	def contract_resource_withRealization_creatingRequired() {
		val resourceName = "Resource2_wRealizationCreatingRequiredProp"
		objectResource = '''
		objectResource «resourceName» type DataType2
			with all properties
			including
				DT2_optional2required!'''

		testContract(
			resourceName -> '''
			type: object
			minProperties: 1
			properties:
			  DT2_required:
			    type: string
			    minLength: 1
			  DT2_optional2required:
			    type: string
			    minLength: 1
			  DT2_optional2:
			    type: string
			    minLength: 1
			required:
			- DT2_required
			- DT2_optional2required'''
		)
	}

	@Test
	def contract_resource_withReferenceLink_Default() {
		val resourceName = "Resource1_DefaultReferenceLink"
		objectResource = '''
		objectResource «resourceName» type DataType1
			referenceLink > DT1_ref
				targetResource Resource2'''

		testContract(
			resourceName -> '''
			type: object
			minProperties: 1
			properties:
			  DT1_required:
			    type: string
			    minLength: 1
			  DT1_optional:
			    type: string
			    minLength: 1
			  DT1_ref:
			    $ref: '#/definitions/Resource2_link'
			# only DT1_required is required
			required:
			- DT1_required'''
		)
	}

	@Test
	def contract_resource_withReferenceEmbed_Default() {
		val resourceName = "Resource1_DefaultReferenceEmbed"
		objectResource = '''
		objectResource «resourceName» type DataType1
			referenceEmbed > DT1_ref'''

		testContract(
			resourceName -> '''
			type: object
			minProperties: 1
			properties:
			  DT1_required:
			    type: string
			    minLength: 1
			  DT1_optional:
			    type: string
			    minLength: 1
			  DT1_ref:
			    $ref: '#/definitions/DataType2'
			required:
			- DT1_required'''
		)
	}

	@Test
	def contract_resource_withReferenceLink_CreatesRequiredProp() {
		val resourceName = "Resource1_ReferenceLinkCreatesRequiredProp"
		objectResource = '''
		objectResource «resourceName» type DataType1
			referenceLink > DT1_ref
				targetResource Resource2
				targetProperties 
					DT2_required, DT2_optional2required!, DT2_optional2'''

		testContract(
			resourceName -> '''
			type: object
			minProperties: 1
			properties:
			  DT1_required:
			    type: string
			    minLength: 1
			  DT1_optional:
			    type: string
			    minLength: 1
			  DT1_ref:
			    type: object
			    minProperties: 1
			    properties:
			      _links:
			        $ref: "#/definitions/_RapidLinksMap"
			      DT2_required:
			        type: string
			        minLength: 1
			      DT2_optional2required:
			        type: string
			        minLength: 1
			      DT2_optional2:
			        type: string
			        minLength: 1
			    required:
			    - DT2_required
			    - DT2_optional2required
			required:
			- DT1_required'''
		)
	}

	@Test
	def contract_resource_withReferenceLink_withExternalDescriptorCreatingRequiredProp() {
		val resourceName = "Resource1_ReferenceLink_wExternalDescriptorCreatingRequiredProp"
		val referencedResourceName = "Resource2_wRealizationCreatingRequiredProp_link";

		objectResource = '''
			objectResource «resourceName» type DataType1
				referenceLink > DT1_ref
					targetResource Resource2_wRealizationCreatingRequiredProp
			
			objectResource Resource2_wRealizationCreatingRequiredProp type DataType2
				with all properties
				including
					DT2_optional2required!
		'''

		testContract(
			resourceName -> '''
			type: object
			minProperties: 1
			properties:
			  DT1_required:
			    type: string
			    minLength: 1
			  DT1_optional:
			    type: string
			    minLength: 1
			  DT1_ref:
			    "$ref": "#/definitions/«referencedResourceName»"
			required:
			- DT1_required'''
		)

		testContract(
			referencedResourceName -> '''
			type: object
			minProperties: 1
			properties:
			  _links:
			    "$ref": "#/definitions/_RapidLinksMap"'''
		)
	}

	@Test
	def contract_resource_withReferenceEmbed_CreatesRequiredProp() {
		val resourceName = "Resource1_ReferenceEmbedCreatesRequiredProp"
		val referencedResourceName = "Resource1_ReferenceEmbedCreatesRequiredProp_DT1_ref"
		objectResource = '''
		objectResource «resourceName» type DataType1
			referenceEmbed > DT1_ref
				targetProperties 
					DT2_required, DT2_optional2required!, DT2_optional2'''

		testContract(
			resourceName -> '''
			type: object
			minProperties: 1
			properties:
			  DT1_required:
			    type: string
			    minLength: 1
			  DT1_optional:
			    type: string
			    minLength: 1
			  DT1_ref:
			    $ref: '#/definitions/«referencedResourceName»'
			required:
			- DT1_required'''
		)

		testContract(
			referencedResourceName -> '''
			type: object
			minProperties: 1
			properties:
			  DT2_required:
			    type: string
			    minLength: 1
			  DT2_optional2required:
			    type: string
			    minLength: 1
			  DT2_optional2:
			    type: string
			    minLength: 1
			required:
			- DT2_required
			- DT2_optional2required '''
		)
	}

	@Test
	def contract_resource_withEmbedded_ReferenceLinkCreatesRequiredProp() {
		val resourceName = "Resource0_withEmbedded_ReferenceLinkCreatesRequiredProp"
		val referencedResourceName = "Resource0_withEmbedded_ReferenceLinkCreatesRequiredProp_DT0_ref"

		objectResource = '''
		objectResource «resourceName» type DataType0
			referenceEmbed > DT0_ref
				referenceLink > DT1_ref
					targetResource Resource2
					targetProperties 
						DT2_required, DT2_optional2required!, DT2_optional2'''

		testContract(
			resourceName -> '''
			type: object
			minProperties: 1
			properties:
			  DT0_ref:
			    "$ref": "#/definitions/«referencedResourceName»"'''
		)

		testContract(
			referencedResourceName -> '''
			type: object
			minProperties: 1
			properties:
			  DT1_required:
			    type: string
			    minLength: 1
			  DT1_optional:
			    type: string
			    minLength: 1
			  DT1_ref:
			    type: object
			    minProperties: 1
			    properties:
			      _links:
			        "$ref": "#/definitions/_RapidLinksMap"
			      DT2_required:
			        type: string
			        minLength: 1
			      DT2_optional2required:
			        type: string
			        minLength: 1
			      DT2_optional2:
			        type: string
			        minLength: 1
			    required:
			    - DT2_required
			    - DT2_optional2required
			required:
			- DT1_required'''
		)

	}

}
