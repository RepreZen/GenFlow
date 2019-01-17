package com.reprezen.genflow.swagger.doc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.reprezen.genflow.api.normal.openapi.ObjectType
import com.reprezen.genflow.api.normal.openapi.OpenApiNormalizer
import com.reprezen.genflow.api.normal.openapi.Option
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import java.net.URL
import org.jsoup.Jsoup
import org.junit.Test

import static org.junit.Assert.*

class CardinalityTest {

	def String contentWithNoMinItems() {
		'''
			---  
			swagger: "2.0"
			info:
			  description: Tax Blaster
			  version: 1.0.0
			  title: TaxBlaster
			host: taxblaster.com
			basePath: /api
			paths: {}
			definitions:
			  Foo:
			    type: object    
			    properties:
			      values:
			        type: array
			        items:
			          type: string
		'''
	}

	def String contentWithOnlyMinItems(Integer cardinality) {
		'''
			---  
			swagger: "2.0"
			info:
			  description: Tax Blaster
			  version: 1.0.0
			  title: TaxBlaster
			host: taxblaster.com
			basePath: /api
			paths: {}
			definitions:
			  Foo:
			    type: object    
			    properties:
			      values:
			        type: array
			        minItems: «cardinality»
			        items:
			          type: string

		'''
	}

	def String contentWithOnlyMaxItems(Integer cardinality) {
		'''
			---  
			swagger: "2.0"
			info:
			  description: Tax Blaster
			  version: 1.0.0
			  title: TaxBlaster
			host: taxblaster.com
			basePath: /api
			paths: {}
			definitions:
			  Foo:
			    type: object    
			    properties:
			      values:
			        type: array
			        maxItems: «cardinality»
			        items:
			          type: string

		'''
	}

	def String contentWithMinMaxItems(Integer min, Integer max) {
		'''
			---  
			swagger: "2.0"
			info:
			  description: Tax Blaster
			  version: 1.0.0
			  title: TaxBlaster
			host: taxblaster.com
			basePath: /api
			paths: {}
			definitions:
			  Foo:
			    type: object    
			    properties:
			      values:
			        type: array
			        minItems: «min»
			        maxItems: «max»
			        items:
			          type: string
		'''
	}

	private def parse(String content) {
		val mapper = new ObjectMapper(new YAMLFactory)
		val tree = mapper.readTree(content)

		val model = new OpenApiNormalizer(
			ObjectType.SWAGGER_MODEL_VERSION,
			Option.DOC_DEFAULT_OPTIONS
		).of(tree).normalizeToSwagger(new URL("file://test.yaml"))

		val generator = new XGenerateSwaggerDoc()
		val context = new FakeGenTemplateContext
		context.genTargetParameters.put(XGenerateSwaggerDoc.PREVIEW_PARAM, true)
		generator.init(context)
		val result = generator.generate(model)

		val doc = Jsoup.parse(result)
		doc.select(".panel-body").get(1).select("table > tbody > tr")
	}

	@Test
	def void testNoMinItems() {
		val el = parse(contentWithNoMinItems)

		assertNotNull(el)
		assertEquals(2, el.size)

		val givenNamesEl = el.get(1).select("td")
		assertEquals(3, givenNamesEl.size)
		assertTrue(givenNamesEl.get(1).select("code").text.contains("string"))
	}

	@Test
	def void testMinItemsEqualsZero() {
		val el = parse(contentWithOnlyMinItems(0))

		assertNotNull(el)
		assertEquals(2, el.size)

		val givenNamesEl = el.get(1).select("td")
		assertEquals(3, givenNamesEl.size)
		assertTrue(givenNamesEl.get(1).select("code").text.contains("string"))
	}

	@Test
	def void testMinItemsEqualsOne() {
		val el = parse(contentWithOnlyMinItems(1))

		assertNotNull(el)
		assertEquals(2, el.size)

		val givenNamesEl = el.get(1).select("td")
		assertEquals(3, givenNamesEl.size)
		assertTrue(givenNamesEl.get(1).select("code").text.contains("string[+]"))
	}

	@Test
	def void testMaxItemsEqualsZero() {
		val el = parse(contentWithOnlyMaxItems(0))

		assertNotNull(el)
		assertEquals(2, el.size)

		val givenNamesEl = el.get(1).select("td")
		assertEquals(3, givenNamesEl.size)
		assertTrue(givenNamesEl.get(1).select("code").text.contains("string[*]"))
	}

	@Test
	def void testMaxItemsEqualsOne() {
		val el = parse(contentWithOnlyMaxItems(1))

		assertNotNull(el)
		assertEquals(2, el.size)

		val givenNamesEl = el.get(1).select("td")
		assertEquals(3, givenNamesEl.size)
		assertTrue(givenNamesEl.get(1).select("code").text.contains("string[?]"))
	}

	@Test
	def void testMinMaxItems() {
		{
			val el = parse(contentWithMinMaxItems(1, 1))

			val givenNamesEl = el.get(1).select("td")
			assertEquals(3, givenNamesEl.size)
			assertTrue(givenNamesEl.get(1).select("code").text.contains("string[1]"))
		}
		{
			val el = parse(contentWithMinMaxItems(2, 4))

			val givenNamesEl = el.get(1).select("td")
			assertEquals(3, givenNamesEl.size)
			assertTrue(givenNamesEl.get(1).select("code").text.contains("string[2..4]"))
		}
	}
}
