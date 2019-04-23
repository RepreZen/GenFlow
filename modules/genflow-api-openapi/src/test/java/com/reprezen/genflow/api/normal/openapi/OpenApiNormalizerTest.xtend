package com.reprezen.genflow.api.normal.openapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.net.URL
import org.junit.Test

import static com.reprezen.genflow.api.normal.openapi.ObjectType.OPENAPI3_MODEL_VERSION
import static org.junit.Assert.*

class OpenApiNormalizerTest {

	private def parse(String content) {
		val mapper = new ObjectMapper(new YAMLFactory)
		val tree = mapper.readTree(content)

		new OpenApiNormalizer(
			OPENAPI3_MODEL_VERSION,
			Option.DOC_DEFAULT_OPTIONS
		).of(tree).normalizeToKaizen(new URL("file://test.yaml")) as OpenApi3
	}

	@Test
	def void testRetainSecuritySchemesInDocView() {
		val content = '''
			openapi: 3.0.0
			info:
			  title: ''
			  version: ''
			paths:
			  /entry:
			    get:
			      responses:
			        '200':
			          description: OK
			security:
			  - oAuth2ClientCredentials:
			     - auth
			components:
			  schemas:
			    Foo:
			      type: object
			  securitySchemes:
			    oAuth2ClientCredentials:
			      type: oauth2
			      flows: 
			        clientCredentials:
			          tokenUrl: '/oauth2/token'
			          scopes:
			            auth: auth
		'''

		val result = parse(content)

		assertTrue(result.isValid)

		assertNotNull(result.securitySchemes.get("oAuth2ClientCredentials"))
		assertEquals(1, result.securitySchemes.size)
		assertEquals(1, result.securityRequirements.size)
		assertNotNull(result.securityRequirements.get(0).getRequirement("oAuth2ClientCredentials"))
	}

	@Test
	def void testRetainSecuritySchemesInPath() {
		val content = '''
			openapi: 3.0.0
			info:
			  title: ''
			  version: ''
			paths:
			  /entry:
			    get:
			      security:
			        - oAuth2ClientCredentials:
			          - auth
			      responses:
			        '200':
			          description: OK
			components:
			  schemas:
			    Foo:
			      type: object
			  securitySchemes:
			    oAuth2ClientCredentials:
			      type: oauth2
			      flows: 
			        clientCredentials:
			          tokenUrl: '/oauth2/token'
			          scopes:
			            auth: auth
		'''

		val result = parse(content)

		assertTrue(result.isValid)
		assertNotNull(result.securitySchemes.get("oAuth2ClientCredentials"))
		assertEquals(1, result.securitySchemes.size)
		assertEquals(0, result.securityRequirements.size)

		assertNotNull(
			result.getPath("/entry").getOperation("get").getSecurityRequirement(0).getRequirement(
				"oAuth2ClientCredentials"))
	}
}
