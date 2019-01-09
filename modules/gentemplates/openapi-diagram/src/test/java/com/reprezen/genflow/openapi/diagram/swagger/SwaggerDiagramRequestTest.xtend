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
import com.reprezen.genflow.openapi.diagram.swagger.SwaggerDiagramData
import io.swagger.parser.Swagger20Parser
import org.junit.Test

import static org.junit.Assert.*

class SwaggerDiagramRequestTest {

	def String fixtureParameters(String paramIn) '''
	swagger: "2.0"
	info:
	  version: 1.0.0
	  title: Swagger Petstore
	  license:
	    name: MIT
	    url: http://dsds
	host: petstore.swagger.io
	basePath: /v1
	schemes:
	  - http
	consumes:
	  - application/json
	produces:
	  - application/json
	paths:
	  /pets/{id}:
	    post:
	      operationId: createPets
	      parameters:
	        - name: id
	          in: «paramIn»
	          required: true
	          type: string
	      responses:
	        201:
	          description: Null response
	definitions:
	  Pet:
	    type: object
	    required:
	      - id
	    properties:
	      id:
	        type: integer
	'''
	
	def String fixtureBodyParameter() '''
	swagger: "2.0"
	info:
	  version: 1.0.0
	  title: Swagger Petstore
	  license:
	    name: MIT
	    url: http://dsds
	host: petstore.swagger.io
	basePath: /v1
	schemes:
	  - http
	consumes:
	  - application/json
	produces:
	  - application/json
	paths:
	  /pets/{id}:
	    post:
	      operationId: createPets
	      parameters:
	        - name: id
	          in: body
	          required: true
	          schema:
	            type: string
	      responses:
	        201:
	          description: Null response
	definitions:
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
		val model = new Swagger20Parser().parse(fixtureParameters("path"))
		val data = new ObjectMapper().readTree(new SwaggerDiagramData(model).generateDiagramData)

		val request = data.get("ResourceAPI").get("resources").get(0).get("methods").get(0).get("request")
		assertNotNull(request)
		assertEquals(1, request.get("parameters").size)
		
		val param = request.get("parameters").get(0)
		assertEquals("RequestParameter", param.get("objecttype").asText)
		assertEquals("id : string", param.get("name").asText)
	}

	@Test
	def void testRequestParameterInQuery() {
		val model = new Swagger20Parser().parse(fixtureParameters("query"))
		val data = new ObjectMapper().readTree(new SwaggerDiagramData(model).generateDiagramData)

		val request = data.get("ResourceAPI").get("resources").get(0).get("methods").get(0).get("request")
		assertNotNull(request)
		assertEquals(1, request.get("parameters").size)
		
		val param = request.get("parameters").get(0)
		assertEquals("QueryParameter", param.get("objecttype").asText)
		assertEquals("id : string", param.get("name").asText)
	}

	@Test
	def void testRequestParameterInBody() {
		val model = new Swagger20Parser().parse(fixtureBodyParameter())
		val data = new ObjectMapper().readTree(new SwaggerDiagramData(model).generateDiagramData)

		val request = data.get("ResourceAPI").get("resources").get(0).get("methods").get(0).get("request")
		assertNotNull(request)
		assertEquals(1, request.get("parameters").size)

		val param = request.get("parameters").get(0)
		assertEquals("Request", param.get("objecttype").asText)
		assertEquals("id : string", param.get("name").asText)
	}
}