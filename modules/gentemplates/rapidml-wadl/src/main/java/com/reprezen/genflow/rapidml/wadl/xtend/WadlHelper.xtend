/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.xtend

import com.reprezen.genflow.api.zenmodel.util.CommonServices
import com.reprezen.rapidml.Element
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.MediaType
import com.reprezen.rapidml.Parameter
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.PrimitiveTypeSourceReference
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.PropertyReference
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.SourceReference
import com.reprezen.rapidml.TypedMessage
import java.util.Collection
import org.eclipse.emf.ecore.EObject

class WadlHelper {
	val commonServices = new CommonServices

	def getRelValue(ReferenceLink referenceLink) {
		referenceLink.linkRelation?.name
	}

	/* MediaTypeHelpers begin */
	def getMediaSuperTypes(MediaType mediaType) {
		commonServices.getMediaSuperTypes(mediaType)
	}

	def supportsJaxb(MediaType mediaType) {
		mediaType.mediaTypeIsKindOf("application/json") || mediaType.xmlMediaType
	}

	def isXmlMediaType(MediaType mediaType) {
		mediaType.mediaTypeIsKindOf("application/xml")
	}

	def mediaTypeIsKindOf(MediaType mediaType, String mediaTypeName) {
		var Collection<MediaType> mediaTypesList = mediaType.mediaSuperTypes
		mediaTypesList += mediaType
		mediaTypesList.exists[it.name == mediaTypeName]
	}

	/* Parameters Helpers */
	def paramName(SourceReference sourceReference) {
		(sourceReference as PropertyReference).conceptualFeature.name
	}

	def getPrimitiveParameters(TypedMessage message) {
		message.parameters.filter[it.isPrimitivePropertyParameter]
	}

	def isPrimitivePropertyParameter(Parameter parameter) {
		parameter.sourceReference.isPrimitiveSourceReference
	}

	def getPrimitiveProperty(Parameter parameter) {
		(parameter.sourceReference as PropertyReference).conceptualFeature as PrimitiveProperty
	}

	def getReferenceParameters(TypedMessage message) {
		message.parameters.filter[it.isReferencePropertyParameter]
	}

	def isReferencePropertyParameter(Parameter parameter) {
		parameter.sourceReference.isReferenceSourceReference
	}

	/* MessageType Helpers */
	def getMessageTypeName(TypedMessage message) {
		CommonServices.getMessageTypeName(message)
	}

	def isPrimitiveSourceReference(SourceReference sourceReference) {
		if (sourceReference instanceof PropertyReference) {
			(sourceReference as PropertyReference).isPrimitivePropertyReference
		} else {
			sourceReference instanceof PrimitiveTypeSourceReference
		}
	}

	def primitiveFeatureType(PrimitiveProperty primitiveProperty) {
		primitiveProperty.type.name
	}

	def isReferenceSourceReference(SourceReference sourceReference) {
		if (sourceReference instanceof PropertyReference) {
			(sourceReference as PropertyReference).isReferencePropertyReference
		} else {
			false
		}
	}

	def isReferencePropertyReference(PropertyReference featureReference) {
		featureReference.conceptualFeature.isReferenceProperty
	}

	def isReferenceProperty(Feature feature) {
		feature instanceof ReferenceProperty
	}

	def isPrimitivePropertyReference(PropertyReference featureReference) {
		featureReference.conceptualFeature.isPrimitiveProperty
	}

	def isPrimitiveProperty(Feature feature) {
		feature instanceof PrimitiveProperty
	}

	def dispatch isMultiValued(Element feature) {
		(feature.maxOccurs > 1) || (feature.maxOccurs == -1)
	}

	def dispatch isMultiValued(PropertyRealization feature) {
		(feature.maxOccurs > 1) || (feature.maxOccurs == -1)
	}

	def <T extends EObject> T getEContainer(EObject ele, Class<T> type) {
		CommonServices::getContainerOfType(ele, type)
	}
}
