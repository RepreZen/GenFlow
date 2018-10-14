/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi3.doc

import com.reprezen.genflow.openapi3.doc.StructureTable.ParametersStructureTable
import com.reprezen.kaizen.oasparser.model3.Parameter
import java.util.List

class ParamsDoc {

	def paramsHtml(List<Parameter> params) {
		if (!params.empty) {
			val table = new ParametersStructureTable(params, #["name", "Name"], #["in", "In"],
				#["default", "Default"], #["type", "Type"], #["doc", "Description"])
			'''
				<li class="list-group-item">
				    <h4>Parameters</h4>
				    
				    «table.render(null)»
				</li>
			'''
		}
	}
}
