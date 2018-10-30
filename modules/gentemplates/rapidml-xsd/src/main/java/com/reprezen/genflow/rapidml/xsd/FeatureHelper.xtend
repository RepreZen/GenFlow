/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd

import com.reprezen.genflow.api.zenmodel.util.CommonServices
import com.reprezen.rapidml.DataModel
import com.reprezen.rapidml.Element
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.PrimitiveType
import com.reprezen.rapidml.PrimitiveTypeSourceReference
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.PropertyReference
import com.reprezen.rapidml.ReferenceElement
import com.reprezen.rapidml.ReferenceEmbed
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.SourceReference
import com.reprezen.rapidml.TypedMessage

class FeatureHelper {
	extension XMLSchemaHelper xmlSchemaHelper
	extension ParamsHelper paramsHelper
	extension TraceHelper traceHelper
	
	val static UNBOUNDED = 'unbounded'
	val commonServices = new CommonServices

	new(Helpers helpers) {
		this.xmlSchemaHelper = helpers.xmlSchemaHelper
		this.paramsHelper = helpers.paramsHelper
		this.traceHelper = helpers.traceHelper
	}

	def isRequired(Feature feature) {
		feature.minOccurs > 0
	}

	def isRequired(ReferenceElement feature) {
		feature.minOccurs > 0
	}

	def isRequired(PropertyRealization includedProperty) {
		includedProperty.minOccurs > 0
	}

	def getLcName(Element element) {
		switch element {
			Feature: element.name.toFirstLower
			ReferenceElement: element.name.toFirstLower
		}
	}

	def getLcName(PropertyRealization property) {
		property.baseProperty.lcName
	}

	def getTypeName(PrimitiveProperty primitiveProperty, ResourceAPI api) {
		val prefix = if (primitiveProperty.type instanceof PrimitiveType)
				"xs"
			else {
				primitiveProperty.type.getEContainer(DataModel).nsPrefix(api)
			}
		return prefix + ":" + primitiveProperty.primitiveFeatureType
	}

	def getTypeName(PropertyRealization property, ResourceAPI api) {
		val baseProperty = property.baseProperty
		switch baseProperty {
			PrimitiveProperty: baseProperty.getTypeName(api)
			default: throw new IllegalArgumentException
		}
	}

	def getListName(Element element) {
		val name = element.lcName+"List"
		element.setListElementInTrace(name)
		return name
	}
	
	def getListName(PropertyRealization property){
		property.baseProperty.listName
	}

	def getItemName(Element element) {
		val name = (listItemElementName ?: "item").replaceAll("\\$\\{property\\}", element.lcName)
		element.setListItemElementInTrace(name)
		return name
	}

	def getItemName(PropertyRealization property) {
		property.baseProperty.itemName
	}

	def ReferenceLink getReferenceLink(Feature feature, Iterable<ReferenceLink> referenceLinks) {
		val refs = referenceLinks.filter[referenceElement == feature]
		if(refs.nullOrEmpty) null else refs.findFirst[]
	}

	def getListMinOccurs(Feature feature) {
		if(feature.isRequired) 1 else 0
	}

	def getListMinOccurs(ReferenceElement feature) {
		if(feature.isRequired) 1 else 0
	}

	def getListMinOccurs(PropertyRealization feature) {
		if(feature.isRequired) 1 else 0
	}

	def getListItemMinOccurs(Feature feature) {
		if(allowEmptyLists) 0 else 1
	}

	def getListItemMinOccurs(ReferenceElement feature) {
		if(allowEmptyLists) 0 else 1
	}

	def getListItemMinOccurs(PropertyRealization feature) {
		if(allowEmptyLists) 0 else 1
	}

	def getListItemMaxOccurs(Feature feature) {
		if(feature.maxOccurs == -1 || feature.maxOccurs > 1) UNBOUNDED else 1
	}

	def getListItemMaxOccurs(ReferenceElement feature) {
		if(feature.maxOccurs == -1 || feature.maxOccurs > 1) UNBOUNDED else 1
	}

	def getListItemMaxOccurs(PropertyRealization feature) {
		if(feature.maxOccurs == -1 || feature.maxOccurs > 1) UNBOUNDED else 1
	}

	def getReferenceEmbeds(TypedMessage message) {
		message.referenceTreatments.filter(ReferenceEmbed)
	}

	def getReferenceEmbeds(ServiceDataResource dataResource) {
		dataResource.referenceTreatments.filter(ReferenceEmbed)
	}

	def Iterable<PrimitiveProperty> getPrimitiveProperties(Iterable<Feature> features) {
		features.filter[it.isPrimitiveProperty].map[it as PrimitiveProperty]
	}

	def Iterable<ReferenceProperty> getReferenceProperties(Iterable<Feature> features) {
		features.filter[it instanceof ReferenceProperty].map[it as ReferenceProperty]
	}

	def Iterable<PropertyRealization> getReferenceProperties(ServiceDataResource dataResource) {
		dataResource.includedProperties.filter[isReferenceProperty(it.baseProperty)]
	}

	def Iterable<PropertyRealization> getReferenceProperties(TypedMessage message) {
		message.includedProperties.filter[isReferenceProperty(it.baseProperty)]
	}

	def isPrimitiveProperty(Feature feature) {
		feature instanceof PrimitiveProperty
	}

	def Iterable<PrimitiveProperty> getPrimitiveElementProperties(Iterable<Feature> features) {
		features.filter[it.isMultiValued].primitiveProperties
	}

	def Iterable<PropertyRealization> getPrimitiveElementProperties(ServiceDataResource dataResource) {
		dataResource.includedProperties.filter[isPrimitiveProperty(it.baseProperty) && it.isMultiValued]
	}

	def Iterable<PropertyRealization> getPrimitiveElementProperties(TypedMessage message) {
		message.includedProperties.filter[isPrimitiveProperty(it.baseProperty) && it.isMultiValued]
	}

	def Iterable<PrimitiveProperty> getPrimitiveSingleFeatures(Iterable<Feature> features) {
		features.primitiveProperties.filter[!it.isMultiValued]
	}

	def Iterable<PropertyRealization> getPrimitiveSingleProperties(Iterable<PropertyRealization> properties) {
		properties.filter[baseProperty.isPrimitiveProperty && !baseProperty.isMultiValued]
	}

	def isMultiValued(Feature feature) {
		feature.maxOccurs == -1
	}

	def isSingleValued(Feature feature) {
		!feature.isMultiValued
	}

	def isMultiValued(ReferenceElement feature) {
		feature.maxOccurs == -1
	}

	def isMultiValued(PropertyRealization feature) {
		feature.maxOccurs == -1
	}

	def isSingleValued(PropertyRealization feature) {
		!feature.isMultiValued
	}

	def isPrimitivePropertyReference(PropertyReference featureReference) {
		featureReference.conceptualFeature.isPrimitiveProperty
	}

	def isPrimitiveSourceReference(SourceReference sourceReference) {
		if (sourceReference instanceof PropertyReference)
			sourceReference.isPrimitivePropertyReference
		else
			sourceReference instanceof PrimitiveTypeSourceReference
	}

	def isReferencePropertyReference(PropertyReference featureReference) {
		featureReference.conceptualFeature.isReferenceProperty
	}

	def isReferenceProperty(Feature feature) {
		feature instanceof ReferenceProperty
	}

	def isReferenceSourceReference(SourceReference sourceReference) {
		if (sourceReference instanceof PropertyReference)
			sourceReference.isReferencePropertyReference
		else
			false
	}

	def primitiveFeatureType(PrimitiveProperty primitiveProperty) {
		primitiveProperty.type.typeName
	}

	def referenceFeatureType(ReferenceProperty referenceProperty) {
		referenceProperty.type.name
	}

	def featureType(Feature feature) {
		if (feature.isPrimitiveProperty)
			(feature as PrimitiveProperty).primitiveFeatureType
		else
			(feature as ReferenceProperty).referenceFeatureType
	}

	def getPrettyPrintedMultiplicity(Feature feature) {
		commonServices.getPrettyPrintedMultiplicity(feature)
	}

	def getPrettyPrintedCardinality(PropertyRealization includedProperty) {
		commonServices.getPrettyPrintedCardinality(includedProperty)
	}
}
