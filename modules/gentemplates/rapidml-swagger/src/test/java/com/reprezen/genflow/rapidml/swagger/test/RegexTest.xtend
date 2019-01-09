/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger.test

import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import org.junit.Test

class RegexTest extends SwaggerGenTestBase{

	@Test
	def void testGeneratedSwaggerIsValidForQuotes() {
		assertGeneratedSwaggerIsValid(model('"'))
	}

	@Test
	def void testGeneratedSwaggerIsValidForApostrophe() {
		assertGeneratedSwaggerIsValid(model("'"))
	}

	def void assertGeneratedSwaggerIsValid(CharSequence model) {
		val zenModel = loadModelAndNormalize(model)		
		val generator = new XGenerateSwagger();
		generator.init(new FakeGenTemplateContext());
		val swagger = generator.generate(zenModel)
		assertNotNull(swagger)
	}

	def model(String regexDelim) {
		'''
			rapidModel TaxBlaster
				resourceAPI TaxBlasterInterface baseURI "http://taxblaster.com/api"
					objectResource MyObject type MyDataType
						URI /root
						mediaTypes
							application/xml
				dataModel MyDataModel
					structure MyDataType
						id : string
							matching regex r«regexDelim»patternValue«regexDelim»
		'''
	}

}
