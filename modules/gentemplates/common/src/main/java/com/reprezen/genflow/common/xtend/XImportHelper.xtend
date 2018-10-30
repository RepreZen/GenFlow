/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.xtend

import com.google.common.collect.Multimap
import com.reprezen.rapidml.DataModel
import com.reprezen.rapidml.DataType
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.ZenModel
import org.eclipse.emf.ecore.EObject

/**
 * Helper methods for import statements.
 */
class XImportHelper extends XGenericImportHelper<DataModel, DataType> {

	override String getModelFullQualifiedName(DataModel dataModel) {
		val model = dataModel.eContainer as ZenModel
		val nsPrefix = if(model.namespace !== null) model.namespace + "."
		'''«nsPrefix»«model.name».«dataModel.name»'''
	}

	def String getModelFullQualifiedName(ResourceAPI resourceAPI) {
		val model = resourceAPI.eContainer as ZenModel
		val nsPrefix = if(model.namespace !== null) model.namespace + "."
		'''«nsPrefix»«model.name».«resourceAPI.name»'''
	}

	override protected void addElement(Multimap<DataModel, DataType> map, EObject e) {
		addType(map, e)
	}

	private def dispatch void addType(Multimap<DataModel, DataType> map, EObject e) {
	}

	private def dispatch void addType(Multimap<DataModel, DataType> map, ServiceDataResource sdr) {
		map.addType(sdr.dataType)
	}

	private def dispatch void addType(Multimap<DataModel, DataType> map, ReferenceProperty rp) {
		map.addType(rp.type)
		map.addType(rp.containingDataType)
	}

	private def dispatch void addType(Multimap<DataModel, DataType> map, PrimitiveProperty rp) {
		map.addType(rp.type)
		map.addType(rp.containingDataType)
	}

	private def dispatch void addType(Multimap<DataModel, DataType> map, Structure s) {
		if (s.eContainer instanceof DataModel && !map.containsValue(s)) {
			map.put(s.eContainer as DataModel, s)
			s.ownedFeatures.forEach[e|map.addType(e)]
			s.ownedElements.forEach[e|map.addType(e)]
		}
	}

	private def dispatch void addType(Multimap<DataModel, DataType> map, DataType dataType) {
		if (dataType.eContainer instanceof DataModel) {
			val dataModel = dataType.eContainer as DataModel
			map.put(dataModel, dataType)
		}
	}

	override getModelName(DataModel model) {
		model.name
	}

	override getElementName(DataType element) {
		element.name
	}

	override protected getModels(ZenModel model) {
		model.dataModels
	}

	def getAlias(EObject model) {
		namespaces.get(model.eResource.URI)?.alias
	}
}
