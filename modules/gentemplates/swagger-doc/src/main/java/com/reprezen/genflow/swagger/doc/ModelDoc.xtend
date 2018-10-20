/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.doc

import io.swagger.models.Model
import io.swagger.models.Swagger

class ModelDoc {

    val Swagger swagger = HelperHelper.swagger
    val String name
    val Model definition

    extension SchemaHelper = HelperHelper.schemaHelper
    extension DocHelper = HelperHelper.docHelper
    extension HtmlHelper = HelperHelper.htmlHelper

    new(String name) {
        this.name = name
        this.definition = swagger.definitions.get(name)
    }

    def getHtml() {
        '''
            <a class="anchor toc-entry" id="«definition.htmlId»" data-toc-level=1 data-toc-text="«name»"></a>
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4 class="panel-title">«name»</h4>
                </div>
                <div class="panel-body">
                    «definition.description.docHtml»
                    «definition.renderSchema»
                </div>
            </div>
        '''
    }

}
