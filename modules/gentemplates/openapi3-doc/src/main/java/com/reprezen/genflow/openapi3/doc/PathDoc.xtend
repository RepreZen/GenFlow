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
import com.reprezen.kaizen.oasparser.model3.Path

class PathDoc {
	val OpenApi3 model = HelperHelper.model
	val String pathName
	val Path path

	extension DocHelper = HelperHelper.docHelper
	extension TagHelper = HelperHelper.tagHelper
	extension HtmlHelper = HelperHelper.htmlHelper
	extension MiscHelper = HelperHelper.miscHelper
	extension OptionHelper = HelperHelper.optionHelper

	new(String pathName) {
		this.pathName = pathName
		this.path = model.paths.get(pathName)
	}

	new(String pathName, Path path) {
		this.pathName = pathName
		this.path = path
	}

	def getHtml() {
		'''
			<a class="anchor toc-entry" id="«getHtmlId(path)»" data-toc-level="1" data-toc-text="«pathName.htmlEscape»"></a>
			<div class="card">
			   <div class="card-header">
					<h4 class="card-title">
					          Path: «pathName»
					          «IF preview»
					          	&nbsp;&nbsp;
					          	<a href="#">
					          	    <span class="glyphicon glyphicon-edit text-primary" style="font-size: 1.3em;" data-toggle="tooltip" title="Go to definition in editor." 
					          	        onclick="reprezen_changeSelection('«path.pointer»', '«path.fileUrl»'); return false;"></span>
					          	</a>
					          «ENDIF»
					</h4>
					  </div>
					  <div class="card-body">
					  «path.commonTags.map[it.modelTag?.description].filterNull.join("\n\n").docHtml»
					 «FOR method : path.operations.keySet.sortByPosition(path.operations)»
					 	«new OpDoc(path.operations.get(method), model, path).getHtml(method)»
					 «ENDFOR»
					 </div>
			</div>
		'''
	}

}
