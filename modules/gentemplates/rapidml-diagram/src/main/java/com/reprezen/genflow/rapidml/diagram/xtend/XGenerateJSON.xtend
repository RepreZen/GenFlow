/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram.xtend

import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.api.zenmodel.ZenModelOutputItem
import com.reprezen.genflow.common.doc.XDocHelper
import com.reprezen.genflow.common.services.ResourceSorterServices
import com.reprezen.rapidml.CollectionParameter
import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.MatrixParameter
import com.reprezen.rapidml.ObjectResource
import com.reprezen.rapidml.Parameter
import com.reprezen.rapidml.PrimitiveTypeSourceReference
import com.reprezen.rapidml.PropertyReference
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TemplateParameter
import com.reprezen.rapidml.URISegment
import com.reprezen.rapidml.URISegmentWithParameter
import com.reprezen.rapidml.ZenModel
import java.util.Collections
import java.util.List

class XGenerateJSON extends ZenModelOutputItem {
	extension XDocHelper xDocHelper
	extension XDataTypeExtensions xDataTypeExtensions
	extension XFeatureExtensions = new XFeatureExtensions
	extension XMethodExtensions xMethodExtensions
	
	override init(IGenTemplateContext context) {
		super.init(context)
		val baseUri = context.primarySource?.inputFile?.toURI 
		xDocHelper = new XDocHelper(baseUri)
		xDataTypeExtensions = new XDataTypeExtensions(baseUri)
		xMethodExtensions = new XMethodExtensions(baseUri)
	}

	override generate(ZenModel model) {
		'''
			var data = «generateJSON(model)»;
			function getJSON() {
			    return data;
			}
		'''
	}

	def String generateJSON(ZenModel model) {
		'''
			{
			«FOR resourceAPI : model.resourceAPIs»
				"ResourceAPI": {
				      "objecttype": "ResourceAPI",
				      "name": "«resourceAPI.name»",
				      "anchorId": "«htmlLink(model)»",
				      "baseURI": "«resourceAPI.baseURI»",
				      "resources": [
				      «FOR aResource : ResourceSorterServices::sort(
				resourceAPI.ownedResourceDefinitions.filter(ServiceDataResource).toList) SEPARATOR ','»
				      	«generateResource(aResource)» 
				      «ENDFOR»
				      ]
				}
			«ENDFOR»
			}
		'''
	}

	def private generateResource(ServiceDataResource aResource) {
		'''
			{
			    "objecttype": "«aResource.resourceType»",
			    "name": "«aResource.name»",
			    "anchorId": "«htmlLink(aResource)»",
			    "id": "«aResource.name»",
			    "URI": {
			        "name": [
			            «IF aResource.getURI !== null»
			            	«FOR segment : aResource.getURI.segments SEPARATOR ','»
			            		{ "objecttype": "URISegment", 
			            		  "label": "/«segment.label»", 
			            		  "id": "«aResource.getURISegmentId(segment)»"}
			            	«ENDFOR»
			            	«var matrixParams = getMatrixParameters(aResource)»
			            	«IF !aResource.getURI.segments.empty && !matrixParams.empty»,«ENDIF»
			            	«generateUriSegments(matrixParams, aResource)»
			            	«val params = getCollectionParameters(aResource)»
			            	«IF (!getMatrixParameters(aResource).empty || !aResource.getURI.segments.empty) &&
				!params.empty»,«ENDIF»
			            	«generateUriSegments(params, aResource)»
			            «ENDIF»
			        ],
			    "parameters": [
			     «IF aResource.getURI !== null»
			     	«FOR aParameter : aResource.getURI.uriParameters SEPARATOR ','»
			     		«aParameter.generateUriParameter(aResource)»
			     	«ENDFOR»     
			     	«val parameters = getCollectionParameters(aResource)»
			     	«IF !aResource.getURI.uriParameters.empty && !parameters.empty», «ENDIF»
			     	«FOR aParameter : parameters SEPARATOR ','»
			     		«aParameter.generateUriParameter(aResource)»     
			     	«ENDFOR»
			    «ENDIF»
			    ]},
			    «IF aResource instanceof ObjectResource»
			    	«aResource.dataType.generateDataType(aResource)»
			    «ELSE»
			    	«(aResource as CollectionResource).generateCollectionDataType»
			    «ENDIF»
			    ,
			    "methods": [
			    «FOR aMethod : aResource.methods SEPARATOR ','»
			    	«aMethod.generateMethod(aResource)»
			    «ENDFOR»
			    ],
			    "mediaTypes": [
			    «FOR aMediaType : aResource.mediaTypes SEPARATOR ','»
			    	"«aMediaType.name»"
			    «ENDFOR»
			    ]
			}
		'''
	}

	def private generateUriSegments(Iterable<? extends Parameter> params, ServiceDataResource aResource) {
		'''
			«FOR segment : params SEPARATOR ','»
				{ "objecttype": "URISegment", 
				  "label": "«getUriSegmentPrefix(segment, params)»", 
				  "id": "«aResource.getURISegmentId(segment)»"}
			«ENDFOR»
		'''
	}

	def private generateUriParameter(Parameter aParameter, ServiceDataResource aResource) {
		'''
			{
			    "objecttype": "«aParameter.objectType»",
			    "name": "«aParameter.name»",
			    "anchorId": "«htmlLink(aParameter)»",
			    "uriFragment": "«getURISegmentId(aResource, aParameter)»",
			    "required": «aParameter.required»,
			    "isProperty": «aParameter.sourceReference instanceof PropertyReference»,
			    «IF aParameter.sourceReference instanceof PrimitiveTypeSourceReference»
			    	"type": "«aParameter.type.name»",
			    «ENDIF»
			    "propertyId": "«aResource.referenceFeatureId(aParameter.sourceReference)»"
			}
		'''
	}

	def private String getResourceType(ServiceDataResource aResource) {
		if(aResource instanceof CollectionResource) 'CollectionResource' else 'ObjectResource'
	}

	def dispatch String getObjectType(Parameter parameter) {
		'<unsupported parameter type>'
	}

	def dispatch String getObjectType(TemplateParameter parameter) {
		'TemplateParameter'
	}

	def dispatch String getObjectType(MatrixParameter parameter) {
		'MatrixParameter'
	}

	def dispatch String getObjectType(CollectionParameter parameter) {
		'CollectionParameter'
	}

	def private String getLabel(URISegment aSegment) {
		if(aSegment instanceof URISegmentWithParameter) '{' + aSegment.name + '}' else aSegment.name
	}

	def private List<MatrixParameter> getMatrixParameters(ServiceDataResource aResource) {
		if(aResource.getURI !== null) aResource.getURI.uriParameters.filter(MatrixParameter).toList else Collections::emptyList
	}

	def private getCollectionParameters(ServiceDataResource aResource) {
		if(aResource instanceof CollectionResource) (aResource as CollectionResource).collectionParameters else Collections::
			emptyList
	}

	def private String getUriSegmentPrefix(Parameter param, Iterable<? extends Parameter> params) {
		(if (param instanceof MatrixParameter)
			'@'
		else if (param instanceof CollectionParameter)
			'*'
		else
			'<unsupported parameter>') + param.name + if(params.last !== param) ', ' else ''
	}

	def dispatch private String getURISegmentId(ServiceDataResource aResource, URISegment segment) {
		aResource.name + '.URI.' + (aResource.getURI.segments.indexOf(segment) + 1)
	}

	def dispatch private String getURISegmentId(ServiceDataResource aResource, Parameter param) {
		'<unsupported parameter type>'
	}

	def dispatch private String getURISegmentId(ServiceDataResource aResource, TemplateParameter param) {
		aResource.getURISegmentId(param.uriSegment)
	}

	def dispatch private String getURISegmentId(ServiceDataResource aResource, MatrixParameter param) {
		aResource.name + '.URI.' + (aResource.getURI.segments.size + getMatrixParameters(aResource).indexOf(param) + 1)
	}

	def dispatch private String getURISegmentId(ServiceDataResource aResource, CollectionParameter param) {
		aResource.name + '.URI.' +
			(aResource.getURI.segments.size + getMatrixParameters(aResource).size +
				getCollectionParameters(aResource).indexOf(param) + 1)
	}
}
