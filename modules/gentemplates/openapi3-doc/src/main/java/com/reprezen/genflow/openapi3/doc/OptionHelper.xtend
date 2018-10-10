package com.reprezen.genflow.openapi3.doc

import com.reprezen.genflow.api.target.ParameterUtils

class OptionHelper implements Helper {

	var extension ParameterUtils paramUtils

	override init() {
		paramUtils = new ParameterUtils(HelperHelper.context)
	}

	public static val URL_PREFIX_PARAM = 'urlPrefix'
	public static val ANCHOR_NAME_PARAM = 'anchorName'
	public static val PREVIEW_PARAM = 'preview'
	public static val HTML_INJECTIONS_PARAM = 'htmlInjections'
	
	def getUrlPrefix() {
		URL_PREFIX_PARAM.parameter.map[asString].orElse(null)
	}

	def getAnchorName() {
		ANCHOR_NAME_PARAM.parameter.map[asString].orElse(null)
	}

	def isPreview() {
		PREVIEW_PARAM.parameter.map[asBoolean].orElse(null) ?: false
	}

	def isShowComponentModels() {
		XOpenApi3DocGenTemplate::SHOW_ALL_OF_COMPONENTS_OPTION.parameter.map[asBoolean].orElse(null) ?: true
	}

	def isIncludeTOC() {
		XOpenApi3DocGenTemplate::INCLUDE_TABLE_OF_CONTENTS_OPTION.parameter.map[asBoolean].orElse(null) ?: true
	}

	def getHtmlInjections() {
		return HTML_INJECTIONS_PARAM.parameter.map[asObject].orElse(null)
	}
}
