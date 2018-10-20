package com.reprezen.genflow.swagger.doc

import io.swagger.models.Response
import io.swagger.models.Swagger

class ResponseDoc {

	val Swagger swagger = HelperHelper.swagger
	val String status
	extension DocHelper = HelperHelper.docHelper
	extension ResponseHelper = HelperHelper.responseHelper
	extension SchemaHelper = HelperHelper.schemaHelper
	extension HtmlHelper = HelperHelper.htmlHelper
	extension ExamplesHelper = HelperHelper.examplesHelper

	new(String status) {
		this.status = status
	}

	def getHtml() {
		'''«swagger.responses.get(status)?.responseHtml»'''
	}

	def getResponseHtml(Response response) {
		'''
			<a class="anchor toc-entry" id="«response.htmlId»" data-toc-level="1" data-toc-text="«status»"></a>
			<div class="panel panel-default">
			    <div class="panel-heading">
			        <h4 class="panel-title"><strong>«status»</strong></h4>
			    </div>
			    <div class="panel-body">
			        <strong>Response</strong> «chevron» «response.responseSchema?.schemaTitle»</h4>
			        «response.description?.docHtml»
			        «response.responseSchema?.renderSchema»
			        «response.getResponseHeaders»
			        «response.examples.renderExamples»
			    </div>
			</div>
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
}
