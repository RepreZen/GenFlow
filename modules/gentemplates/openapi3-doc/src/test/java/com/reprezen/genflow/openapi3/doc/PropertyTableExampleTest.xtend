package com.reprezen.genflow.openapi3.doc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.reprezen.genflow.api.normal.openapi.ObjectType
import com.reprezen.genflow.api.normal.openapi.OpenApiNormalizer
import com.reprezen.genflow.api.normal.openapi.Option
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.net.URL
import org.jsoup.Jsoup
import org.junit.Test

import static org.junit.Assert.*

class PropertyTableExampleTest {

	val singleExample = '''
		openapi: "3.0.0"
		info:
		  version: 1.0.0
		  title: Test
		paths:
		  /pets:
		    get:
		      responses:
		        200:      
		          description: Ok    
		          content:
		            application/json:
		              example: {
		                "foo": "bar"
		              }
	'''
	
	val manyExamples = '''
		openapi: "3.0.0"
		info:
		  version: 1.0.0
		  title: Test
		paths:
		  /pets:
		    get:
		      responses:
		        200:      
		          description: Ok    
		          content:
		            application/json:
		              examples:
		                first:
		                  value: {
		                    "foo": "bar"
		                  }
		                second: 
		                  value: "Hello"
	'''

	private def parse(String content) {
		val mapper = new ObjectMapper(new YAMLFactory)

		val model = new OpenApiNormalizer(
			ObjectType.OPENAPI3_MODEL_VERSION,
			Option.DOC_DEFAULT_OPTIONS
		).of(mapper.readTree(content)).normalizeToKaizen(new URL("file://test.yaml")) as OpenApi3

		val generator = new XGenerateOpenApi3Doc()
		val context = new FakeGenTemplateContext
		context.genTargetParameters.put(OptionHelper.PREVIEW_PARAM, true)
		generator.init(context)
		generator.generate(model)
	}

	private def select(String doc) {
		Jsoup.parse(doc).select(".list-group > .list-group-item:last-child")
	}

	@Test
	def void testResponseExample() {
		select(parse(singleExample))
		val el = select(parse(singleExample)).select("h4")

		assertEquals(2, el.size)
		assertEquals("Response 200 Example", el.text)
	}

	@Test
	def void testResponseExamples() {
		val doc = select(parse(manyExamples))
		val el = doc.select("h4")

		assertEquals(2, el.size)
		assertEquals("Response 200 Examples", el.text)

		val list = doc.select("dl").get(0)

		assertEquals("first", list.child(0).text)
		assertTrue(list.child(1).select("pre").text.contains("foo:"))
		assertEquals("second", list.child(2).text)
		assertTrue(list.child(3).select("pre").text.contains("Hello"))
	}
}
