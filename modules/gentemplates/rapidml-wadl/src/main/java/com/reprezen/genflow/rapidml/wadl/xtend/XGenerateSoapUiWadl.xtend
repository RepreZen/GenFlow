/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.xtend

import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource

class XGenerateSoapUiWadl extends XGenerateWadl {

	override generateResourcesElement(ResourceAPI resourceAPI) {
		'''
			<resources base="«resourceAPI.baseURI»">
			«FOR resourceDef : resourceAPI.ownedResourceDefinitions»
				«resourceDef.generateResource»
			«ENDFOR»
			</resources>
		'''
	}

	override generateResource(ResourceDefinition resourceDef) {
		'''
			<resource id="«resourceDef.name»" path="«resourceDef.getURI»">
			«resourceDef.generateParameters»
			«FOR method : resourceDef.methods»
				«method.generateMethod(resourceDef as ServiceDataResource)»
			«ENDFOR»
			</resource>
		'''
	}
}
