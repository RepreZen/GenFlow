/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.openapi3

import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path

class OpenApi3Anchors {
	def dispatch htmlLink(OpenApi3 spec) {
		return "";
	}
	
	def dispatch htmlLink(Path path) {
		return "";
	}
	
	def dispatch htmlLink(Operation operation) {
		return "";
	}
}
