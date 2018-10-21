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
import com.modelsolv.reprezen.restapi.HasSecurityValue
import com.modelsolv.reprezen.restapi.SecurityScheme
import com.modelsolv.reprezen.restapi.SecuritySchemeLibrary
import com.modelsolv.reprezen.restapi.ZenModel
import java.util.Collections
import org.eclipse.emf.ecore.EObject

/**
 * Helper methods for import statements.
 */
class XSecuritySchemeImportHelper extends XGenericImportHelper<SecuritySchemeLibrary, SecurityScheme> {

	override String getModelFullQualifiedName(SecuritySchemeLibrary dataModel) {
		val model = dataModel.eContainer as ZenModel
		(if(model.namespace !== null) "" + model.namespace + "." else "") //
		+ (dataModel.eContainer as ZenModel).name + "." + dataModel.name
	}

	override protected void addElement(Multimap<SecuritySchemeLibrary, SecurityScheme> map, EObject e) {
		addType(map, e)
	}

	private def dispatch void addType(Multimap<SecuritySchemeLibrary, SecurityScheme> map, EObject e) {
	}

	private def dispatch void addType(Multimap<SecuritySchemeLibrary, SecurityScheme> map, HasSecurityValue sdr) {
		sdr.securedBy.forEach[map.addSecuritySchemeType(it.scheme)]
	}

	private def void addSecuritySchemeType(Multimap<SecuritySchemeLibrary, SecurityScheme> map, SecurityScheme scheme) {
		if (scheme.eContainer instanceof SecuritySchemeLibrary) {
			val dataModel = scheme.eContainer as SecuritySchemeLibrary
			map.put(dataModel, scheme)
		}
	}

	override getModelName(SecuritySchemeLibrary model) {
		model.name
	}

	override getElementName(SecurityScheme element) {
		element.name
	}

	override protected getModels(ZenModel model) {
		Collections.singletonList(model.securitySchemesLibrary)
	}

}
