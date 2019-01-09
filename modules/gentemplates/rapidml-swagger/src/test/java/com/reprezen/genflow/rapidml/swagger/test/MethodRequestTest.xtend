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
import io.swagger.models.RefModel
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.HeaderParameter
import io.swagger.models.parameters.QueryParameter
import org.junit.Test

import static org.hamcrest.CoreMatchers.*

class MethodRequestTest extends SwaggerGenTestBase{

	def String model() {
		'''
			rapidModel TaxBlaster
				resourceAPI TaxBlasterAPI baseURI "http://my-namespace.com"
			
					objectResource StandardResource type TaxFiling
						URI /standard
						method GET
							request
								required param language type string in query
								param optionalParam type string in query
								param optionalHeaderParam type string in header
								required param requiredHeaderParam type string in header
							response StandardResource statusCode 200
					
						method POST
							request with StandardResource
							response with StandardResource statusCode 200
					
					objectResource InverseResource type TaxFiling
						URI /inversed
						method GET
							request with InverseResource // with datatype
							response InverseResource statusCode 200
					
						method POST
							request // no data type
							response with InverseResource statusCode 200
							
				dataModel TaxBlasterDataModel
					structure TaxFiling
						id : string
        '''
	}

	@Test
	def void printModel() throws Exception {
		val zenModel = loadModelAndNormalize(model())

		val generator = new XGenerateSwagger()
		generator.init(new FakeGenTemplateContext)

		val swaggerText = generator.generate(zenModel)
		println(swaggerText)
	}

	@Test
	def void test_standard_method_requests() throws Exception {
		val zenModel = loadModelAndNormalize(model())

		val generator = new XGenerateSwagger()
		generator.init(new FakeGenTemplateContext)
		val swagger = generator.getSwagger(zenModel)

		val resource = swagger.paths.get("/standard")
		assertNotNull(resource)

		// get
		assertEquals(4, resource.get.parameters.length)

		assertThat(resource.get.parameters.get(0), instanceOf(typeof(HeaderParameter)))
		assertThat(resource.get.parameters.get(0).name, equalTo("optionalHeaderParam"))
		assertThat(resource.get.parameters.get(0).required, equalTo(false))

		assertThat(resource.get.parameters.get(1), instanceOf(typeof(HeaderParameter)))
		assertThat(resource.get.parameters.get(1).name, equalTo("requiredHeaderParam"))
		assertThat(resource.get.parameters.get(1).required, equalTo(true))

		assertThat(resource.get.parameters.get(2), instanceOf(typeof(QueryParameter)))
		assertThat(resource.get.parameters.get(2).name, equalTo("language"))
		assertThat(resource.get.parameters.get(2).required, equalTo(true))

		assertThat(resource.get.parameters.get(3), instanceOf(typeof(QueryParameter)))
		assertThat(resource.get.parameters.get(3).name, equalTo("optionalParam"))
		assertThat(resource.get.parameters.get(3).required, equalTo(false))

		//post
		assertEquals(1, resource.post.parameters.length)
		assertThat(resource.post.parameters.head, instanceOf(typeof(BodyParameter)))
		assertThat(resource.post.parameters.head.name, equalTo("TaxFiling"))
		assertThat(((resource.post.parameters.head as BodyParameter).schema as RefModel).$ref,
			equalTo("#/definitions/TaxFiling"))
	}

	@Test
	def void test_inverse_method_requests() throws Exception {
		val zenModel = loadModelAndNormalize(model())

		val generator = new XGenerateSwagger()
		generator.init(new FakeGenTemplateContext)
		val swagger = generator.getSwagger(zenModel)

		val resource = swagger.paths.get("/inversed")
		assertNotNull(resource)

		//get
		assertEquals(1, resource.get.parameters.length)
		assertThat(resource.get.parameters.head, instanceOf(typeof(BodyParameter)))
		assertThat(resource.get.parameters.head.name, equalTo("TaxFiling"))
		assertThat(((resource.get.parameters.head as BodyParameter).schema as RefModel).$ref,
			equalTo("#/definitions/TaxFiling"))

		//post
		assertNull(resource.post.parameters)
	}

}
