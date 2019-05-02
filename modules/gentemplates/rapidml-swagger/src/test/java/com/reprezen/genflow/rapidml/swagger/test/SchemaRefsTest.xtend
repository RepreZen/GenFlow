/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger.test

import com.reprezen.genflow.test.common.RapidMLInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@InjectWith(typeof(RapidMLInjectorProvider))
@RunWith(typeof(XtextRunner))
class SchemaRefsTest {

	@Ignore
	@Test
	def void testResponse_SchemaRef_Zen1813_commented() {
		/*
		 * val swagger = generateSwaggerFromRapid(model_Zen1813)

		 * val expectedStructureName = "GetMyObject_Structure"

		 * val response = swagger.getPath('/uri').get.responses.get('200');
		 * assertEquals("#/definitions/" + expectedStructureName, (response.schema as RefProperty).$ref)

		 assertNotNull(swagger.definitions.get(expectedStructureName)) */
	}

	@Ignore
	@Test
	def void testResponse_SchemaRef_Zen2185_commented() {
		/*
		 * 	val swagger = generateSwaggerFromRapid(model_Zen2185)

		 * 	val expectedStructureName = "GetTaxFilingObject_TaxFiling"

		 * 	val response = swagger.getPath('/uri').get.responses.get('200');
		 * 	assertEquals("#/definitions/" + expectedStructureName, (response.schema as RefProperty).$ref)

		 assertNotNull(swagger.definitions.get(expectedStructureName)) */
	}

	def generateSwaggerFromRapid(CharSequence modelText) {
		/*
		 * 	val zenModel = parser.parse(modelText)
		 * 	zenModel.generateImplicitValues
		 * 	val generator = new XGenerateSwagger()
		 * 	generator.init(new FakeGenTemplateContext)
		 * 	val swaggerAsText = generator.generate(zenModel)
		 * 	println(swaggerAsText)

		 * 	// we need this workaround because the final result is a compination of getSwagger and JsonSchemaGenerator#generate as deefinitions
		 * 	val swagger = new Swagger20Parser().read(new ObjectMapper(new YAMLFactory()).readTree(swaggerAsText))
		 return swagger */
	}

	def model_Zen1813() {
		'''
			rapidModel ResourceRealizationConstraints 
				resourceAPI ResourceAPI baseURI "http://localhost"
					objectResource MyObject type Structure
						URI /uri
						mediaTypes
							application/xml
						method GET getMyObject
							request
							response this MyObject statusCode 200
								all properties
									prop1
										valueRange from '1.0' exclusive to '2.0' inclusive
								param prop1 bound to property prop1 in header
				dataModel DataModel
					structure Structure
						prop1: double
		'''
	}

	def model_Zen2185() {
		'''
			rapidModel TaxBlaster
				resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"
			
					objectResource TaxFilingObject type TaxFiling
						URI taxFilings/{id}
							/** filingID of the requested TaxFiling */
							required templateParam id property filingID
			
						mediaTypes
							application/xml
						method GET getTaxFiling
							request
							response with type TaxFiling statusCode 200
							response statusCode 404
			
				dataModel TaxBlasterDataModel
					/** A tax filing record for a given user, in a given tax jurisdiction, in a 
					    specified tax year. */
					structure TaxFiling
						/** A unique, system-assigned identifier for the tax filing. */
						filingID : string
						/** Country, province, state or local tax authority where this is being filed. */
						jurisdiction : string
		'''
	}
}
