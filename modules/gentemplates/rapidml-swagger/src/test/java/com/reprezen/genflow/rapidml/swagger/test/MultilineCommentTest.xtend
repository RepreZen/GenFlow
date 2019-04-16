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
import com.reprezen.genflow.common.jsonschema.Options
import com.reprezen.genflow.rapidml.swagger.XGenerateSwaggerYaml
import com.reprezen.genflow.rapidml.swagger.XSwaggerGenTemplate
import com.reprezen.rapidml.implicit.DocumentationSpaceProcessor
import org.junit.Test

class MultilineCommentTest extends SwaggerGenTestBase {

	val String properMultiline = '''
		Line 1
		Line 2
		Line 3
	'''

	val String multilineWithSpaceFollowedByLineBreak = '''
		Line 1
		Line 2 spaceAndLinebreak 
		Line 3
	'''

	val String multilineWithTrailingSpace = '''
		Line 1
		Line 2
		Line 3 space 
	'''

	val String controlSymbol = '''
		Line 1
		Line 2
		Line 3 space {0}:"value"
	'''

	def String model() {
		'''
			rapidModel MultilineComment
				/**Line 1
				Line 2
				Line 3*/
				resourceAPI MultilineCommentInterface
			
					/** «properMultiline»*/
					objectResource Resource_ProperMultiline type Type
			
					/** «multilineWithSpaceFollowedByLineBreak»*/
					objectResource Resource_MultilineWithSpaceFollowedByLineBreak type Type
			
					/** «multilineWithTrailingSpace»*/
					objectResource Resource_MultilineWithTrailingSpace type Type
			
					/** «controlSymbol»*/
					objectResource Resource_ControlSymbol type Type
			
				dataModel MultilineCommentDataModel
					structure Type
						id: string
        '''
	}

	// @Test
	def void printModel() throws Exception {
		val zenModel = loadModelAndNormalize(model)

		val generator = new XGenerateSwaggerYaml()
		generator.init(createFakeGenTemplateContext)

		val swaggerText = generator.generate(zenModel)
		println(swaggerText)
	}

	@Test
	def void regex_proper_multiline() throws Exception {
		val processed = new DocumentationSpaceProcessor().removeTrailingSpaces(properMultiline);
		assertEquals(properMultiline.trim, processed)
	}

	@Test
	def void regex_multiline_with_space_followed_by_linebreak() throws Exception {
		val processed = new DocumentationSpaceProcessor().removeTrailingSpaces(multilineWithSpaceFollowedByLineBreak);
		val expected = '''
			Line 1
			Line 2 spaceAndLinebreak
			Line 3
		'''
		assertEquals(expected.trim, processed)
	}

	@Test
	def void regex_multiline_with_trailing_space() throws Exception {
		val processed = new DocumentationSpaceProcessor().removeTrailingSpaces(multilineWithTrailingSpace);
		val expected = '''
			Line 1
			Line 2
			Line 3 space
		'''
		assertEquals(expected.trim, processed)
	}

	@Test
	def void regex_multiline_with_dots() throws Exception {
		// It will match any number of periods, followed by at least one space,
		// followed by a newline, followed by any number of periods. And then you'll remove the periods and spaces and leave a newline, 
		val input = '''
		... 
		.....'''
		val processed = new DocumentationSpaceProcessor().removeTrailingSpaces(input);
		val expected = '''
			...
			.....
		'''
		assertEquals(expected.trim, processed)
	}

	@Test @Ignore
	def void test_proper_multiline() throws Exception {
		val zenModel = loadModelAndNormalize(model)

		val generator = new XGenerateSwaggerYaml()
		generator.init(createFakeGenTemplateContext)

		val swaggerText = generator.generate(zenModel)
		val multilineTagValue = '''
			- name: Resource_ProperMultiline
			  description: |-
			    Line 1
			    Line 2
			    Line 3
	    '''
		assertTrue(swaggerText.contains(multilineTagValue))
	}

	@Test @Ignore
	def void test_multiline_with_space_followed_by_linebreak() throws Exception {
		val zenModel = loadModelAndNormalize(model)

		val generator = new XGenerateSwaggerYaml()
		generator.init(createFakeGenTemplateContext)

		val swaggerText = generator.generate(zenModel)
		val multilineTagValue = '''
			- name: Resource_MultilineWithSpaceFollowedByLineBreak
			  description: |-
			    Line 1
			    Line 2 spaceAndLinebreak
			    Line 3
    	'''
		assertTrue(swaggerText.contains(multilineTagValue))
	}

	@Test @Ignore
	def void test_multiline_with_trailing_space() throws Exception {
		val zenModel = loadModelAndNormalize(model)

		val generator = new XGenerateSwaggerYaml()
		generator.init(createFakeGenTemplateContext)

		val swaggerText = generator.generate(zenModel)
		val multilineTagValue = '''
			- name: Resource_MultilineWithTrailingSpace
			  description: |-
			    Line 1
			    Line 2
			    Line 3 space
    	'''
		assertTrue(swaggerText.contains(multilineTagValue))
	}

	def private createFakeGenTemplateContext() {
		return new FakeGenTemplateContext(
			#{Options::ALLOW_EMPTY_OBJECT -> true, Options::ALLOW_EMPTY_ARRAY -> true,
				Options::ALLOW_EMPTY_STRING -> true, XSwaggerGenTemplate::FOLD_MULTILINE -> true})
	}

}
