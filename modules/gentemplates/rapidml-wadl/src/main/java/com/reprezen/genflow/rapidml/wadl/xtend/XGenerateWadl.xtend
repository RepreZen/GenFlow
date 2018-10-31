/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.xtend

import com.google.common.collect.Iterables
import com.reprezen.genflow.api.zenmodel.ZenModelExtractOutputItem
import com.reprezen.rapidml.DataModel
import com.reprezen.rapidml.Documentable
import com.reprezen.rapidml.HttpMessageParameterLocation
import com.reprezen.rapidml.MatrixParameter
import com.reprezen.rapidml.MediaType
import com.reprezen.rapidml.MessageParameter
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.ObjectResource
import com.reprezen.rapidml.Parameter
import com.reprezen.rapidml.ReferenceElement
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.TemplateParameter
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.TypedRequest
import com.reprezen.rapidml.TypedResponse
import com.reprezen.rapidml.ZenModel
import java.util.ArrayList
import java.util.LinkedList
import java.util.List

class XGenerateWadl extends ZenModelExtractOutputItem<ResourceAPI> {

	extension WadlHelper = new WadlHelper
	extension TraceHelper traceHelper

	override String generate(ZenModel zenModel, ResourceAPI resourceAPI) {
		this.traceHelper = new TraceHelper(context, zenModel)
		'''
			«resourceAPI.generateApplicationElement(zenModel)»
		'''
	}

	def private generateApplicationElement(ResourceAPI resourceAPI, ZenModel zenModel) {
		'''
			<application xmlns="http://wadl.dev.java.net/2009/02"
					xmlns:xs="http://www.w3.org/2001/XMLSchema"
					xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
					«zenModel.generateNamespaceAdditions»
					xsi:schemaLocation="http://wadl.dev.java.net/2009/02 wadl.xsd">
				«zenModel.generateGrammar»
				«resourceAPI.generateResourcesElement»
			</application>
		'''
	}

	def private parameterLocation(HttpMessageParameterLocation type) {
		if(type == HttpMessageParameterLocation::HEADER) "header" else "query"
	}

	def protected generateResourcesElement(ResourceAPI resourceAPI) {
		'''
			<resources base="«resourceAPI.baseURI»/«resourceAPI.name»">
			«FOR resourceDef : resourceAPI.ownedResourceDefinitions»
				«resourceDef.generateResource»
			«ENDFOR»
			</resources>
			«FOR resourceDef : resourceAPI.ownedResourceDefinitions»
				«generateResourceType(resourceDef)»
			«ENDFOR»
		'''
	}

	def private generateNamespaceAdditions(ZenModel zenModel) {
		'''
			«zenModel.generateXsdNamespaceDeclarations»
			«generateAtomNamespaceDeclarations»
		'''
	}

	def private generateAtomNamespaceDeclarations() {
		'''xmlns:atom="http://www.w3.org/2005/Atom"'''
	}

	def private generateXsdNamespaceDeclarations(ZenModel zenModel) {
		'''
			«FOR interfaceDataModel : zenModel.dataModels»
				«interfaceDataModel.generateNamespaceDeclaration»
			«ENDFOR»
			«FOR resourceAPI : zenModel.resourceAPIs»
				«resourceAPI.generateNamespaceDeclaration»
			«ENDFOR»
		'''
	}

	def private generateNamespaceDeclaration(DataModel interfaceDataModel) {
	}

	def private generateNamespaceDeclaration(ResourceAPI resourceAPI) {
		'''xmlns:«resourceAPI.nsPrefix»="«resourceAPI.namespace»"'''
	}

	def private generateGrammar(ZenModel zenModel) {
		'''
			<grammars>
			    «zenModel.generateInclude»
			</grammars>
		'''
	}

	def private generateInclude(ZenModel zenModel) {
		'''
			«FOR resourceAPI : zenModel.resourceAPIs»
				<include href="«resourceAPI.xsdFilePath»"/>
			«ENDFOR»
		'''
	}

	def protected generateResource(ResourceDefinition resourceDef) {
		'''
			<resource id="«resourceDef.idInTrace»" path="«resourceDef.getURI»" type="#«resourceDef.idInTrace»Type">
			</resource>
		'''
	}

	def private generateResourceType(ResourceDefinition resourceDef) {
		'''
			<resource_type id="«resourceDef.idInTrace»Type">
			«resourceDef.generateParameters»
			«FOR method : resourceDef.methods»
				«method.generateMethod(resourceDef as ServiceDataResource)»
			«ENDFOR»
			</resource_type>
		'''
	}

	def protected generateMethod(Method method, ServiceDataResource containingDataResource) {
		'''
			<method name="«method.httpMethod»" id="«method.idInTrace»">
			    «method.generateDoc»
			    <request>
			        «method.request.generateRequestParameters»
			        «method.request.generateMediaTypes(containingDataResource)»
			    </request>
			    «FOR response : method.responses»
			    	<response«IF response.statusCode != -1» status="«response.statusCode»"«ENDIF»>
			    	    «response.generateResponseParameters»
			    	    «response.generateMediaTypes(containingDataResource)»
			    	</response>
			    «ENDFOR»
			</method>
		'''
	}

	def private generateDoc(Documentable documentable) {
		if (documentable?.documentation !== null) '''<doc>«documentable.documentation.text»</doc>'''
	}

	def private generateMediaTypes(TypedMessage message, ServiceDataResource containingDataResource) {
		// TODO use bound resource for resource-bound message, right?
		if (message.hasResourceType || message.hasDataType) {
			'''«message.generateRepresentation(containingDataResource)»'''
		}
	}

	def private generateRepresentation(TypedMessage message, ServiceDataResource containingDataResource) {
		'''
			«FOR derivedMediaType : Iterables.concat(message.mediaTypes.map[it.mediaSuperTypes].flatten, message.mediaTypes).
				toSet»
				<representation mediaType="«derivedMediaType.name»" «message.generateRepresentationElement(derivedMediaType)»>
				    «message.generateRepresentationParameters(derivedMediaType, containingDataResource)»
				</representation>
			«ENDFOR»
		'''
	}

	def private generateRepresentationElement(TypedMessage message, MediaType mediaType) {
		if (mediaType.supportsJaxb) {
			if (message.resourceType === null) {
				'''element="«getNsPrefix(message.getEContainer(ResourceAPI))»:«message.messageTypeName.toFirstLower»"'''
			} else {
				'''element="«getComplexTypeQName(message.resourceType)»"'''
			}
		}
	}

	def private generateRepresentationParameters(TypedMessage message, MediaType mediaType,
		ServiceDataResource containingDataResource) {
		if (message.resourceType === null) {
			'''
				«FOR referenceLink : message.referenceTreatments.filter(ReferenceLink)»
					«referenceLink.generateRepresentationParameter(mediaType, message.messageTypeName, containingDataResource)»
				«ENDFOR»
			'''
		} else {
			'''«message.getEContainer(ResourceDefinition).generateRepresentationParameters(mediaType)»'''
		}
	}

	def private generateRepresentationParameters(ResourceDefinition resourceDefinition, MediaType mediaType) {
		if (resourceDefinition instanceof ServiceDataResource) {
			'''«(resourceDefinition as ServiceDataResource).generateServiceDataResourceRepresentationParameters(mediaType)»'''
		}
	}

	def private generateServiceDataResourceRepresentationParameters(ServiceDataResource dataResource,
		MediaType mediaType) {
		'''
			«FOR referenceLink : dataResource.allReferenceTreatments.filter(ReferenceLink)»
				«referenceLink.generateRepresentationParameter(mediaType, referenceLink.targetResource.idInTrace+"Type", dataResource)»
			«ENDFOR»
		'''
	}

	def private generateRepresentationParameter(ReferenceLink referenceLink, MediaType mediaType, String typeName,
		ServiceDataResource containingDataResource) {
		'''
			<param name="«referenceLink.parameterName»" style="plain" 
			    «referenceLink.generateRepresentationParameterType(mediaType)» 
			    «referenceLink.generateRepresentationParameterPath(mediaType, containingDataResource)»>
			    <link resource_type="#«typeName»"
			    «IF referenceLink.relValue !== null»
			    	rel="«referenceLink.relValue»"
			    «ENDIF»
			    />
			</param>
		'''
	}

	def private generateRepresentationParameterPath(ReferenceLink referenceLink, MediaType mediaType,
		ServiceDataResource containingDataResource) {
		if (mediaType.supportsJaxb) {
			'''path="«getPath(referenceLink, containingDataResource)»"'''
		}
	}

	def private generateRepresentationParameterType(ReferenceLink referenceLink, MediaType mediaType) {
		if (mediaType.supportsJaxb) {
			'''type="«referenceLink.type»"'''
		}
	}

	def protected generateParameters(ResourceDefinition resourceDef) {
		if (resourceDef !== null) {
			if (resourceDef.getURI !== null) {
				'''
					«FOR param : resourceDef.getURI.uriParameters.filter(TemplateParameter)»
						<param id="«resourceDef.name + '_resource_' + param.name»" name="«param.name»" style="template" «param.
						generateParameterType»/>
					«ENDFOR»
					«FOR param : resourceDef.getURI.uriParameters.filter(MatrixParameter)»
						<param id="«resourceDef.name + '_resource_' + param.name»" name="«param.name»" style="matrix" «param.
						generateParameterType»/>
					«ENDFOR»
				'''
			}
		}
	}

	def private generateRequestParameters(TypedRequest request) {
		if (request !== null) {
			val method = request.containingMethod
			'''
				«FOR param : request.primitiveParameters»
					«param.generateRequestParameter(method)»
				«ENDFOR»
			'''
		}
	}

	def private generateRequestParameter(MessageParameter param, Method method) {
		'''
			<param id="«method.id + '_request_' + param.name»" name="«param.name»" style="«param.httpLocation.parameterLocation»" «param.generateParameterType»/>
		'''
	}

	def private generateResponseParameters(TypedResponse request) {
		if (request !== null) {
			val method = request.containingMethod;
			'''
				«FOR param : request.primitiveParameters»
					«param.generateResponseParameter(method)»
				«ENDFOR»            
			'''
		}
	}

	def private generateResponseParameter(MessageParameter param, Method method) {
		'''
			<param id="«method.id + '_response_' + param.name»" name="«param.name»" style="header" «param.generateParameterType»/>
		'''
	}

	def private dispatch generateParameterType(Parameter param) {
		if (param?.sourceReference !== null) {
			'''type="xs:«param.type.name»"'''
		}
	}

	def private dispatch generateParameterType(ObjectResource resource) {
		if (resource?.dataType !== null) {
			'''type="xs:«resource.dataType.paramType»"'''
		}
	}

	def private getParameterName(ReferenceLink referenceLink) {
		referenceLink.referenceElement.name.toFirstLower
	}

	def private hasResourceType(TypedMessage message) {
		message.resourceType !== null
	}

	def private hasDataType(TypedMessage message) {
		message.actualType !== null
	}

	def private getPath(ReferenceLink referenceLink, ServiceDataResource containingDataResource) {
		referenceLink.getXPath(containingDataResource)
	}

	def private getType(ReferenceLink referenceLink) {
		"xs:anyURI"
	}

	def private paramType(Structure complexType) {
		complexType.name
	}

	def getXPath(ReferenceLink referenceLink, ServiceDataResource containingDataResource) {
		"/" + referenceLink.getPathToAtomLinkWithNs(containingDataResource).join("/") + "/atom:link"
	}

	def private getPathToAtomLinkWithNs(ReferenceLink referenceLink, ServiceDataResource containingDataResource) {
		val nsPrefix = getNsPrefix(referenceLink.getEContainer(ResourceAPI))
		referenceLink.getPathToAtomLink(containingDataResource).map[nsPrefix + ":" + it]
	}

	def private Iterable<String> getPathToAtomLink(ReferenceLink referenceLink, ServiceDataResource dataResource) {
		var List<String> result = new ArrayList

		result += dataResource.rootElementName
		result += referenceLink.containmentXpath
		result += referenceLink.referenceElement.referenceXpath
		result
	}

	def private Iterable<String> getContainmentXpath(ReferenceLink referenceLink) {
		referenceLink.embedHierarchy.map[referenceElement].map[it.containmentXpath].flatten
	}

	def private Iterable<String> getReferenceXpath(ReferenceElement refProperty) {
		var LinkedList<String> result = new LinkedList<String>

		if (refProperty.isMultiValued) {
			result += refProperty.listElementName
			result += refProperty.listItemElementName
		} else {
			result += refProperty.elementName
		}
		result
	}

	def private Iterable<String> getContainmentXpath(ReferenceElement refProperty) {
		var LinkedList<String> result = new LinkedList<String>
		if (refProperty.isMultiValued) {
			result += refProperty.listElementName
			result += refProperty.listItemElementName
		} else {
			result += refProperty.elementName
		}
		result
	}
}
