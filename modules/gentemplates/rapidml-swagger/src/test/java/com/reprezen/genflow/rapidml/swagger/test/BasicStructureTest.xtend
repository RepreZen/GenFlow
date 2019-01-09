/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger.test

import com.google.common.collect.Iterables
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import io.swagger.models.Operation
import io.swagger.models.Swagger
import org.junit.Test

class BasicStructureTest extends SwaggerGenTestBase {

	@Test
	def void testInfo() {
		val Swagger swagger = getSwagger
		assertEquals('localhost', swagger.host)
		assertEquals('/api', swagger.basePath)
		assertArrayEquals(#['HTTP'], Iterables::transform(swagger.schemes, [name]))

		val info = swagger.info;
		assertEquals('0.0.7', info.version)
		assertEquals('', info.description)
		assertEquals('swaggerTestAPI', info.title)
		assertNull(info.contact)
	}

	@Test
	def void testPaths() {
		val Swagger swagger = getSwagger

		assertEquals(2, swagger.paths.size)
		assertArrayEquals(#['/index', '/res'], swagger.paths.keySet)

		val path1 = swagger.paths.get('/index')

		assertEquals(1, path1.operations.size)
		assertOperation(path1.operations.get(0), 'getIndex', 'oRes1', 2)

		val path2 = swagger.paths.get('/res')
		assertEquals(2, path2.operations.size)
		assertOperation(path2.operations.get(0), 'getResColl', 'oRes1Coll', 1)
		assertOperation(path2.operations.get(1), 'updateResColl', 'oRes1Coll', 2)
	}

	@Test
	def void testTags() {
		val Swagger swagger = getSwagger

		assertArrayEquals(#['oRes1', 'oRes1Coll'], Iterables::transform(swagger.tags, [name]))
	}

	def assertOperation(Operation operation, String operationId, String tag, int responses) {
		assertEquals(operationId, operation.operationId)
		assertArrayEquals(#[tag], operation.tags)
		assertEquals(responses, operation.responses.size)
	}

	def getSwagger() {
		val zenModel = loadModelAndNormalize(model)

		new XGenerateSwagger().getSwagger(zenModel)
	}

	def model() {
		'''
			rapidModel SwaggerTest
			
				resourceAPI swaggerTestAPI baseURI "http://localhost/api" version "0.0.7"
			
					objectResource oRes1 type Structure
						URI index
						mediaTypes
							application/json
						method GET getIndex
							response oRes1 statusCode 200
							response statusCode 404
			
					default collectionResource oRes1Coll type Structure2
						URI /res
						mediaTypes
							application/json
						method GET getResColl
							request
							response type Structure statusCode 200
						method POST updateResColl
							request oRes1Coll
							response oRes1 statusCode 200
							response statusCode 400
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
