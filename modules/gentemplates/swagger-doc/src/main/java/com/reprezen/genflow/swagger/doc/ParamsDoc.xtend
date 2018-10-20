/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.doc

import com.reprezen.genflow.swagger.doc.HelperHelper
import com.reprezen.genflow.swagger.doc.StructureTable
import io.swagger.models.HttpMethod
import io.swagger.models.Path
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import java.util.List

class ParamsDoc {
    val Swagger swagger = HelperHelper.swagger
    val Path path
    val String pathName
    val HttpMethod method
    val List<Parameter> pathParams

    new(String pathName, Path path, HttpMethod method) {
        this.path = path
        this.pathName = pathName
        this.method = method
        this.pathParams = path.parameters ?: #[]
    }

    def paramsHtml(List<Parameter> params) {
        if (!params.empty) {
            val table = StructureTable::get(swagger, #["name", "Name"], #["in", "In"], #["default", "Default"],
                #["type", "Type"], #["doc", "Description"])
            '''
                <li class="list-group-item">
                    <h4>Parameters</h4>
                    «table.render(params, null, null)»
                </li>
            '''
        }
    }
}
