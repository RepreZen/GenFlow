/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi3.doc

import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Schema

class ModelDoc {

    val OpenApi3 model = HelperHelper.model
    val String name
    val Schema definition

    extension SchemaHelper = HelperHelper.schemaHelper
    extension DocHelper = HelperHelper.docHelper
    extension HtmlHelper = HelperHelper.htmlHelper

    new(String name) {
        this.name = name
        this.definition = model.schemas.get(name)
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
