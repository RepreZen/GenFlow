/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.swagger

import com.fasterxml.jackson.databind.ObjectMapper
import com.reprezen.genflow.openapi.diagram.openapi3.OpenApi3DiagramData
import com.reprezen.kaizen.oasparser.OpenApiParser
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.net.URL
import org.junit.Test

import static org.junit.Assert.*

class OpenApiDiagramRequestTest {

	def String fixtureParameters(String paramIn) '''
		openapi: "3.0.0"
		info:
		  version: 1.0.0
		  title: Swagger Petstore
		  license:
		    name: MIT
		    url: hello
		servers:
		  - url: http://petstore.swagger.io/v1
		paths:
		  /pets/{id}:
		    post: 
		      operationId: createPets
		      parameters:
		        - name: id
		          in: «paramIn»
		          schema:
		            type: string
		      responses:
		        201:
		          description: Null response
		components:
		  schemas:
		    Pet:
		      type: object
		      required:
		        - id
		      properties:
		        id:
		          type: integer
	'''

	def String fixtureRequestBody() '''
		openapi: "3.0.0"
		info:
		  version: 1.0.0
		  title: Swagger Petstore
		  license:
		    name: MIT
		    url: hello
		servers:
		  - url: http://petstore.swagger.io/v1
		paths:
		  /pets/{id}:
		    post: 
		      operationId: createPets
		      requestBody:
		        content:
		          application/json:
		            schema:
		              $ref: "#/components/schemas/Pet"
		          application/xml:
		            schema:
		              type: string
		      responses:
		        201:
		          description: Null response
		components:
		  schemas:
		    Pet:
		      type: object
		      required:
		        - id
		      properties:
		        id:
		          type: integer
	'''

	@Test
	def void testRequestParameterInPath() {
		val model = new OpenApiParser().parse(fixtureParameters("path"), new URL("file://test.yaml")) as OpenApi3
		val data = new ObjectMapper().readTree(new OpenApi3DiagramData(model).generateDiagramData)

		val request = data.get("ResourceAPI").get("resources").get(0).get("methods").get(0).get("request")
		assertNotNull(request)
		assertEquals(1, request.get("parameters").size)
		
		val param = request.get("parameters").get(0)
		assertEquals("RequestParameter", param.get("objecttype").asText)
		assertEquals("id : string", param.get("name").asText)
	}

	@Test
	def void testRequestParameterInQuery() {
		val model = new OpenApiParser().parse(fixtureParameters("query"), new URL("file://test.yaml")) as OpenApi3
		val data = new ObjectMapper().readTree(new OpenApi3DiagramData(model).generateDiagramData)

		val request = data.get("ResourceAPI").get("resources").get(0).get("methods").get(0).get("request")
		assertNotNull(request)
		assertEquals(1, request.get("parameters").size)
		
		val param = request.get("parameters").get(0)
		assertEquals("QueryParameter", param.get("objecttype").asText)
		assertEquals("id : string", param.get("name").asText)
	}

	@Test
	def void testRequestBody() {
		val model = new OpenApiParser().parse(fixtureRequestBody(), new URL("file://test.yaml")) as OpenApi3
		val data = new ObjectMapper().readTree(new OpenApi3DiagramData(model).generateDiagramData)

		val request = data.get("ResourceAPI").get("resources").get(0).get("methods").get(0).get("request")
		assertNotNull(request)
		assertEquals(1, request.get("parameters").size)

		val param = request.get("parameters").get(0)
		assertEquals("Request", param.get("objecttype").asText)
		assertEquals("Pet", param.get("name").asText)
	}

}