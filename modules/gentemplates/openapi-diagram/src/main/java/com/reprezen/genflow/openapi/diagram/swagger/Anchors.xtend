/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.swagger

import io.swagger.models.Swagger
import io.swagger.models.Path
import io.swagger.models.Operation

class Anchors {
	def dispatch htmlLink(Swagger swagger) {
		return "";
	}
	
	def dispatch htmlLink(Path path) {
		return "";
	}
	
	def dispatch htmlLink(Operation operation) {
		return "";
	}
}
