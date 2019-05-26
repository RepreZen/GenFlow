/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi3.doc

import com.reprezen.kaizen.oasparser.model3.Callback
import com.reprezen.kaizen.oasparser.model3.Link
import com.reprezen.kaizen.oasparser.model3.MediaType
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path
import com.reprezen.kaizen.oasparser.model3.RequestBody
import com.reprezen.kaizen.oasparser.model3.Response

class OpDoc {
	val Operation op

	extension KaiZenParserHelper = new KaiZenParserHelper
	extension DocHelper = HelperHelper.docHelper
	extension TagHelper = HelperHelper.tagHelper
	extension SchemaHelper = HelperHelper.schemaHelper
	extension ResponseHelper = HelperHelper.responseHelper
	extension HtmlHelper = HelperHelper.htmlHelper
	extension MiscHelper = HelperHelper.miscHelper
	extension OptionHelper = HelperHelper.optionHelper
	extension ExamplesHelper = HelperHelper.examplesHelper

	new(Operation op, OpenApi3 model, Path path) {
		this.op = op;
	}

	def String getHtml(String method) {
		'''
			<div class="row justify-content-between method-header">
				<div class="row col-10 justify-content-start align-items-center method-badges">
					<span class="badge badge-primary resource-method">«method»</span>
					<code>«op.operationId.deprecate(op.isDeprecated)»</code>
					<a class="anchor toc-entry" id="«op.htmlId»" data-toc-level="2" data-toc-text="«method»"></a>
				</div>
				«op.tagBadges»
			</div>
			«IF preview»
				&nbsp;
				<a href="#">
					<span class="fas fa-edit" style="font-size: 1.5em;" data-toggle="tooltip" title="Go to definition in editor." 
					onclick="reprezen_changeSelection('«op.pointer»', '«op.fileUrl»');return false;"></span>
				</a>
			«ENDIF»
			<ul class="list-group">
			    «op.summary.getDocHtml(op.description)»
			    «new ParamsDoc().paramsHtml(op.nonBodyParameters)»
			    «op.getMessageBodyHtml()»
			    «FOR callback : op.callbacks.keySet»
			    	«op.callbacks.get(callback).getCallbackHtml(callback)»
			    «ENDFOR»
			</ul>
		'''
	}

	def getNonBodyParameters(Operation op) {
		op.parameters?.filter[it.in != "body"].toList
	}

	def getMessageBodyHtml(Operation op /* , HttpMethod method */ ) {
		'''
			«op.requestBody.asNullIfMissing?.getRequestBodyHtml»
			«FOR code : op.responses.keySet.sortByPosition(op.responses)»«op.responses.get(code).getResponseHtml(code)»«ENDFOR»
		'''
	}

	def getRequestBodyHtml(RequestBody bodyParam) {
		'''
			<li class="list-group-item">
			    «FOR contentType : bodyParam.contentMediaTypes.keySet»
			    	<strong>Request</strong> <span class="badge badge-primary">«contentType»</span>«chevron»«bodyParam.contentMediaTypes.get(contentType).schema?.schemaTitle»
			    	«getMediaTypeHtml(contentType, bodyParam.contentMediaTypes.get(contentType))»
			    «ENDFOR»
			</li>
		'''
	}

	def getMediaTypeHtml(String name, MediaType mediaType) {
		''' 
			«mediaType.schema?.description?.docHtml»
			   «mediaType.schema?.renderSchema»
			   «mediaType.renderExample»
			   «mediaType.renderExamples»
		'''
	}

	def getResponseHtml(Response response, String status) {
		'''
			<li class="list-group-item">
				<h4><strong>Response</strong> «status.statusLabel»</h4>
				«response.description?.docHtml»
				«FOR contentType : response.contentMediaTypes.keySet»
					«response.getResponseContentHtml(contentType, response.contentMediaTypes.get(contentType), status)»
					<br/>
				«ENDFOR»
				«response.responseHeaders»
			</li>
		'''
	}

	def getResponseContentHtml(Response response, String contentType, MediaType mediaType, String status) {
		val schema = mediaType?.schema
		'''
			«chevron» <span class="badge badge-primary">«contentType»</span> 
			«schema?.schemaTitle»
			«schema?.renderSchema»
			«mediaType?.renderExample»
			«mediaType?.renderExamples»
			«response.responseLinks»
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

	def getCallbackHtml(Callback callback, String name) {
		'''
			<li class="list-group-item">
				<h4><strong>Callback</strong> «name»</h4>
				«FOR path : callback.callbackPaths.keySet»
					«new PathDoc(path, callback.callbackPaths.get(path)).html»
				«ENDFOR»
			</li>
		'''
	}

	def getResponseLinks(Response response) {
		if (response.links.empty)
			''''''
		else
			'''
				<li class="list-group-item">
					<h4><strong>Links</strong></h4>
					«FOR link : response.links.keySet»
						«link.getLinkHtml(response.links.get(link))»
					«ENDFOR»
				</li>
			'''
	}

	def getLinkHtml(String name, Link link) {
		val parameters = link.parameters.keySet

		'''
			«chevron» «name» <code> «link.operationId» </code>
			&nbsp;
			<p>«link.description»</p>
			<h5>Parameters</h5>
			<table class="table">
				<tr><th>Name</th><th>Value</th></tr>
				«FOR parameter : parameters»
					<tr><th>«parameter»</th><td>«link.parameters.get(parameter)»</td></tr>
				«ENDFOR»
			</table>
		'''
	}

	def chevron() {
		'<span class="fas fa-chevron-right"></span>'
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
						"primary"
				}
			} catch (NumberFormatException e) {
				status // handle "default" case
			}
		'''<span class="badge badge-«context»">«status»</span>'''
	}

	def deprecate(String text, Boolean deprecated) {
		if (deprecated !== null && deprecated) '''<S>«text»</S>''' else text
	}
}
