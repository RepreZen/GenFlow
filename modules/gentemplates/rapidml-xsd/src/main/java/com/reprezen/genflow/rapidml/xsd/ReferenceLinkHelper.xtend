/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd

import com.reprezen.rapidml.NamedLinkDescriptor
import com.reprezen.rapidml.ObjectRealization
import com.reprezen.rapidml.ReferenceEmbed
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedMessage
import java.util.LinkedList

class ReferenceLinkHelper {
	extension XMLSchemaHelper xmlSchemaHelper

	new(Helpers helpers) {
		this.xmlSchemaHelper = helpers.xmlSchemaHelper
	}

	def Iterable<ReferenceProperty> getContainmentReferencesAtPosition(Iterable<ReferenceTreatment> referenceTreatments,
		Integer index) {
		referenceTreatments.filter[it.containmentReferences.size >= index].map [
			it.containmentReferences.toList.get(index)
		].toSet
	}

	def dispatch isPropertyOverridenByReferenceLink(ReferenceProperty referenceProperty,
		ServiceDataResource dataResource, Iterable<ReferenceProperty> pathToCurrentSegment) {
		isPropertyOverridenByReferenceLink(
			referenceProperty,
			getContainmentReferencesAtPosition(
				dataResource.referenceLinks.map[it as ReferenceTreatment],
				pathToCurrentSegment.size + 1
			)
		)
	}

	def dispatch isPropertyOverridenByReferenceLink(ReferenceProperty referenceProperty, TypedMessage message,
		Iterable<ReferenceProperty> pathToCurrentSegment) {
		isPropertyOverridenByReferenceLink(
			referenceProperty,
			getContainmentReferencesAtPosition(
				message.referenceLinks.map[it as ReferenceTreatment],
				pathToCurrentSegment.size + 1
			)
		)
	}

	def private isPropertyOverridenByReferenceLink(ReferenceProperty referenceProperty,
		Iterable<ReferenceProperty> featuresOverridenByReferenceLinks) {
		featuresOverridenByReferenceLinks.exists[it == referenceProperty]
	}

	def dispatch ObjectRealization getLinkDescriptor(ReferenceLink referenceLink) {
		if (referenceLink.linkDescriptor !== null)
			referenceLink.linkDescriptor
		else
			referenceLink.targetResource.linkDescriptor
	}

	def dispatch NamedLinkDescriptor getLinkDescriptor(ResourceDefinition dataResource) {
		if ((dataResource !== null) && (dataResource instanceof ServiceDataResource))
			(dataResource as ServiceDataResource).defaultLinkDescriptor
		else
			null
	}

	def getReferenceTreatmentName(ReferenceTreatment referenceLink) {
		referenceLink.containmentReferences.map[it.name].join(".") + "." + referenceLink.referenceElement.name
	}

	def Iterable<ReferenceProperty> getContainmentReferences(ReferenceTreatment referenceLink) {
		new LinkedList<ReferenceProperty>
	}

	def getReferenceProperty(ReferenceLink referenceLink) {
		referenceLink.referenceElement
	}

	def getRelValue(ReferenceLink referenceLink) {
		if(referenceLink.linkRelation === null) null else referenceLink.linkRelation.name
	}

	def ServiceDataResource getContainingServiceDataResource(ReferenceTreatment referenceLink) {
		val eServiceDataResource = referenceLink.getEContainer(ServiceDataResource)
		val eReferenceEmbed = referenceLink.getEContainer(ReferenceEmbed)

		if(eServiceDataResource !== null) eServiceDataResource else eReferenceEmbed.containingServiceDataResource
	}

	def startsWithPath(ReferenceTreatment referenceLink, Iterable<ReferenceProperty> containmentPath) {
		containmentPath == referenceLink.getFirstContainmentFragments(containmentPath.size)
	}

	def dispatch private Iterable<ReferenceProperty> getFirstContainmentFragments(ReferenceTreatment referenceTreatment,
		Integer number) {
		referenceTreatment.containmentReferences.getFirstContainmentFragments(number)
	}

	def dispatch private Iterable<ReferenceProperty> getFirstContainmentFragments(
		Iterable<ReferenceProperty> containmentPath, Integer number) {
		if(containmentPath.size >= number) containmentPath.take(number) else null
	}

	def getContainmentDepth(ReferenceLink referenceLink) {
		0
	}
}
