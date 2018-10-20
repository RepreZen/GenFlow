/*******************************************************************************
 * i * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.swagger

import com.reprezen.genflow.common.MethodServices
import io.swagger.models.HttpMethod
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.FormParameter
import io.swagger.models.parameters.Parameter
import io.swagger.models.properties.Property
import java.util.HashMap

import static org.apache.commons.text.StringEscapeUtils.*

class SwaggerDiagramData {
	private static final StatusCodeComparator STATUS_CODE_COMPARATOR = new StatusCodeComparator
	private static final int DEFAULT_RESPONSE_CODE = -1;

	extension XtendHelper = new XtendHelper
	extension Anchors = new Anchors

	private SwaggerDiagramTypes typesProvider
	private Swagger swagger

	public new(Swagger model) {
		swagger = model
		typesProvider = new SwaggerDiagramTypes(model)
	}

	def String generateDiagramData() {
		val String result = generateJSON(swagger)
		return result
	}

	def String generateJSON(Swagger swagger) {
		'''
			{
				"ResourceAPI": {
					"objecttype": "ResourceAPI",
					"name": "«escapeEcmaScript(swagger.info?.title)»",
					"anchorId": "«htmlLink(swagger)»",
					"baseURI": "«escapeEcmaScript(swagger.host+swagger.basePath)»",
					"resources": [
					«swagger.paths.safe.entrySet.joinedMap(',',
				[ e, idx |
					generateResource(e.key, e.value, idx)
				])»
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
				«FOR entry : path.operationMap.safe.sorted.entrySet SEPARATOR ','»
					«generateMethod(path, entry.key, entry.value)»
				«ENDFOR»
				],
				"mediaTypes" : [
				]
			}
		'''
	}

	def String generateMethod(Path path, HttpMethod httpMethod, Operation operation) {
		'''
			{
				"objecttype": "Method",
				"name": "«escapeEcmaScript(operation.operationId)»",
				"anchorId": "«htmlLink(operation)»",
				"type": "«httpMethod»",
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
		val name2param = new HashMap<String, Parameter>()
		operation.parameters.safe.forEach[name2param.put(it.name, it)]
		path.parameters.safe.forEach[name2param.put(it.name, it)]

		'''
			"request": {
			    "objecttype": "Request",
			    "resource_type": "«operation.requestType»",
			    "name": "(empty request)",
			    "parameters":  [
			    «FOR next : name2param.values SEPARATOR ','»
			    	«generateRequestParameter(next)»
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
			    "required": «parameter.required»
			}
		'''
	}

	def generateResponseHeader(String headerName, Property header) {
		'''
			{
			    "objecttype": "HeaderParameter",
			    "name": "«escapeEcmaScript(headerName)»",
			    "isProperty": false,
			    "propertyId": "<undefined>",
			    "required": «header.required»
			}
		'''
	}

	def generateResponse(String code, Response response) {
		val intCode = StatusCodeComparator.safeParse(code, DEFAULT_RESPONSE_CODE)

		// the next line is according to spec but I don't think it is good 
		// val defaultPrefix = if(intCode == DEFAULT_RESPONSE_CODE) "(default response) :" else ""
		// val defaultPrefix = ""
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
		switch (parameter.in ?: "<null>") {
			case "query": return 'QueryParameter'
			case "header": return 'HeaderParameter'
			case "path": return 'RequestParameter'
			case "formData": return 'Request'
			case "body": return 'Request'
			default: 'Request'
		}
	}

	def getParameterLabel(Parameter parameter) {
		typesProvider.parameterLabel(parameter)
	}

	def String getResponseType(Response response) {
		val schema = response.responseSchema
		return typesProvider.propertyTypeLabel("Response", schema)
	}

	def String getRequestType(Operation operation) {
		var bodyParams = operation.parameters.filter(BodyParameter)
		if (!bodyParams.empty) {
			return typesProvider.computeType(bodyParams.head)
		}
		var formParams = operation.parameters.filter(FormParameter)
		if (!formParams.empty) {
			return '''Form Data'''
		}
		''''''
	}

}
