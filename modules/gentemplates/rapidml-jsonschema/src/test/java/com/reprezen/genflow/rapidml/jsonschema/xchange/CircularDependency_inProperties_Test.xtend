package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

/**
 * Tests for ZEN-3619 "RAPID-XChange Contract GenTemplate: Certain structures translating incorrectly from RAPID-ML to OpenAPI"
 */
class CircularDependency_inProperties_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
			rapidModel IconSet
				resourceAPI IconSetInterface baseURI "http://IconSet.com/api"
				
					objectResource OrganizationObject type Organization
			
					objectResource RatingObject type Rating
						URI index
						
						referenceLink > includedOrganizations
							targetResource OrganizationObject
							
						default linkDescriptor ratingLink
							ratingID
							ratingImage
			
						method GET getRating
							response with RatingObject statusCode 200
							response statusCode 404
			
				dataModel IconSetDataModel
				
					/** A set of URLs different sized icons with the same underlying meaning. */
					structure IconSet
						small : anyURI
						large : anyURI
			
					structure Rating
						ratingImage : as reference to IconSet
						ratingID: string
						includedOrganizations: reference to Organization
						
					structure Organization
						id: string
						currentRating : reference to Rating
		'''
	}

	@Test
	def contract_IconSet() {
		testContract('IconSet', '''
type: object
minProperties: 1
description: A set of URLs different sized icons with the same underlying meaning.
properties:
  small:
    type: string
    minLength: 1
  large:
    type: string
    minLength: 1''');
	}

	@Test
	def contract_Rating() {
		testContract('RatingObject', '''
type: object
minProperties: 1
properties:
  ratingID:
    type: string
    minLength: 1
  includedOrganizations:
    $ref: "#/definitions/OrganizationObject_link"
  ratingImage:
    "$ref": "#/definitions/IconSet"
    ''');
		testContract('OrganizationObject_link', '''
type: object
minProperties: 1
properties:
  _links:
    "$ref": "#/definitions/_RapidLinksMap"
    ''');
	}

}
