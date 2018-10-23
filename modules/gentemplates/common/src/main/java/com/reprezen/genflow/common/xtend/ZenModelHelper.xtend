/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.xtend

import com.reprezen.restapi.Documentable
import com.reprezen.restapi.Enumeration
import com.reprezen.restapi.Feature
import com.reprezen.restapi.PrimitiveProperty
import com.reprezen.restapi.RealizationContainer
import com.reprezen.restapi.ReferenceEmbed
import com.reprezen.restapi.ReferenceLink
import com.reprezen.restapi.ReferenceProperty
import com.reprezen.restapi.ReferenceTreatment
import com.reprezen.restapi.ResourceAPI
import com.reprezen.restapi.ServiceDataResource
import com.reprezen.restapi.TypedMessage
import com.reprezen.restapi.UserDefinedType
import com.reprezen.restapi.ZenModel
import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.Collections
import java.util.List
import org.eclipse.emf.ecore.EObject

class ZenModelHelper {
	protected def dispatch List<? extends Object> getReferenceTreatmentIncludedProperties(ReferenceEmbed refEmbed) {
		// contains Features and ReferenceTreatments
		val includedProps = new ArrayList<EObject>();
		includedProps.addAll(getIncludedPrimitiveProperties(refEmbed))
		includedProps.addAll(refEmbed.nestedReferenceTreatments)
		return includedProps
	}

	protected def dispatch getReferenceTreatmentIncludedProperties(ReferenceLink refEmbed) {
		return getIncludedPrimitiveProperties(refEmbed)
	}

	def List<Feature> getIncludedPrimitiveProperties(ReferenceTreatment refTreatment) {
		return getIncludedProperties(refTreatment).filter[e|e instanceof PrimitiveProperty].toList
	}

	def List<Feature> getIncludedProperties(ReferenceTreatment refLink) {
		refLink.linkDescriptor?.allIncludedProperties?.map[baseProperty] ?: Collections::emptyList
	}

	def boolean hasReferenceTreatment(RealizationContainer resource, Feature feature) {
		resource.referenceTreatments.map[referenceElement].exists[f|f === feature]
	}

	def boolean hasReferenceTreatment(TypedMessage resource, Feature feature) {
		// TODO remove this method when TypedMessage and ServiceDataResource have a shared ObjectRealization supertype
		resource.referenceTreatments.map[referenceElement].exists[f|f === feature]
	}
	
	def dispatch Collection<Enumeration> getUsedEnums(Object o) {
		// For Xtend compilation. Otherwise it tries to cast to Extensible
		throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.<Object>asList(o).toString());
	}

	def dispatch Collection<Enumeration> getUsedEnums(ZenModel model) {
		model.resourceAPIs.map[it.usedEnums].flatten.toSet
	}

	def dispatch Collection<Enumeration> getUsedEnums(ResourceAPI resourceAPI) {
		resourceAPI.ownedResourceDefinitions.filter(ServiceDataResource).map[dataInterface|getUsedEnums(dataInterface)].
			flatten.toSet
	}

	def dispatch Collection<Enumeration> getUsedEnums(ServiceDataResource resource) {
		(resource.includedProperties.filter[e|!hasReferenceTreatment(resource, e.baseProperty)].map [
			getUsedEnums(it.baseProperty)
		].flatten + resource.referenceTreatments.filter(ReferenceEmbed).map[getUsedEnums(it)].flatten).toSet
	}

	def dispatch Collection<Enumeration> getUsedEnums(ReferenceTreatment ref) {
		getReferenceTreatmentIncludedProperties(ref).map[getUsedEnums(it)].flatten.toSet
	}

	def dispatch Collection<Enumeration> getUsedEnums(PrimitiveProperty pp) {
		if (pp.type instanceof Enumeration)
			Collections.singletonList(pp.type as Enumeration)
		else
			Collections.emptyList()
	}

	def dispatch Collection<Enumeration> getUsedEnums(ReferenceProperty ref) {
		Collections.emptyList()
	}

	def dispatch Collection<UserDefinedType> getUsedUserDefinedTypes(Object o) {
		// For Xtend compilation. Otherwise it tries to cast to Extensible
		throw new IllegalArgumentException("Unhandled parameter types: " + Arrays.<Object>asList(o).toString());
	}

	def dispatch Collection<UserDefinedType> getUsedUserDefinedTypes(ZenModel model) {
		model.resourceAPIs.map[it.getUsedUserDefinedTypes].flatten.toSet
	}

	def dispatch Collection<UserDefinedType> getUsedUserDefinedTypes(ResourceAPI resourceAPI) {
		resourceAPI.ownedResourceDefinitions.filter(ServiceDataResource).map [ dataInterface |
			getUsedUserDefinedTypes(dataInterface)
		].flatten.toSet
	}

	def dispatch Collection<UserDefinedType> getUsedUserDefinedTypes(ServiceDataResource resource) {
		(resource.includedProperties.filter[e|!hasReferenceTreatment(resource, e.baseProperty)].map [
			getUsedUserDefinedTypes(it.baseProperty)
		].flatten + resource.referenceTreatments.filter(ReferenceEmbed).map[getUsedUserDefinedTypes(it)].flatten).toSet
	}

	def dispatch Collection<UserDefinedType> getUsedUserDefinedTypes(ReferenceTreatment ref) {
		getReferenceTreatmentIncludedProperties(ref).map[getUsedUserDefinedTypes(it)].flatten.toSet
	}

	def dispatch Collection<UserDefinedType> getUsedUserDefinedTypes(PrimitiveProperty pp) {
		if (pp.type instanceof UserDefinedType) {
			var List<UserDefinedType> list = new ArrayList
			list += pp.type as UserDefinedType
			var parent = (pp.type as UserDefinedType).baseType;
			while (parent instanceof UserDefinedType) {
				list += parent as UserDefinedType
				parent = (parent as UserDefinedType).baseType
			}
			list
		} else {
			Collections.emptyList()
		}
	}

	def dispatch Collection<UserDefinedType> getUsedUserDefinedTypes(ReferenceProperty ref) {
		Collections.emptyList()
	}

	def String getDocumentation(EObject obj) {
		if (obj instanceof Documentable) {
			val doc = (obj as Documentable).documentation?.text ?: ""
			doc.trim()
		} else {
			""
		}
	}

}
