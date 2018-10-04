/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi3.doc

import com.reprezen.kaizen.oasparser.model3.Response

class ResponseHelper implements Helper {

	extension HtmlHelper htmlHelper

	override init() {
		htmlHelper = HelperHelper.htmlHelper
	}

	def getHeadersHtml(Response response) {
		val headers = response.headers
		if (headers !== null && !headers.empty) {
			'''
				<table class="table-condensed">
				    <tr><th>Name</th><th>Type</th><th>Description</th></tr>
				    «FOR header : headers.entrySet»
				    	<tr><th>«header.key»</th><td>«header.value?.schema.type»</td><td>«header.value?.description?.htmlEscape»</td></tr>
				    «ENDFOR»
				</table>
			'''
		}
	}
}
