package com.reprezen.genflow.openapi3.doc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.kaizen.oasparser.OpenApiParser
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.net.URL
import org.jsoup.Jsoup
import org.junit.Test

import static org.junit.Assert.*

class CardinalityTest {

	def String contentWithNoMinItems() {
		'''
			---
			openapi: "3.0.0"
			info:
			  version: 1.0.0
			  title: My API Spec
			paths:
			  /resourceUrl:
			    get:
			      responses:
			        200:
			          description: Success
			          content:
			            application/json:
			              schema:
			                type: object
			                properties:
			                  givenNames:
			                    type: array
			                    items:
			                      type: string
		'''
	}

	def String contentWithOnlyMinItems(Integer cardinality) {
		'''
			---
			openapi: "3.0.0"
			info:
			  version: 1.0.0
			  title: My API Spec
			paths:
			  /resourceUrl:
			    get:
			      responses:
			        200:
			          description: Success
			          content:
			            application/json:
			              schema:
			                type: object
			                properties:
			                  givenNames:
			                    type: array
			                    items:
			                      type: string
			                    minItems: «cardinality»
		'''
	}

	def String contentWithOnlyMaxItems(Integer cardinality) {
		'''
			---
			openapi: "3.0.0"
			info:
			  version: 1.0.0
			  title: My API Spec
			paths:
			  /resourceUrl:
			    get:
			      responses:
			        200:
			          description: Success
			          content:
			            application/json:
			              schema:
			                type: object
			                properties:
			                  givenNames:
			                    type: array
			                    items:
			                      type: string
			                    maxItems: «cardinality»
		'''
	}

	private def parse(String content) {
		val mapper = new ObjectMapper(new YAMLFactory)
		val model = new OpenApiParser().parse(mapper.readTree(
			content
		), new URL("file://test.yaml")) as OpenApi3
		
		val generator = new XGenerateOpenApi3Doc()
		generator.init(new FakeGenTemplateContext)
		val result = generator.generate(model)
		
		val doc = Jsoup.parse(result)
		doc.select(".list-group-item > table > tbody > tr")
	}

	@Test
	def void testNoMinItems() {
		val el = parse(contentWithNoMinItems)

		assertNotNull(el)
		assertEquals(2, el.size)

		val givenNamesEl = el.get(1).select("td")
		assertEquals(3, givenNamesEl.size)
		assertTrue(givenNamesEl.get(1).select("code").text.contains("items"))
	}

	@Test
	def void testMinItemsEqualsZero() {
		val el = parse(contentWithOnlyMinItems(0))

		assertNotNull(el)
		assertEquals(2, el.size)

		val givenNamesEl = el.get(1).select("td")
		assertEquals(3, givenNamesEl.size)
		assertTrue(givenNamesEl.get(1).select("code").text.contains("items"))
	}

	@Test
	def void testMinItemsEqualsOne() {
		val el = parse(contentWithOnlyMinItems(1))

		assertNotNull(el)
		assertEquals(2, el.size)

		val givenNamesEl = el.get(1).select("td")
		assertEquals(3, givenNamesEl.size)
		assertTrue(givenNamesEl.get(1).select("code").text.contains("items[+]"))
	}

	@Test
	def void testMaxItemsEqualsZero() {
		val el = parse(contentWithOnlyMaxItems(0))

		assertNotNull(el)
		assertEquals(2, el.size)

		val givenNamesEl = el.get(1).select("td")
		assertEquals(3, givenNamesEl.size)
		assertTrue(givenNamesEl.get(1).select("code").text.contains("items[*]"))
	}

	@Test
	def void testMaxItemsEqualsOne() {
		val el = parse(contentWithOnlyMaxItems(1))

		assertNotNull(el)
		assertEquals(2, el.size)

		val givenNamesEl = el.get(1).select("td")
		assertEquals(3, givenNamesEl.size)
		assertTrue(givenNamesEl.get(1).select("code").text.contains("items[?]"))
	}
}
