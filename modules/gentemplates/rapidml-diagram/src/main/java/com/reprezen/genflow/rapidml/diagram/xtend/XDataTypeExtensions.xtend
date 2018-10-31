/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram.xtend

import com.google.common.collect.Lists
import com.reprezen.genflow.common.doc.XDocHelper
import com.reprezen.rapidml.CollectionRealizationEnum
import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.ReferenceEmbed
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.datatypes.cardinality.Cardinality
import com.reprezen.rapidml.datatypes.cardinality.FeatureCardinalities
import com.reprezen.rapidml.realization.processor.CycleDetector
import java.net.URI

import static extension com.reprezen.genflow.common.xtend.XDataTypeExtensions.*

/**
 * Extension for generate JSON object for datatype
 */
class XDataTypeExtensions {
	extension XDocHelper xDocHelper
	extension XFeatureExtensions = new XFeatureExtensions

	new(URI baseUri) {
		xDocHelper = new XDocHelper(baseUri)
	}

	def generateDataType(Structure dataType, ServiceDataResource resource) {
		'''
			"dataType": {
			    "objecttype": "DataType",
			    "name": "«dataType.name»«IF resource instanceof CollectionResource»*«ENDIF»",
			    "anchorId": "«htmlLink(dataType)»",
			    "id": "«resource.name».«dataType.name»",
			    "properties": [
			        «FOR feature : resource.dataType.ownedFeatures.filter(PrimitiveProperty).filter[e|
                resource.isIncluded(e)] SEPARATOR ','»
			        	«feature.generatePrimitiveProperty(resource)»
			        «ENDFOR»
			    ]
			    ,"referenceTreatments": [
			        «FOR aReferenceTreatment : resource.referenceTreatments SEPARATOR ','»
			        	«aReferenceTreatment.generateReferenceTreatment(resource, new CycleDetector<ReferenceTreatment>)»
			        «ENDFOR»
			    ]
			}
		'''
	}

	def private generatePrimitiveProperty(PrimitiveProperty aFeature, ServiceDataResource aResource) {
		'''
			{
			    "objecttype": "PrimitiveProperty",
			    "name": "«aFeature.name»",
			    "type": "«aFeature.type.name»",
			    «aFeature.generateCardinality»    
			    "id": "«aResource.featureId(aFeature)»"
			}
		'''
	}

	def private generateCardinality(Feature feature) {
		val Cardinality cardinality = FeatureCardinalities.getFeatureCardinalities().getCardinality(feature)
		if (!cardinality.getLabel().empty) '''"cardinality": "«cardinality.getLabel()»",''' else ""
	}

	def CharSequence generateCollectionDataType(CollectionResource res) {
		if (res.resourceRealizationKind == CollectionRealizationEnum::REFERENCE_LINK_LIST) {
			'"baseType" : ' + res.generateCollectionReferenceLink(res.referenceLinks.get(0))
		} else {
			res.dataType.generateDataType(res)
		}
	}

	def String generateCollectionReferenceLink(ServiceDataResource aResource, ReferenceLink link) {
		'''
			{
			    "objecttype": "ReferenceLink",
			    "name": "«aResource.dataType.name»*",
			    "anchorId": "«htmlLink(aResource)»",
			    "cardinality": "*",
			    "id": "«aResource.name».baseType",
			    "referencedResourceId": "«link.targetResource.name»",
			    "properties": [
			    «IF link.linkDescriptor !== null»
			    	«FOR feature : link.linkDescriptor.allIncludedProperties SEPARATOR ','»
					«generatePropertyRealization(feature, aResource, null)»
				«ENDFOR»
			«ENDIF»
			    ]
			}
		'''
	}

	def dispatch private String generateReferenceTreatment(ReferenceLink link, ServiceDataResource aResource,
		CycleDetector<ReferenceTreatment> cycleDetector) {
		'''
			{
			    "objecttype": "ReferenceLink",
			    "name": "«link.referenceTreatmentName»«link.getCardinalityLabel(aResource.includedProperties)»",
			    "cardinality": "«link.getCardinalityLabel(aResource.includedProperties)»",
			    "id": "«link.referenceTreatmentId(aResource)»",
			    "referencedResourceId": "«link.targetResourceId»",
			    «IF link.linkRelation !== null»
			    	"linkRelation": "rel: «link.linkRelation.name»",
			    «ENDIF»
			    "properties": [
			    «IF link.linkDescriptor !== null»
			    	«FOR feature : link.linkDescriptor.allIncludedProperties SEPARATOR ','»
			    		«feature.generatePropertyRealization(aResource, link)»
			    	«ENDFOR»
			    «ENDIF»
			    ]
			}
		'''
	}

	def dispatch private String generateReferenceTreatment(ReferenceEmbed ref, ServiceDataResource resource,
		CycleDetector<ReferenceTreatment> cycleDetector) {
		val isRecursive = !cycleDetector.visit(ref)

		'''
			{
			    "objecttype": "ReferenceEmbed",
			    "name": "«ref.referenceTreatmentName» : «ref.referenceElement.dataType.name»«ref.getCardinalityLabel(resource.includedProperties)»«IF isRecursive» (recursive) «ENDIF»",
			    "id": "«ref.referenceTreatmentId(resource)»",
			    "properties": [
			    «IF ref.linkDescriptor !== null && !isRecursive»
			    	«FOR feature : ref.linkDescriptor.allIncludedProperties.map(it|it.baseProperty).filter(PrimitiveProperty) SEPARATOR ','»
			    		«feature.generateReferenceTreatmentFeature(resource, ref)»
			    	«ENDFOR»
			    «ENDIF»
			    ], "referenceTreatments": [
			        «IF !isRecursive» 
			        	«FOR nestedReferenceTreatment : ref.nestedReferenceTreatments SEPARATOR ','»
			        		«nestedReferenceTreatment.generateReferenceTreatment(resource, cycleDetector)»
			        	«ENDFOR»
			        «ENDIF»
			    ]
			}
		'''
	}

	def private getReferenceTreatmentName(ReferenceTreatment aReferenceLink) {
		aReferenceLink.referenceElement.name
	}

	def private referenceTreatmentId(ReferenceTreatment ref, ServiceDataResource resource) {
		if (ref instanceof ReferenceLink)
			referenceTreatmentIdPath(ref, resource) + '.referenceLink'
		else if (ref instanceof ReferenceEmbed)
			referenceTreatmentIdPath(ref, resource) + '.referenceEmbed'
		else
			referenceTreatmentIdPath(ref, resource) + '.referenceTreatment'
	}

	def private referenceTreatmentIdPath(ReferenceTreatment ref, ServiceDataResource resource) {
		var names = Lists::newArrayList(ref.embedHierarchy.map[e|getReferenceTreatmentName(e)]);
		names.add(getReferenceTreatmentName(ref))
		names.add(0, resource.name)
		names.join('.')
	}

	def private getTargetResourceId(ReferenceLink aReferenceLink) {
		if(aReferenceLink.targetResource === null) '<undefined>' else aReferenceLink.targetResource.name
	}

	def private dispatch generateReferenceTreatmentFeature(Feature feature, ServiceDataResource resource,
		ReferenceTreatment ref) {
		'<unknown feature type>'
	}

	def private dispatch generateReferenceTreatmentFeature(ReferenceProperty feature, ServiceDataResource resource,
		ReferenceTreatment ref) {
		'''
			{
			    "objecttype": "ReferenceProperty",
			    "name": "«feature.name»",
			    "type": "«feature.type.name»",
			    «feature.generateCardinality»    
			    "id": "«(if (ref === null) resource.name else referenceTreatmentId(ref,resource)) + '.' + feature.name»"
			}
		'''
	}

	def private dispatch generateReferenceTreatmentFeature(PrimitiveProperty feature, ServiceDataResource resource,
		ReferenceTreatment ref) {
		'''
			{
			    "objecttype": "PrimitiveProperty",
			    "name": "«feature.name»",
			    "type": "«feature.type.name»",
			    «feature.generateCardinality»    
			    "id": "«(if (ref === null) resource.name else referenceTreatmentId(ref, resource)) + '.' + feature.name»"
			}
		'''
	}

	def private generatePropertyRealization(PropertyRealization feature, ServiceDataResource resource,
		ReferenceTreatment ref) {
		generateReferenceTreatmentFeature(feature.baseProperty, resource, ref)
	}
}
