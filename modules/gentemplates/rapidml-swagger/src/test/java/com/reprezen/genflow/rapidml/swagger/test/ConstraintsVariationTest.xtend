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
import io.swagger.models.Operation
import io.swagger.models.Swagger
import io.swagger.models.properties.AbstractNumericProperty
import io.swagger.models.properties.RefProperty
import io.swagger.models.properties.StringProperty
import java.math.BigDecimal
import org.junit.Assert
import org.junit.Test

class ConstraintsVariationTest extends SwaggerGenTestBase {

	@Test
	def stringLength_FromTest() throws Exception {
		checkStringConstraints("string", "length from 1", 1, null, null)
	}

	@Test
	def stringLength_ToTest() throws Exception {
		checkStringConstraints("string", "length to 10", null, 10, null)
	}

	@Test
	def stringLength_FromToEqualsTest() throws Exception {
		checkStringConstraints("string", "length from 10 to 10", 10, 10, null)
	}

	@Test
	def stringLength_FromToTest() throws Exception {
		checkStringConstraints("string", "length from 0 to 10", 0, 10, null)
	}

	@Test
	def regexTest() throws Exception {
		checkStringConstraints("string", "matching regex \"[A-Z]+\"", null, null, "[A-Z]+")
	}

	@Test
	def valueRange_FromExToIncTest() throws Exception {
		checkNumberConstraints("double", "valueRange from '1.0' exclusive to '2.0' inclusive", 1.0.bigd, 2.0.bigd, true,
			null)
	}

	@Test
	def valueRange_FromIncToExTest() throws Exception {
		checkNumberConstraints("double", "valueRange from '1.0' inclusive to '2.0' exclusive", 1.0.bigd, 2.0.bigd, null,
			true)
	}

	@Test
	def valueRange_FromTest() throws Exception {
		checkNumberConstraints("double", "valueRange from minimum '1.0'", 1.0.bigd, null, null, null)
	}

	@Test
	def valueRange_ToTest() throws Exception {
		checkNumberConstraints("double", "valueRange up to '2.0'", null, 2.0.bigd, null, null)
	}

	def checkStringConstraints(String type, String constraint, Integer minLength, Integer maxLength, String pattern) {
		val prop = getProperty(type, constraint)

		assertTrue(prop instanceof StringProperty)
		val stringProp = prop as StringProperty
		assertEquals(minLength, stringProp.minLength)
		assertEquals(maxLength, stringProp.maxLength)
		assertEquals(pattern, stringProp.pattern)
	}

	def checkNumberConstraints(String type, String constraint, BigDecimal min, BigDecimal max, Boolean minEx,
		Boolean maxEx) {
		val prop = getProperty(type, constraint)

		assertTrue(prop instanceof AbstractNumericProperty)
		val numProp = prop as AbstractNumericProperty
		assertNumber(min, numProp.minimum)
		if (minEx !== null && minEx) {
			assertTrue("exclusiveMinimum should be true", numProp.exclusiveMinimum)
		}
		assertNumber(max, numProp.maximum)
		if (maxEx !== null && maxEx) {
			assertTrue("exclusiveMaximum should be true", numProp.exclusiveMaximum)
		}
	}

	def assertNumber(BigDecimal expected, BigDecimal actual) {
		if (expected !== null) {
			assertNotNull("Expected " + expected + " but was null", actual)
			assertEquals(expected, actual)
		} else {
			assertNull(actual)
		}
	}

	def getProperty(String type, String constraint) {
		val zenModel = loadModelAndNormalize(model(type, constraint))

		val generator = new XGenerateSwagger()
		generator.init(new FakeGenTemplateContext())
		val swagger = generator.getSwagger(zenModel)
		swagger.checkSwaggerConsistency
		val headerProperty = swagger.getPath('/uri').get.responses.get('200').headers.get('prop1')

		assertNotNull("Header param \"prop1\" not found", headerProperty)
		headerProperty
	}

	def String model(String type, String constraint) {
		'''
			rapidModel ResourceRealizationConstraints 
				resourceAPI ResourceAPI baseURI "http://localhost"
					objectResource MyObject type Structure
						URI /uri
						mediaTypes
							application/xml
						method GET getMyObject
							request
							response type Structure statusCode 200
								all properties
									prop1
										«constraint»
								param prop1 bound to property prop1 in header
				dataModel DataModel
					structure Structure
						prop1: «type»
		'''
	}

	def checkSwaggerConsistency(Swagger swagger) {
		if (swagger.definitions === null) {

			// see https://modelsolv.atlassian.net/browse/ZEN-1813?focusedCommentId=20728&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-20728
			return
		}
		Assert.assertNotNull(swagger.definitions)
		swagger.paths?.forEach [ uri, path |
			path.operations?.forEach[op|checkAllReferencedStructuresExist(swagger, uri, op)]
		]
	}

	def checkAllReferencedStructuresExist(Swagger swagger, String uri, Operation operation) {
		val references = operation.responses?.mapValues[it.schema]?.filter[code, schema|schema instanceof RefProperty]
		references?.forEach [ code, schema |
			val ref = schema as RefProperty
			Assert.assertTrue(
                '''Uri «uri»: one of the operations references for code «code» unknown structure «ref.get$ref»''',
				swagger.definitions.containsKey(ref.simpleRef))
		]
	}

	def private bigd(Double value) {
		BigDecimal.valueOf(value)
	}
}
