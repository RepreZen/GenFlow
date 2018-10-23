/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.xtend

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.reprezen.restapi.ImportDeclaration
import com.reprezen.restapi.ZenModel
import com.reprezen.restapi.util.RestapiModelUtils
import java.util.HashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.naming.QualifiedName

/**
 * Helper methods for import statements.
 */
abstract class XGenericImportHelper<M, T extends EObject> {
	public val Multimap<M, T> importedTypes = HashMultimap::create
	public val Multimap<M, T> ownedTypes = HashMultimap::create
	public val Map<URI, ImportDeclaration> namespaces = new HashMap
	public val Map<T, String> qualifiedNames = new HashMap
	public val Map<M, String> qualifiedDataModelNames = new HashMap

	def void init(ZenModel model) {
		calculateImportedTypes(model);
		calculateQualifiedNames()
		calculateDataModelNames()
	}

	def calculateDataModelNames() {
		val Multimap<String, M> nameCollisions = HashMultimap::create
		importedTypes.keySet.forEach[nameCollisions.put(it.modelName, it)]
		ownedTypes.keySet.forEach[nameCollisions.put(it.modelName, it)]

		for (String simpleName : nameCollisions.keySet) {
			val collidedTypes = nameCollisions.get(simpleName)
			if (collidedTypes.size() > 1) {
				collidedTypes.forEach [
					qualifiedDataModelNames.put(
						it,
						if (ownedTypes.keySet.contains(it))
							it.modelName
						else
							getModelFullQualifiedName(it)
					)
				]
			} else {
				collidedTypes.forEach[qualifiedDataModelNames.put(it, it.modelName)]
			}
		}
	}

	abstract def String getModelName(M model);

	abstract def String getElementName(T element);

	def calculateQualifiedNames() {
		val Multimap<String, T> nameCollisions = HashMultimap::create
		importedTypes.values.forEach[nameCollisions.put(it.elementName, it)]
		ownedTypes.values.forEach[nameCollisions.put(it.elementName, it)]

		for (String simpleName : nameCollisions.keySet) {
			val collidedTypes = nameCollisions.get(simpleName)
			if (collidedTypes.size() > 1) {
				collidedTypes.forEach [
					qualifiedNames.put(
						it,
						if (ownedTypes.values.contains(it))
							it.elementName
						else
							getTypeFullQualifiedName(it)
					)
				]
			} else {
				collidedTypes.forEach[qualifiedNames.put(it, it.elementName)]
			}
		}
	}

	def String getQualifiedName(T dataType) {
		qualifiedNames.get(dataType) ?: dataType.elementName
	}

	def String getModelQualifiedName(M dataModel) {
		qualifiedDataModelNames.get(dataModel) ?: dataModel.modelName
	}

	def String getTypeFullQualifiedName(T dataType) {
		if (dataType.eResource !== null && importedTypes.containsValue(dataType)) {
			if (namespaces.containsKey(dataType.eResource.URI)) {
				val model = RestapiModelUtils::getZenModel(dataType)
				val namespace = model?.namespace
				var QualifiedName qname = QualifiedName::EMPTY
				if (namespace !== null) {
					qname = QualifiedName::create(namespace.split("\\."))
				}
				qname = qname.append(
					QualifiedName::create(model.name, (dataType.eContainer as M).modelName, dataType.elementName))
				return qname.toString
			}
			return getModelQualifiedName(getContainer(dataType)) + "." + dataType.elementName
		}
		dataType.elementName

	}

	def M getContainer(T element) {
		return element.eContainer as M
	}

	abstract def String getModelFullQualifiedName(M dataType)

	/**
	 * Multimap of imported type grouped by DataModel
	 * 
	 * @param model Zen Model
	 */
	private def calculateImportedTypes(ZenModel model) {

		// calculate resource namespaces
		for (importDef : model.imports) {
			val rs = EcoreUtil2::getResource(model.eResource, importDef.importURI)
			namespaces.put(rs.URI, importDef)
		}

		// aggregate all types of model
		model.eAllContents.forEach[e|importedTypes.addElement(e)]

		// move current model types
		model.getModels.forEach[e|ownedTypes.putAll(e, importedTypes.removeAll(e))]
	}

	abstract protected def List<M> getModels(ZenModel model)

	abstract protected def void addElement(Multimap<M, T> map, EObject e)

}
