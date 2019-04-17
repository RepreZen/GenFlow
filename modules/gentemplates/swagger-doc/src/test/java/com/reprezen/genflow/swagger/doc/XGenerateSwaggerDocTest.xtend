package com.reprezen.genflow.swagger.doc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.reprezen.genflow.api.normal.openapi.ObjectType
import com.reprezen.genflow.api.normal.openapi.OpenApiNormalizer
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.junit.Test

import static org.junit.Assert.*
import com.reprezen.genflow.api.normal.openapi.Option

class XGenerateSwaggerDocTest {

	@Test
	public def void testRemoveUnwantedIndentation() {
		val content = '''
			swagger: "2.0"
			info:
			  version: 1.0.0
			  title: TaxBlaster
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
			  /taxFilings:
			    get:
			      responses:
			        200:
			          description: Successful response, with a representation of the Tax Filing.
			          schema:
			            type: object
			            description: An individual Tax Filing record.
			            properties:
			              filingID:
			                type: string
			              taxLiability:
			                type: number
			          examples:
			            application/json :
			              {
			                filingID : 1234,
			                jurisdiction : Federal,
			                year : 2015,
			                period : CALENDAR_YEAR,
			                currency : EUR,
			                grossIncome : 74832,
			                taxLiability : 15640
			              }
		'''
		val expectedPreTagContent = '''
		<pre class="remove-xtend-indent">
		{
		  &quot;filingID&quot; : 1234,
		  &quot;jurisdiction&quot; : &quot;Federal&quot;,
		  &quot;year&quot; : 2015,
		  &quot;period&quot; : &quot;CALENDAR_YEAR&quot;,
		  &quot;currency&quot; : &quot;EUR&quot;,
		  &quot;grossIncome&quot; : 74832,
		  &quot;taxLiability&quot; : 15640
		}
		</pre>'''

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
		val generated = generator.generate(model)

		assertNotNull(generated)
		assertEquals(expectedPreTagContent, generated.generatedPreTagContent)
	}

	private def getGeneratedPreTagContent(String result) {
		val regex = Pattern.compile("<pre\\s+[^>]*class=\"remove-xtend-indent\"[^>]*>.*</pre>", Pattern.DOTALL)
		val Matcher matcher = regex.matcher(result)
		if (matcher.find()) {
			return matcher.group
		}
	}

}
