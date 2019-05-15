package com.reprezen.genflow.openapi3.doc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.io.Resources
import com.reprezen.genflow.api.target.GenTargetUtils
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.kaizen.oasparser.OpenApiParser
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors
import org.junit.Test

import static org.junit.Assert.*

class XGenerateOpenApi3DocTest {

	@Test
	def void testGeneration() {
		val genTarget = GenTargetUtils.load(Paths.get(Resources.getResource("Doc.gen").toURI).toFile)
		val result = genTarget.execute(Logger.getLogger("test"))
		
		val indexFiles = Files.find(new File(result.baseDirectory, "generated").toPath, 1, [path, attrs |
			path.fileName.toString.endsWith("html")
		]).collect(Collectors.toList)

		assertEquals(1, indexFiles.size)
	}

	@Test
	def void testRemoveUnwantedIndentation() {
		val content = '''
			openapi: "3.0.0"
			info:
			  version: 1.0.0
			  title: My API Spec
			  description: |
			    Example code block in Swagger-OpenAPI v2 
			    ```
			    {  
			      "filingID" : "ASDF1234",  
			      "taxpayerID" : "987-89-2345",  
			      "jurisdiction" : "Internal Revenue Service",
			      "fileDate" : "2017-04-10",
			      "isJointFiling" : false,
			      "filingStatus" : "submitted",
			      "payment" : [
			        {
			          "paymentID": "P198723",
			          "date": "2018-04-10",
			          "amount": 1500.00,
			          "currency": "USD"
			        },
			        {
			          "paymentID": "P12347",
			          "date": "2018-06-30",
			          "amount": 2231.89,
			          "currency": "USD"
			        }
			      ]
			    }
			    ```
			
			paths:
			  /resourceUrl:
			    get:
			      responses:
			        200:
			          description: Success
		'''
		val expectedPreTagContent = '''
		<pre><code>{  
		  &quot;filingID&quot; : &quot;ASDF1234&quot;,  
		  &quot;taxpayerID&quot; : &quot;987-89-2345&quot;,  
		  &quot;jurisdiction&quot; : &quot;Internal Revenue Service&quot;,
		  &quot;fileDate&quot; : &quot;2017-04-10&quot;,
		  &quot;isJointFiling&quot; : false,
		  &quot;filingStatus&quot; : &quot;submitted&quot;,
		  &quot;payment&quot; : [
		    {
		      &quot;paymentID&quot;: &quot;P198723&quot;,
		      &quot;date&quot;: &quot;2018-04-10&quot;,
		      &quot;amount&quot;: 1500.00,
		      &quot;currency&quot;: &quot;USD&quot;
		    },
		    {
		      &quot;paymentID&quot;: &quot;P12347&quot;,
		      &quot;date&quot;: &quot;2018-06-30&quot;,
		      &quot;amount&quot;: 2231.89,
		      &quot;currency&quot;: &quot;USD&quot;
		    }
		  ]
		}
		</code></pre>'''

		val mapper = new ObjectMapper(new YAMLFactory)
		val model = new OpenApiParser().parse(mapper.readTree(
			content
		), new URL("file://test.yaml")) as OpenApi3

		val generator = new XGenerateOpenApi3Doc()
		generator.init(new FakeGenTemplateContext)
		val generated = generator.generate(model)

		assertNotNull(generated)
		assertEquals(expectedPreTagContent, generated.generatedPreTagContent)
	}

	private def getGeneratedPreTagContent(String result) {
		val regex = Pattern.compile("<pre(\\s+.*)?>.*</pre>", Pattern.DOTALL)
		val Matcher matcher = regex.matcher(result)
		if (matcher.find()) {
			return matcher.group
		}
	}

}
