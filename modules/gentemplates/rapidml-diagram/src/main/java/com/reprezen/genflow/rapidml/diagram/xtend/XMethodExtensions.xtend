/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram.xtend

import com.reprezen.genflow.common.doc.XDocHelper
import com.reprezen.genflow.common.services.MethodServices
import com.reprezen.rapidml.HttpMessageParameterLocation
import com.reprezen.rapidml.MessageParameter
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.PropertyReference
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedRequest
import com.reprezen.rapidml.TypedResponse
import java.net.URI

class XMethodExtensions {
	extension XDocHelper xDocHelper
	extension XFeatureExtensions = new XFeatureExtensions

	new(URI baseUri) {
		xDocHelper = new XDocHelper(baseUri)
	}

	def generateMethod(Method method, ServiceDataResource resource) {
		'''
			{
			    "objecttype": "Method",
			    "name": "«method.id»",
			    "anchorId": "«htmlLink(method)»",
			    "type": "«method.httpMethod»",
			    «IF method.request !== null»
			    	«method.request.generateRequest(resource)»   
			    «ENDIF»
			    "responses": [
			    «FOR response : method.responses SEPARATOR ','»
			    	«response.generateResponse(resource)»
			    «ENDFOR»
			    ]
			}
		'''
	}

	def private generateRequest(TypedRequest request, ResourceDefinition resource) {
		'''
			"request": {
			    "objecttype": "Request",
			    «IF request.resourceType !== null»
			    	"resource_type": "«request.resourceType.name»",
			    «ELSEIF request.dataType !== null»
			    	"resource_type": "«request.dataType.name»",
			    «ENDIF»
			    «IF (request.resourceType === null && request.dataType === null) && 
                	(request.parameters.empty && request.dataType === null)»
			    	"name": "(empty request)",
			    «ENDIF»
			    "parameters":  [
			    «FOR aParameter : request.parameters SEPARATOR ','»
			    	«aParameter.generateParameter(resource, 'QueryParameter')»
			    «ENDFOR»
			    ]
			},
		'''
	}

	def private generateResponse(TypedResponse response, ResourceDefinition resource) {
		'''
			{
			    "objecttype": "Response",
			    «IF response.resourceType !== null»
			    	"resource_type": "«response.resourceType.name»",
			    «ELSEIF response.dataType !== null»
			    	"resource_type": "«response.dataType.name»",
			    «ENDIF»
			
				«IF response.parameters.empty && response.dataType === null && response.statusCode == -1»
				"name": "(empty response)",
				«ENDIF»
				«IF response.statusCode != -1»
				"statusCode": "«response.generateResponseStatusCode»",
				"statusCodeGroup": "«getResponseStatusCodeGroup(response.statusCode)»",
			    «ENDIF»
			    "parameters":  [
			    «FOR aParameter : response.parameters SEPARATOR ','»
				«aParameter.generateParameter(resource, 'HeaderParameter')»
			    «ENDFOR»
			    ]
			}
		'''
	}

	def private generateParameter(MessageParameter parameter, ResourceDefinition resource, String parameterType) {
		'''
			{
			    "objecttype": "«parameter.getParameterType(parameterType)»",
			    "name": "«parameter.messageParameterName»",
			    "isProperty": «parameter.sourceReference instanceof PropertyReference»,
			    «IF parameter.sourceReference instanceof PropertyReference»
			    	"property": "«(parameter.sourceReference as PropertyReference).conceptualFeature.name»",
			    «ENDIF»
			    "propertyId": "«resource.referenceFeatureId(parameter.sourceReference)»",
			    "required": «parameter.required»
			}
		'''
	}

	def private getParameterType(MessageParameter parameter, String defaultType) {
		switch (parameter.httpLocation) {
			case HttpMessageParameterLocation.HEADER: return 'HeaderParameter'
			case HttpMessageParameterLocation.QUERY: return 'QueryParameter'
			default: return defaultType
		}
	}

	def private getMessageParameterName(MessageParameter aParameter) {
		aParameter.name + ':' + aParameter.type.name
	}

	def private generateResponseStatusCode(TypedResponse response) {
		val desc = MethodServices.getResponseStatusCodeDescription(response.statusCode)
		response.statusCode + (if(!desc.empty) ' - ' + desc else '')
	}

	def private getResponseStatusCodeGroup(Integer code) {
		switch code {
			case code >= 100 && code < 200: 'Informational'
			case code >= 200 && code < 300: 'Success'
			case code >= 300 && code < 400: 'Redirection'
			case code >= 400 && code < 500: 'Client Error'
			case code >= 500 && code < 600: 'Server Error'
			default: '<Unknown server code>'
		}
	}

}
