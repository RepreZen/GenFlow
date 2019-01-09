package com.reprezen.genflow.rapidml.jsonschema.xchange

import org.junit.Test

class DefaultRealization_Test extends XChangeSchemaTestBase {

	override String rapid_model() {
		'''
			rapidModel DefaultRealization
				resourceAPI DefaultRealizationsAPI baseURI "http://my-namespace.com"
					//object resource with GET and PUT methods
					objectResource DefaultRealizationsObject type DefaultRealizations
						URI /uri/{id}
						referenceEmbed > ref1
			
				dataModel DefaultRealizationsDataModel
					structure DefaultRealizations
						id : string
						ref1: reference MyDataType2
						
					structure MyDataType2
						id : string
		'''
	}

	@Test
	def contract_PersonObject() {
		testContract('DefaultRealizationsObject', '''
			type: "object"
			minProperties: 1
			properties:
			  id:
			    type: "string"
			    minLength: 1
			  ref1:
			    $ref: "#/definitions/MyDataType2"
		  ''');
	}

}
