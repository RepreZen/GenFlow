/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.doc

import io.swagger.models.Path
import io.swagger.models.Swagger

class PathDoc {
    val Swagger swagger = HelperHelper.swagger
    val String pathName
    val Path path
    extension DocHelper = HelperHelper.docHelper
    extension TagHelper = HelperHelper.tagHelper
    extension HtmlHelper = HelperHelper.htmlHelper
    extension MiscHelper = HelperHelper.miscHelper
    extension OptionHelper = HelperHelper.optionHelper
    extension AttributeHelper = HelperHelper.attributeHelper

    new(String pathName) {
        this.pathName = pathName
        this.path = swagger.paths.get(pathName)
    }

    def getHtml() {
        '''
            <a class="anchor toc-entry" id="«getHtmlId(path)»" data-toc-level="1" data-toc-text="«pathName.htmlEscape»"></a>
            <div class="panel panel-default">
               <div class="panel-heading">
            		<h4 class="panel-title">
            		          Path: «pathName»
            		          «IF preview»
            		              &nbsp;&nbsp;
            		              <a href="#">
            		                  <span class="glyphicon glyphicon-edit text-primary" style="font-size: 1.3em;" data-toggle="tooltip" title="Go to definition in editor." 
            		                      onclick="reprezen_changeSelection('«path.pointer»', '«path.getRZVE.fileUrl»'); return false;"></span>
            		              </a>
            		          «ENDIF»
            		</h4>
            		  </div>
            		  <div class="panel-body">
            		  «path.commonTags.map[it.modelTag?.description].filterNull.join("\n\n").docHtml»
            		 «FOR method : path.operationMap.keySet.sortByPosition(path.operationMap)»
            		     «new OpDoc(path.operationMap.get(method), swagger, pathName).getHtml(method)»
            		 «ENDFOR»
            		 </div>
            </div>
        '''
    }

}
