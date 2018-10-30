/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.doc

import io.swagger.models.HttpMethod
import io.swagger.models.Operation
import io.swagger.models.Response
import io.swagger.models.parameters.BodyParameter

class OpDoc {
	val Operation op

	extension DocHelper = HelperHelper.docHelper
	extension TagHelper = HelperHelper.tagHelper
	extension SchemaHelper = HelperHelper.schemaHelper
	extension ResponseHelper = HelperHelper.responseHelper
	extension HtmlHelper = HelperHelper.htmlHelper
	extension MiscHelper = HelperHelper.miscHelper
	extension OptionHelper = HelperHelper.optionHelper
	extension AttributeHelper = HelperHelper.attributeHelper
	extension ExamplesHelper = HelperHelper.examplesHelper

	new(Operation op) {
		this.op = op;
	}

	def getHtml(HttpMethod method) {
		'''
			<a class="anchor toc-entry" id="«op.htmlId»" data-toc-level="2" data-toc-text="«method»"></a>
			<span class="label label-primary resource-method">«method»</span>
			<code>«op.operationId.deprecate(op.deprecated)»</code>
			«op.tagBadges»
			«IF preview»
				&nbsp;
				<a href="#">
						<span class="glyphicon glyphicon-edit" style="font-size: 1.5em;" data-toggle="tooltip" title="Go to definition in editor." 
						onclick="reprezen_changeSelection('«op.pointer»', '«op.getRZVE.fileUrl»');return false;"></span>
				</a>
			«ENDIF»
			<ul class="list-group">
			    <li class="list-group-item">«op.summary.getDocHtml(op.description)»</li>
			    <li class="list-group-item">
			    <h4>Media Types</h4>
			    <table class="table">
			        <tr><th>Consumes</th><td>«MimeTypeHelper.getHtml(op.consumes)»</td></tr>
			        <tr><th>Produces</th><td>«MimeTypeHelper.getHtml(op.produces)»</td></tr>
			    </table>
			    </li>
			    «new ParamsDoc().paramsHtml(op.nonBodyParameters)»
			    «op.getMessageBodyHtml(method)»
			</ul>
		  '''
	}

	def getNonBodyParameters(Operation op) {
		op.parameters?.filter[it.in != "body"].toList
	}

	def getBodyParameter(Operation op) {
		op.parameters?.filter[it.in == "body"].last as BodyParameter
	}

	def getMessageBodyHtml(Operation op, HttpMethod method) {
		return '''
			«op.bodyParameter?.requestHtml»
			«FOR code : op.responses.keySet.sortByPosition(op.responses)»«op.responses.get(code).getResponseHtml(code)»«ENDFOR»
		'''
	}

	def getRequestHtml(BodyParameter bodyParam) {
		'''
			<li class="list-group-item">
			    <strong>Request</strong> «chevron» «bodyParam.schema.schemaTitle»
			    «bodyParam.schema.renderSchema»
			    «bodyParam.examples.renderExamples»
			</li>
		'''
	}

	def getResponseHtml(Response response, String status) {
		'''
			<li class="list-group-item">
			    <strong>Response</strong> «status.statusLabel» «chevron» «response.responseSchema?.schemaTitle»
			    «response.description?.docHtml»
			    «response.responseSchema?.renderSchema» 
			    «response.responseHeaders»
			    «response.examples.renderExamples»
			</li>
		'''
	}

	def private getResponseHeaders(Response response) {
		val html = response.getHeadersHtml
		if (html !== null) {
			'''
				<h4>Headers</h4>
				«html»
			'''
		}
	}

	def chevron() {
		'<span class="glyphicon glyphicon-chevron-right"></span>'
	}

	def statusLabel(String status) {
		val context = try {
				switch s: Integer.parseInt(status) {
					case s >= 100 && s < 200: // Informational
						"info"
					case s >= 200 && s < 300: // Success
						"success"
					case s >= 300 && s < 400: // Redirection
						"info"
					case s >= 400 && s < 500: // Bad request
						"danger"
					case s >= 500 && s < 600: // server error
						"danger"
					default:
						"default"
				}
			} catch (NumberFormatException e) {
				status // handle "default" case
			}
		'''<span class="label label-«context»">«status»</span>'''
	}

	def deprecate(String text, Boolean deprecated) {
		if (deprecated !== null && deprecated) '''<S>«text»</S>''' else text
	}
}
