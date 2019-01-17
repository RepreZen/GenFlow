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

class PropertyTableTest {

	val model = '''
		---
		swagger: "2.0"
		info:  
		  version: 1.0.0
		  title: Test
		schemes:
		- http
		paths:
		  /pets:
		    get:
		      responses:
		        200:
		          description: Ok
		          schema:
		            type: array
		            items:
		              type: object
		              properties:
		                id:
		                  type: integer
		                  format: int64
		                name:
		                  type: string
		                nicknames:
		                  type: array
		                  items:
		                    type: string
		                records: 
		                  type: array
		                  items:
		                    $ref: "#/definitions/Record" 
		definitions:
		  Record:
		    type: object
		    properties:
		      id:
		        type: integer
		      documentDate:
		        type: string
		        format: date
		      content:
		        type: string
	'''
	
	private def parse(String content) {
		val mapper = new ObjectMapper(new YAMLFactory)

		 val model = new OpenApiNormalizer(
			ObjectType.SWAGGER_MODEL_VERSION,
			Option.DOC_DEFAULT_OPTIONS
		).of(mapper.readTree(content)).normalizeToSwagger(new URL("file://test.yaml"))

		val generator = new XGenerateSwaggerDoc()
		val context = new FakeGenTemplateContext
		context.genTargetParameters.put(XGenerateSwaggerDoc.PREVIEW_PARAM, true)
		generator.init(context)
		generator.generate(model)
	}

	private def select(String doc) {
		Jsoup.parse(doc).select(".list-group > .list-group-item:last-child > table > tbody > tr")
	}

	@Test
	def void testResponseReturnType() {
		val doc = Jsoup.parse(parse(model))
		val elType = doc.select(".list-group > .list-group-item:last-child > code")

		assertTrue(elType.text.contains("object[*]"))
	}

	@Test
	def void testMethodHeader() {
		val rows = select(parse(model))

		assertEquals(10, rows.size)

		val header = rows.get(0)
		assertEquals(3, header.childNodes.size)
		assertTrue(header.child(0).text.contains("Name"))
		assertTrue(header.child(1).text.contains("Type"))
		assertTrue(header.child(2).text.contains("Description"))
	}

	@Test
	def void testMethodPrimtitiveTypes() {
		val rows = select(parse(model))

		assertEquals(10, rows.size)

		val idProp = rows.get(1)
		assertEquals(3, idProp.childNodes.size)
		assertTrue(idProp.child(0).text.contains("id"))
		assertTrue(idProp.child(1).text.contains("integer"))

		val idPropTypeTitle = rows.get(2).select("tbody > tr > th")
		val idPropTypeFormat = rows.get(2).select("tbody > tr > td")

		assertTrue(idPropTypeTitle.text.contains("Format"))
		assertTrue(idPropTypeFormat.text.contains("int64"))

		val nameProp = rows.get(3)
		assertEquals(3, nameProp.childNodes.size)
		assertTrue(nameProp.child(0).text.contains("name"))
		assertTrue(nameProp.child(1).text.contains("string"))
		
		val nickNamesProp = rows.get(4)
		assertEquals(3, nickNamesProp.childNodes.size)
		assertTrue(nickNamesProp.child(0).text.contains("nicknames"))
		assertTrue(nickNamesProp.child(1).text.contains("string[*]"))

		val recordsProp = rows.get(5)
		assertEquals(3, recordsProp.childNodes.size)
		assertTrue(recordsProp.child(0).text.contains("records"))
		assertTrue(recordsProp.child(1).text.contains("Record[*]"))

		val recordsPropId = rows.get(6)
		assertEquals(3, recordsPropId.childNodes.size)
		assertTrue(recordsPropId.child(0).text.contains("id"))
		assertTrue(recordsPropId.child(1).text.contains("integer"))
		
		val recordsPropDate = rows.get(7)
		assertEquals(3, recordsPropDate.childNodes.size)
		assertTrue(recordsPropDate.child(0).text.contains("documentDate"))
		assertTrue(recordsPropDate.child(1).text.contains("string"))
		
		val recordsPropDateTitle = rows.get(8).select("tbody > tr > th")
		val recordsPropDateFormat = rows.get(8).select("tbody > tr > td")

		assertTrue(recordsPropDateTitle.text.contains("Format"))
		assertTrue(recordsPropDateFormat.text.contains("date"))

		val contentProp = rows.get(9)
		assertEquals(3, contentProp.childNodes.size)
		assertTrue(contentProp.child(0).text.contains("content"))
		assertTrue(contentProp.child(1).text.contains("string"))
	}
}
