package com.reprezen.genflow.swagger.doc

import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.Parameter

class ParamDoc {
	extension SchemaHelper = HelperHelper.schemaHelper
	extension DocHelper = HelperHelper.docHelper
	extension HtmlHelper = HelperHelper.htmlHelper;
	extension ExamplesHelper = HelperHelper.examplesHelper

	val String name
	val Swagger swagger = HelperHelper.swagger

	new(Swagger swagger, String name) {
		this.name = name
	}

	def getHtml() {
		val param = swagger.parameters.get(name)
		'''
			<a class="anchor toc-entry" id="«param.htmlId»" data-toc-level="1" data-toc-text="«name»"></a>
			<div class="panel panel-default">
			    <div class="panel-heading">
			        <h4 class="panel-title"><strong>«name»</strong></h4>
			    </div>
			    <div class="panel-body">
			        «swagger.parameters.get(name).paramHtml»
			    </div>
			</div>
		'''
	}

	def paramHtml(Parameter param) {
		switch param.in {
			case "body": bodyParamHtml(param)
			default: nonBodyParamHtml(param)
		}
	}

	def bodyParamHtml(Parameter param) {
		val bodyParam = param as BodyParameter
		'''
			«param.description.docHtml»
			«bodyParam.schema.renderSchema»
			«bodyParam.examples.renderExamples»
		'''
	}

	def nonBodyParamHtml(Parameter param) {
		val table = StructureTable::get(swagger, #["name", "Name"], #["in", "In"], #["default", "Default"],
			#["type", "Type"], #["doc", "Description"])
		table.render(param, param.name, null)
	}
}
