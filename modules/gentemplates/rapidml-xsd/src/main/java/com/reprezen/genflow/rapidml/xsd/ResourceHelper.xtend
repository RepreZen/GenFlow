/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd

import com.reprezen.rapidml.ObjectResource
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.Structure

class ResourceHelper {
	extension XMLSchemaHelper xmlSchemaHelper
	extension FeatureHelper featureHelper

	new(Helpers helpers) {
		this.xmlSchemaHelper = helpers.xmlSchemaHelper
		this.featureHelper = helpers.featureHelper
	}

	def ObjectResource getCorrespondingResource(Structure complexType, ResourceAPI resourceAPI) {
		val defaultResource = complexType.getDefaultResource(resourceAPI)
		if (defaultResource !== null)
			defaultResource
		else {
			val onlyResource = complexType.getOnlyResource(resourceAPI)
			if(onlyResource !== null) defaultResource else null
		}
	}

	def private ObjectResource getDefaultResource(Structure complexType, ResourceAPI resourceAPI) {
		val resources = complexType.getAllResources(resourceAPI).filter[it.^default]
		if(resources.nullOrEmpty) null else resources.findFirst[]
	}

	def private ServiceDataResource getOnlyResource(Structure complexType, ResourceAPI resourceAPI) {
		val resources = complexType.getAllResources(resourceAPI)
		if(resources.size == 1) resources.findFirst[] else null
	}

    def ServiceDataResource getTargetResource(ReferenceProperty feature, ServiceDataResource dataResource) {
        val referenceLink = feature.getReferenceLink(dataResource.referenceLinks as Iterable<ReferenceLink>)
        if(referenceLink !== null) referenceLink.targetResource as ServiceDataResource else feature.type.
            getCorrespondingResource(dataResource.getInterface)
    }

	def private Iterable<ObjectResource> getAllResources(Structure complexType, ResourceAPI resourceAPI) {
		resourceAPI.ownedResourceDefinitions.map[it as ObjectResource].filter[it.dataType == complexType]
	}

	def ResourceAPI getInterface(ServiceDataResource dataResource) {
		if(dataResource === null) null else dataResource.getEContainer(ResourceAPI)
	}
}
