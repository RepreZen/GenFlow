/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import org.junit.Test

class ExampleTest extends SwaggerGenTestBase{

	@Test
	def void testJsonExample() {
		val zenModel = loadModelAndNormalize(modelWithJsonExample) 

		val swagger = new XGenerateSwagger().getSwagger(zenModel)
		val response = swagger.getPath('/index').get.responses.get('200');

		assertEquals(1, response.examples.size)
		// following ought to be an equals test between trees, but it doesn't work - probably different jackson versions involved
		assertEquals(new ObjectMapper().readTree('''{"prop1": 1, "prop2": "test_value"}''').toString(),
			response.examples.get('application/json').toString())
	}

	@Test
	def void testXmlExample() {
		val zenModel = loadModelAndNormalize(modelWithXmlExample)

		val swagger = new XGenerateSwagger().getSwagger(zenModel)
		val response = swagger.getPath('/index').get.responses.get('200');

		assertEquals(1, response.examples.size)
		assertEquals('''
			<?xml version="1.0" encoding="UTF-8"?>
			<TaxFiling version="1.0">
				<filingID>#123456</filingID>
				<taxLiability>45,000.00</taxLiability>
			</TaxFiling>
		'''.toString.trim, response.examples.get('application/xml').toString.trim)
	}

	def modelWithJsonExample() {
		'''
			rapidModel SwaggerTest
			
				resourceAPI swaggerTestAPI baseURI "http://localhost/api"
			
					objectResource oRes1 type Structure
						URI index
						mediaTypes
							application/json
							application/xml
						method GET getIndex
							response oRes1 statusCode 200
								example «"'''"»{"prop1" : 1, "prop2": "test_value"}«"'''"»
							response statusCode 404
						example «"'''"»{"resource_prop1" : 1, "resource_prop2": "test_value"}«"'''"»
			
				dataModel TaxBlasterDataModel
					structure Structure
						prop1 : long
						prop2 : string
						prop2ref: reference to Structure2
					structure Structure2
						prop1 : string
		'''
	}

	def modelWithXmlExample() {
		'''
			rapidModel SwaggerTest
			
				resourceAPI swaggerTestAPI baseURI "http://localhost/api"
			
					objectResource oRes1 type Structure
						URI index
						mediaTypes
							application/xml
						method GET getIndex
							response oRes1 statusCode 200
								example «"'''"»
			<?xml version="1.0" encoding="UTF-8"?>
			<TaxFiling version="1.0">
				<filingID>#123456</filingID>
				<taxLiability>45,000.00</taxLiability>
			</TaxFiling>«"'''"»
							response statusCode 404
				dataModel TaxBlasterDataModel
					structure Structure
						prop1 : long
						prop2 : string
						prop2ref: reference to Structure2
					structure Structure2
						prop1 : string
		'''
	}
}
