/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.openapi3

import com.reprezen.genflow.common.services.MethodServices
import com.reprezen.genflow.openapi.diagram.swagger.StatusCodeComparator
import com.reprezen.genflow.openapi.diagram.swagger.XtendHelper
import com.reprezen.kaizen.oasparser.model3.Header
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Parameter
import com.reprezen.kaizen.oasparser.model3.Path
import com.reprezen.kaizen.oasparser.model3.RequestBody
import com.reprezen.kaizen.oasparser.model3.Response
import com.reprezen.kaizen.oasparser.model3.Schema

import static org.apache.commons.text.StringEscapeUtils.*

class OpenApi3DiagramData {
	static final StatusCodeComparator STATUS_CODE_COMPARATOR = new StatusCodeComparator
	static final int DEFAULT_RESPONSE_CODE = -1;

	extension XtendHelper = new XtendHelper
	extension OpenApi3Anchors = new OpenApi3Anchors

	OpenApi3DiagramTypes typesProvider
	OpenApi3 swagger

	new(OpenApi3 model) {
		swagger = model
		typesProvider = new OpenApi3DiagramTypes()
	}

	def String generateDiagramData() {
		val String result = generateJSON(swagger)
		return result
	}

	def String generateJSON(OpenApi3 swagger) {
		swagger.servers
		'''
			{
				"ResourceAPI": {
					"objecttype": "ResourceAPI",
					"name": "«escapeEcmaScript(swagger.info?.title)»",
					"anchorId": "«htmlLink(swagger)»",
«««					"baseURI": "«escapeEcmaScript(swagger.info.host+swagger.basePath)»",
					"resources": [
					«swagger.paths.safe.entrySet.joinedMap(',', [ e, idx | generateResource(e.key, e.value, idx) ])»
					]
				}
			}
		'''
	}

	def String generateResource(String uri, Path path, int pathIdx) {
		'''
			{
			    "objecttype": "ObjectResource",
			    "name": "«uri»",
			    "anchorId": "«htmlLink(path)»",
			    "id": "«uri»",
			    "URI": {
					"name": [ 
					{
				"objecttype": "URISegment", 
				"label": "", 
				"id": "«uri».URI.«pathIdx»"
				}
				],
				"parameters": [
				]
				},
				"methods": [
				«FOR entry : path.operations.safe.sorted.entrySet SEPARATOR ','»
					«generateMethod(path, entry.key, entry.value)»
				«ENDFOR»
				],
				"mediaTypes" : [
				]
			}
		'''
	}

	def String generateMethod(Path path, String httpMethod, Operation operation) {
		'''
			{
				"objecttype": "Method",
				"name": "«escapeEcmaScript(operation.operationId)»",
				"anchorId": "«htmlLink(operation)»",
				"type": "«httpMethod.toUpperCase»",
				«generateRequestWithTrailingComma(path, operation)»
				"responses": [
				«FOR entry : operation.responses.safe.sortedWith(STATUS_CODE_COMPARATOR).entrySet SEPARATOR ','»
					«generateResponse(entry.key, entry.value)»
				«ENDFOR»
				]
			}
		'''
	}

	def generateRequestWithTrailingComma(Path path, Operation operation) {
		val values = newLinkedHashMap()

		// Request body should be display first, 
		// we only support displaying a single request body for now.
		operation.requestBody.contentMediaTypes.keySet.take(1).forEach[k | 
			values.put(k, operation.requestBody.contentMediaTypes.get(k).schema)
		]

		// merge parameters from path level and operation level
		path.parameters.safe.andAlso(operation.parameters.safe).forEach[
			values.put(it.name, it)
		]

		'''
			"request": {
			    "objecttype": "Request",
			    "resource_type": "«operation.requestType»",
			    "name": "(empty request)",
			    "parameters":  [
			    «FOR next : values.values SEPARATOR ','»
			    	«IF next instanceof Parameter»
			    		«generateRequestParameter(next as Parameter)»
			    	«ELSE»
			    		«generateRequestParameter(operation.requestBody, next as Schema)»
			    	«ENDIF»
			    «ENDFOR»
			    ]
			},
		'''
	}

	def generateRequestParameter(Parameter parameter) {
		'''
			{
			    "objecttype": "«escapeEcmaScript(parameter.parameterObjectType)»",
			    "name": "«escapeEcmaScript(parameter.parameterLabel)»",
			    "isProperty": false,
			    "propertyId": "<undefined>",
			    "required": «parameter.isRequired()»
			}
		'''
	}

	def generateRequestParameter(RequestBody request, Schema schema) {
		'''
			{
			    "objecttype": "Request",
			    "name": "«escapeEcmaScript(schema.schemaLabel)»",
			    "isProperty": false,
			    "propertyId": "<undefined>",
			    "required": «request.isRequired()»
			}
		'''
	}

	def generateResponseHeader(String headerName, Header header) {
		'''
			{
			    "objecttype": "HeaderParameter",
			    "name": "«escapeEcmaScript(headerName)»",
			    "isProperty": false,
			    "propertyId": "<undefined>",
			    "required": «header.isRequired»
			}
		'''
	}

	def generateResponse(String code, Response response) {
		val intCode = StatusCodeComparator.safeParse(code, DEFAULT_RESPONSE_CODE)

		'''
			{
			    "objecttype": "Response",
			    "resource_type": "«response.responseType»",
			    "statusCode": "«intCode.responseStatusCode»",
			    "statusCodeGroup": "«getResponseStatusCodeGroup(intCode)»",
			    "parameters":  [
			    «FOR entry : response.headers.safe.sorted.entrySet SEPARATOR ','»
			    	«generateResponseHeader(entry.key, entry.value)»
			    «ENDFOR»
			    ]
			}
		'''
	}

	def static private getResponseStatusCode(int statusCode) {
		if (statusCode == DEFAULT_RESPONSE_CODE) {
			return "(default response)"
		}
		val desc = MethodServices.getResponseStatusCodeDescription(statusCode)
		statusCode + (if(!desc.empty) ' - ' + desc else '')
	}

	def static private getResponseStatusCodeGroup(int code) {

		// this string constants are use in JS: 
		// @see: chart-flow-jquery.js: function image(type, obj)
		switch code {
			case DEFAULT_RESPONSE_CODE: 'Default'
			case code >= 100 && code < 200: 'Informational'
			case code >= 200 && code < 300: 'Success'
			case code >= 300 && code < 400: 'Redirection'
			case code >= 400 && code < 500: 'Client Error'
			case code >= 500 && code < 600: 'Server Error'
			default: '<Unknown server code>'
		}
	}

	def getParameterObjectType(Parameter parameter) {
		switch (parameter.in?.toLowerCase ?: "<null>") {
			case "query": return 'QueryParameter'
			case "header": return 'HeaderParameter'
			case "path": return 'RequestParameter'
		}
		return 'RequestParameter'
	}

	def getParameterLabel(Parameter parameter) {
		typesProvider.parameterLabel(parameter)
	}
	
	def String getSchemaLabel(Schema schema) {
		typesProvider.propertyTypeLabel("Request", schema)
	}

	def String getResponseType(Response response) {
		val schema = response.contentMediaTypes.values.head?.schema
		return typesProvider.propertyTypeLabel("Response", schema)
	}

	def String getRequestType(Operation operation) {
		if (operation.requestBody !== null) {
			return typesProvider.computeType(operation.requestBody)
		}
		''''''
	}

}
