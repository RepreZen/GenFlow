/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc.xtend

import com.reprezen.genflow.api.zenmodel.util.CommonServices
import com.reprezen.genflow.common.doc.XDocHelper
import com.reprezen.genflow.common.services.DocServices
import com.reprezen.genflow.common.xtend.XDataTypeExtensions
import com.reprezen.genflow.common.xtend.XImportHelper
import com.reprezen.genflow.common.xtend.XParameterHelper
import com.reprezen.rapidml.CollectionRealizationEnum
import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.Enumeration
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.HasSecurityValue
import com.reprezen.rapidml.MediaType
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.ObjectRealization
import com.reprezen.rapidml.Parameter
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.RealizationContainer
import com.reprezen.rapidml.ReferenceEmbed
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.TypedRequest
import com.reprezen.rapidml.TypedResponse
import com.reprezen.rapidml.UserDefinedType
import com.reprezen.rapidml.ZenModel
import com.reprezen.rapidml.realization.processor.CycleDetector
import com.reprezen.rapidml.util.InheritanceUtils
import com.reprezen.rapidml.util.TagUtils
import java.util.Collections
import java.util.List

class XGenerateInterfaces {
	extension XDocHelper docHelper
	val parameterHelper = new XParameterHelper
	val docServices = new DocServices
	val commonServices = new CommonServices
	val XImportHelper importHelper
	var boolean isLiveView = false

	new(XImportHelper importHelper, XDocHelper docHelper) {
		this.importHelper = importHelper
		this.docHelper = docHelper
	}

	def generateInterfaces(ZenModel zenModel) {
		'''
			«FOR iface : zenModel.resourceAPIs»
				«generateInterface(iface, zenModel)»
			«ENDFOR»
		'''
	}

	def private generateInterface(ResourceAPI resourceAPI, ZenModel zenModel) {
		val alias = importHelper.getAlias(resourceAPI)
		val aliasNote = if (alias !== null) ''' [as&nbsp;<em>«alias»</em>]'''
		'''
			<a class="anchor"></a>
			<div class="panel panel-primary" id="«resourceAPI.htmlLink»">
			  <div class="panel-heading">
			    <h3 class="panel-title"><strong>«resourceAPI.nameOrTitle»</strong> <small>(«resourceAPI.baseURI»)</small></h3>
			    <p>«importHelper.getModelFullQualifiedName(resourceAPI)»«aliasNote»</p>
			  </div>
			  <div class="panel-body restful-interface">
			    «resourceAPI.generateDocItem»
			    «generateSecuritySchemeUsage(resourceAPI)»
			    «FOR resource : resourceAPI.ownedResourceDefinitions»
			    	«generateResource(resource as ServiceDataResource)»
			    «ENDFOR»
			  </div>
			</div>
		'''
	}

	def private generateResource(ServiceDataResource resource) {
		'''
			<a class="anchor" id="«resource.htmlLink»" data-zenname="«resource.name»"></a>
			<div class="panel panel-default">
			  <div class="panel-heading">
			    <h3 class="panel-title">
			      «IF resource instanceof CollectionResource»
			      	<span class="glyphicon glyphicon-th-list"></span>
			      «ELSE»
			      	<span class="glyphicon glyphicon-folder-close"></span>
			      «ENDIF»
			      «resource.name» <code>«resource.URI»</code> 
			      «IF resource.dataType !== null»
			      	<span class="glyphicon glyphicon-arrow-right"></span> <code>«importHelper.
                getQualifiedName(resource.dataType)»</code>
			      «ENDIF»
			      «IF resource.^default»
			      	<span class="label label-default">Default</span>
			      «ENDIF»
			    </h3>
			  </div>
			  <div class="panel-body object-resource">
			  «resource.generateDocItem»
			  <h4>Resource Properties</h4>
			  <table class="table table-condensed">
			  «generateMediaTypes(resource.mediaTypes)»
			  «generateLinkDescriptors(resource)»
			  </table>
			  
			  «generateSecuritySchemeUsage(resource)»
			
			  «IF resource.URI !== null && !resource.URI.uriParameters.empty»
			  	<h4>Parameters</h4>
			  	«generateParams(resource.URI.uriParameters)»
			  «ENDIF»
			
			  «IF resource instanceof CollectionResource»
			  	
			  	  «IF !(resource as CollectionResource).collectionParameters.empty»
			  	  	<h4>Collection Parameters</h4>
			  	  	«generateParams((resource as CollectionResource).collectionParameters)»
			  	  «ENDIF»
			  	  
			  	  «IF (resource as CollectionResource).resourceRealizationKind ==
                CollectionRealizationEnum::REFERENCE_LINK_LIST»
			  	  	<h4>Data Properties</h4>
			  	  	<table class="table table-condensed">
			  	  	   <tr>
			  	  	     <td>«resource.dataType.name»</td>
			  	  	     <td>
			  	  	       «val defResource = resource.referenceLinks.get(0).targetResource as ServiceDataResource»
			  	  	       <a href="#«defResource.htmlLink»">«defResource.name»*</a>
			  	  	     </td>
			  	  	   </tr>
			  	  	       «IF defResource.defaultLinkDescriptor !== null»
			  	  	       	«FOR feature : defResource.defaultLinkDescriptor.allIncludedProperties.map[baseProperty]»
			  	  	       		«generateReferenceTreatmentPropertyRow(feature, 1, false)»
			  	  	       	«ENDFOR»
			  	  	       «ENDIF»
			  	  	</table>
			  	  «ELSE»
			  	  	«generateResourceDataType(resource)»
			  	  «ENDIF»
			  «ELSE»
			  	«generateResourceDataType(resource)»
			  «ENDIF»
			
			  «FOR method : resource.methods»
			  	«generateMethod(method)»
			  «ENDFOR»
			  </div>
			</div>
		'''
	}

	def private generateLinkDescriptors(ServiceDataResource resource) {
		'''
			«IF !resource.definedLinkDescriptors.empty»
				<tr>
				  <th>Link Descriptors</th>
				  <td>
				  «FOR link : resource.definedLinkDescriptors SEPARATOR '<br /> '» «link.name»
				  					                  («FOR feature : link.allIncludedProperties.map[baseProperty] SEPARATOR ', '»<code>«feature.name»</code>«ENDFOR»)
				  					                  «IF link.^default»
				  					                  	<span class="label label-default">Default</span>
				  					                  «ENDIF»
				  «ENDFOR»
				  </td>
				</tr>
			«ENDIF»
		'''
	}

	def private generateParams(List<? extends Parameter> params) {
		'''
			<table class="table table-condensed">
			    <tr>
			      <th>Name</th>
			      <th>Documentation</th>
			      <th>Default</th>
			      <th>Property</th>
			      <th>Type</th>
			    </tr>
			    «FOR param : params»
			    	<tr>
			    	  <td>
			    	    «param.name»
			    	    «IF !param.required»(<em>optional</em>)«ENDIF»
			    	  </td>
			    	  <td>«param?.generateDoc»</td>
			    	  <td>«param.^default»</td>
			    	  <td>«parameterHelper.paramName(param.sourceReference)»</td>
			    	  <td>«parameterHelper.paramType(param.sourceReference, importHelper)»</td>
			    	</tr>
			    «ENDFOR»
			</table>
		'''
	}

	def private generateRealizationTemplateDetails(RealizationContainer resource) {
		if (!isLiveView)
			return ''''''

		val templateNameTag = TagUtils.getTagWithName(resource, TagUtils.REALIZATION_TEMPLATE_NAME)
		val isInline = !resource.isWithDefaultRealization
		val realizationName = if (isInline)
				"(inline)"
			else
				resource.realizationName + if(resource instanceof CollectionResource) " [*]" else ""

		'''
			«IF templateNameTag.isPresent»
				Realization: «realizationName»
				«IF !isInline»
					<a href="#«resource.htmlLink»-template" data-toggle="collapse">
						<span id="«resource.htmlLink»-template-controller" data-toggle="tooltip" 
							data-hidden-title="View Property Details" data-visible-title="Hide Property Details" 
							class="glyphicon glyphicon-collapse-down">
						</span>
					</a>
					<div id="«resource.htmlLink»-template" class="well well-sm collapse" data-controller="#«resource.htmlLink»-template-controller" style="margin:0">
						<table class="table" style="margin:0">
							<tr><th>Realization Name</th><td>«realizationName»</td></tr>
							<tr><th>Realization Template</th><td>«templateNameTag.get.value»</td></tr>
							<tr><th>Data Structure</th><td><a href="#«resource.actualType.htmlLink»">«resource.actualType.name»</a></td></tr>
						</table>
					</div>
				«ENDIF»
			«ENDIF»
		'''
	}

	def private generateResourceDataType(ServiceDataResource resource, String headerMessage) {
		'''
			<h4>Data Properties</h4>
			«resource.generateRealizationTemplateDetails»
			«headerMessage»
			<table class="table table-condensed">
			    <tr>
			        <th>Name</th>
			        <th>Type</th>
			        <th>Documentation</th>
			    </tr>
			
			«FOR aFeature : resource.dataType.ownedFeatures.filter[e|!hasReferenceTreatment(resource, e)]»
				«IF resource.isIncluded(aFeature)»
					«generateIncludedPropertyRow(getIncludedProperty(resource, aFeature))»
				«ENDIF»
			«ENDFOR»
			«generateReferenceTreatments(resource)»
			</table>
		'''
	}

	def private generateResourceDataType(ServiceDataResource resource) {
		generateResourceDataType(resource, "")
	}

	def private generateMessageName(TypedMessage message) {
		'''
			«IF message.resourceType !== null»
				<span class="glyphicon glyphicon-chevron-right"></span>
				<a href="#«message.resourceType.htmlLink»">«message.resourceType.name»</a>
			«ELSEIF message.actualType !== null»
				<span class="glyphicon glyphicon-chevron-right"></span>
				<a href="#«message.actualType.htmlLink»">«message.actualType.name»</a>
			«ENDIF»
		'''
	}

	def private generateMessageDataType(TypedMessage message) {
		'''
			«IF !message.referenceTreatments.empty ||
                !message.actualType.ownedFeatures.filter[e|!hasReferenceTreatment(message, e) && message.isIncluded(e)].
                    empty»
				<h4>Data Properties</h4>
				«message.generateRealizationTemplateDetails»
				<table class="table table-condensed">
				    <tr>
				        <th>Name</th>
				        <th>Type</th>
				        <th>Documentation</th>
				    </tr>
				
				«FOR aFeature : message.actualType.ownedFeatures.filter[e|!hasReferenceTreatment(message, e)]»
					«IF message.isIncluded(aFeature)»
						«generateIncludedPropertyRow(getIncludedProperty(message, aFeature))»
					«ENDIF»
				«ENDFOR»
				«generateReferenceTreatments(message)»
				</table>
			«ENDIF»
		'''
	}

	def private generateMethod(Method method) {
		'''
			<a class="anchor" id="«method.htmlLink»"></a>
			<span class="label label-primary resource-method">«method.httpMethod»</span>
			<code>«method.id»</code>
			
			«method.generateDocItem»
			
			«generateSecuritySchemeUsage(method)»
			
			«IF method.request !== null || !method.responses.empty»<ul class="list-group">«ENDIF»
			«IF method.request !== null»
				«generateRequest(method.request)»
			«ENDIF»
			«IF !method.responses.empty»
				«FOR response : method.responses»
					«generateResponse(response)»
				«ENDFOR»
			«ENDIF»
			«IF method.request !== null || !method.responses.empty»</ul>«ENDIF»
		'''
	}

	def private generateRequest(TypedRequest request) {
		var id = docServices.randomId
		'''
			«IF request !== null»
				<li class="list-group-item">
				<strong>Request</strong> «generateMessageName(request)»
				«IF request.hasOverridedMediaTypes»
					<h4>Properties</h4>
					<table class="table table-condensed">
					«generateMediaTypes(request.mediaTypes)»
					</table>
				«ENDIF»
				«IF showMessageDataType(request)»
					«generateMessageDataType(request)»
				«ENDIF»
				«request.generateDocItem»
				«IF !request.parameters.empty»
					<div class="pull-right" data-toggle="collapse" data-target="#collapse-«id»" style="cursor: pointer">
					  <strong>Parameters</strong>
					  <span class="caret"></span>
					</div>
					<div id="collapse-«id»" class="collapse in">
					  «generateParams(request.parameters)»
					</div>
				«ENDIF»
				</li>
			«ENDIF»
		'''
	}

	def private generateResponse(TypedResponse response) {
		'''
			  <li class="list-group-item">
			  <strong>Response</strong> «generateMessageName(response)»
			«IF response.statusCode > 0»
				<span class="label label-«statusColorCode(response.statusCode)»">«response.statusCode»</span>
			«ENDIF»
			«IF response.hasOverridedMediaTypes»
				<h4>Properties</h4>
				<table class="table table-condensed">
				«generateMediaTypes(response.mediaTypes)»
				</table>
			«ENDIF»
			«IF showMessageDataType(response)»
				«generateMessageDataType(response)»
			«ENDIF»
			  «response.generateDocItem»
			  </li>
		'''
	}

	def private boolean showMessageDataType(TypedMessage message) {
		// if usesResourcesDefaultRealization then skip the message data types table because it's identical to the one of the resource, 
		// and there is a hyperlink the resource in the header
		return message.actualType !== null && message.resourceType === null
	}

	def private generateIncludedPropertyRow(PropertyRealization includedProperty) {
		'''
			<tr>
			    <td>«includedProperty.baseProperty.name»</td>
			    <td>
			    «IF includedProperty.baseProperty instanceof ReferenceProperty»
			    	<a href="#«(includedProperty.baseProperty as ReferenceProperty).type.htmlLink»">«generateIncludedPropertyType(
                includedProperty)»</a>
			    «ELSE»
			    	«val type = (includedProperty.baseProperty as PrimitiveProperty).type»
			    	«IF type instanceof Enumeration || type instanceof UserDefinedType»
			    		<a href="#«type.htmlLink»">«generateIncludedPropertyType(includedProperty)»</a>
			    	«ELSE»
			    		«generateIncludedPropertyType(includedProperty)»
			    	«ENDIF»
			    «ENDIF»
			    «includedProperty.allConstraints.generateInlineConstraints»
			    </td>
			    <td>«includedProperty.baseProperty?.generateDoc»</td>
			</tr>
		'''
	}

	def private generateMediaTypes(List<MediaType> mediaTypes) {
		'''
			«IF !mediaTypes.empty»
				<tr>
					<th>Media Types</th>
					<td>
					«FOR mediaType : mediaTypes SEPARATOR ', '»<a target="_blank" href="«mediaType.specURL»">«mediaType.name»</a>«ENDFOR»
					</td>
				</tr>
			«ENDIF»
		'''
	}

	def private boolean hasOverridedMediaTypes(TypedMessage message) {
		if (!message.mediaTypes.empty) {
			return !message.mediaTypes.elementsEqual(
				(message.eContainer as Method).containingResourceDefinition.mediaTypes)
		}
		return false
	}

	def private boolean hasReferenceTreatment(ServiceDataResource resource, Feature feature) {
		resource.referenceTreatments.map[referenceElement].exists[f|f === feature]
	}

	def private boolean hasReferenceTreatment(TypedMessage message, Feature feature) {
		message.referenceTreatments.map[referenceElement].exists[f|f === feature]
	}

	def private PropertyRealization getIncludedProperty(ServiceDataResource resource, Feature feature) {
		getIncludedProperty(resource.properties, feature)
	}

	def private PropertyRealization getIncludedProperty(TypedMessage message, Feature feature) {
		getIncludedProperty(message.properties, feature)
	}

	def private PropertyRealization getIncludedProperty(ObjectRealization objectRealization, Feature feature) {
		objectRealization.allIncludedProperties.filter[InheritanceUtils::sameOrOverrides(it.baseProperty, feature)].head
	}

	def private generateReferenceTreatments(ServiceDataResource resource) {
		''' 
			«FOR ReferenceTreatment refTreatment : resource.referenceTreatments»
				«generateReferenceTreatment(refTreatment, resource.includedProperties, new CycleDetector<ReferenceTreatment>, 0)»
			«ENDFOR»
		'''
	}

	def private generateReferenceTreatments(TypedMessage message) {
		''' 
			«FOR ReferenceTreatment refTreatment : message.referenceTreatments»
				«generateReferenceTreatment(refTreatment, message.includedProperties, new CycleDetector<ReferenceTreatment>, 0)»
			«ENDFOR»
		'''
	}

	def dispatch private String generateReferenceTreatment(ReferenceLink refLink,
		List<PropertyRealization> includedProperies, CycleDetector<ReferenceTreatment> cycleDetector, Integer indent) {
		val referencedProperty = refLink.referenceElement
		val cardinality = XDataTypeExtensions::getCardinalityLabel(refLink, includedProperies)
		'''
			<tr>
			<td style="text-indent: «indent + 1 * parameterHelper.getContainmentReferences(refLink).size»em;">
			   «referencedProperty.name»
			    </td>
			 <td>
			<a href="#«refLink.targetResource.htmlLink»">«refLink.targetResource.name»</a>«cardinality»
			</td>
			<td>«if(referencedProperty instanceof ReferenceProperty) (referencedProperty as ReferenceProperty).generateDoc
                else "TODO"»</td>
			</tr>
			«FOR feature : getIncludedProperties(refLink)»
				«generateReferenceTreatmentPropertyRow(feature, indent + 1, true)»
			«ENDFOR»
		'''
	}

	def dispatch private String generateReferenceTreatment(ReferenceEmbed refEmbed,
		List<PropertyRealization> includedProperies, CycleDetector<ReferenceTreatment> cycleDetector, Integer indent) {

		val isRecursive = !cycleDetector.visit(refEmbed)
		val referencedProperty = refEmbed.referenceElement
		val cardinality = XDataTypeExtensions::getCardinalityLabel(refEmbed, includedProperies)

		'''
			<tr>
			    <td style="text-indent: «indent + 1 * parameterHelper.getContainmentReferences(refEmbed).size»em;">
			    «referencedProperty.name» «IF isRecursive» (recursive) «ENDIF»
			    </td>
			    <td>
			    <a href="#«referencedProperty.dataType.htmlLink»">«importHelper.getQualifiedName(referencedProperty.dataType)»</a>«cardinality»
			    </td>
			<td>«if(referencedProperty instanceof ReferenceProperty) (referencedProperty as ReferenceProperty).generateDoc else "TODO"»</td>
			</tr>
			«IF !isRecursive»
				«FOR feature : getIncludedProperties(refEmbed).filter[e|e instanceof PrimitiveProperty]»
					«generateReferenceTreatmentPropertyRow(feature, indent + 1, true)»
				«ENDFOR»							
				«FOR ReferenceTreatment refTreatment : refEmbed.nestedReferenceTreatments»					
					«generateReferenceTreatment(refTreatment, includedProperies, cycleDetector, indent + 1)»
				«ENDFOR»
			«ENDIF»
		'''
	}

	def private generateIncludedPropertyType(PropertyRealization includedProperty) {
		'''
			«parameterHelper.featureType(includedProperty.baseProperty, importHelper)»«commonServices.
                getPrettyPrintedCardinality(includedProperty)»
		'''
	}

	def private List<Feature> getIncludedProperties(ReferenceTreatment refLink) {
		if (refLink.linkDescriptor === null) {
			Collections::emptyList
		} else {
			refLink.linkDescriptor.allIncludedProperties.map[baseProperty]
		}
	}

	def private generateReferenceTreatmentPropertyRow(Feature property, Integer indent, boolean genDoc) {
		'''
			<tr>
			    <td style="text-indent: «indent»em;">«property.name»</td>
			    <td>
			    «IF property instanceof ReferenceProperty»
			    	<a href="#«(property as ReferenceProperty).type.htmlLink»">«parameterHelper.
                featureType(property, importHelper)»«commonServices.getPrettyPrintedMultiplicity(property)»</a>
			    «ELSE»
			    	«parameterHelper.featureType(property, importHelper)»«commonServices.
                getPrettyPrintedMultiplicity(property)»
			    «ENDIF»
			    </td>
			    «IF genDoc»
			    	<td>«property?.generateDoc»</td>
			    «ENDIF»
			</tr>
		'''
	}

	def protected generateSecuritySchemeUsage(HasSecurityValue hasSecuredValue) {
		'''
			«IF !hasSecuredValue.securedBy.empty»
				«XDocHelper.tableWithHeader("Security Schemes", "Name", "Type", "Authorized Scopes")»
				«FOR authMethod : hasSecuredValue.securedBy»
					<tr>
					    <td><a href="#«authMethod.scheme.htmlLink»">«authMethod.scheme.name»</a></td>
					    <td>«authMethod.scheme.type.toString»</td>
					    <td>«FOR scope : authMethod.scopes SEPARATOR ', '»«scope.name»«ENDFOR»</td>
					</tr>
				«ENDFOR»
				</table>
			«ENDIF»
		'''
	}

	def static String statusColorCode(Integer code) {
		switch code {
			case code >= 100 && code < 200: "info"
			case code >= 200 && code < 300: "success"
			case code >= 300 && code < 400: "info"
			default: "danger"
		}
	}

	def isLiveView(boolean isLiveView) {
		this.isLiveView = isLiveView
	}

}
