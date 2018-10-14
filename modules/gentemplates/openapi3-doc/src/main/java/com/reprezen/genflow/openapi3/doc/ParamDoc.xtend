package com.reprezen.genflow.openapi3.doc

import com.reprezen.genflow.openapi3.doc.StructureTable.ParameterStructureTable
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Parameter

class ParamDoc {
	extension HtmlHelper = HelperHelper.htmlHelper

	val String name
	val OpenApi3 model = HelperHelper.model

	new(OpenApi3 model, String name) {
		this.name = name
	}

	def getHtml() {
		val param = model.parameters.get(name)
		'''
			<a class="anchor toc-entry" id="«param.htmlId»" data-toc-level="1" data-toc-text="«name»"></a>
			<div class="panel panel-default">
			    <div class="panel-heading">
			        <h4 class="panel-title"><strong>«name»</strong></h4>
			    </div>
			    <div class="panel-body">
			        «model.parameters.get(name).paramHtml»
			    </div>
			</div>
		'''
	}

	def paramHtml(Parameter param) {
		nonBodyParamHtml(param)
	}

	def nonBodyParamHtml(Parameter param) {
		val table = new ParameterStructureTable(param, #["name", "Name"], #["in", "In"], #["default", "Default"],
			#["type", "Type"], #["doc", "Description"])
		table.render(param.name)
	}
}
