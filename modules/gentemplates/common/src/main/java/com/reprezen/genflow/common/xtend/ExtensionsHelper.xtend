/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.xtend

import com.reprezen.restapi.Extensible
import com.reprezen.restapi.Extension

class ExtensionsHelper {
	def Iterable<Extension> getRapidExtensions(Extensible rapidElement) {
		return rapidElement.getExtensions().filter[it.name.startsWith("x-")];
	}
}
