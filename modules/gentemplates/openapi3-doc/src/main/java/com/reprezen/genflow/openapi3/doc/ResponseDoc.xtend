package com.reprezen.genflow.openapi3.doc

import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Response

class ResponseDoc {

	val OpenApi3 model = HelperHelper.model
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
		'''«model.responses.get(status)?.responseHtml»'''
	}

	def getResponseHtml(Response response) {
		val mediaType = response.contentMediaTypes.values.head
		val schema = mediaType?.schema

		'''
			<a class="anchor toc-entry" id="«response.htmlId»" data-toc-level="1" data-toc-text="«status»"></a>
			<div class="panel panel-default">
			    <div class="panel-heading">
			        <h4 class="panel-title"><strong>«status»</strong></h4>
			    </div>
			    <div class="panel-body">
			        <strong>Response</strong> «chevron» «schema?.schemaTitle»</h4>
			        «response.description?.docHtml»
			        «schema?.renderSchema»
			        «response.getResponseHeaders»
			        «mediaType?.renderExample»
			        «mediaType?.renderExamples»
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
