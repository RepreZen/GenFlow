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

	def getUrlPrefix() {
		URL_PREFIX_PARAM.parameter.orElse(null)?.asString?.orElse(null)
	}

	def getAnchorName() {
		ANCHOR_NAME_PARAM.parameter.orElse(null)?.asString?.orElse(null)
	}

	def isPreview() {
		PREVIEW_PARAM.parameter.orElse(null)?.asBoolean?.orElse(null) ?: false
	}

	def isShowComponentModels() {
		OpenApi3DocGenTemplate::SHOW_ALL_OF_COMPONENTS_OPTION.parameter.orElse(null)?.asBoolean?.orElse(null) ?: true
	}

	def isIncludeTOC() {
		OpenApi3DocGenTemplate::INCLUDE_TABLE_OF_CONTENTS_OPTION.parameter.orElse(null)?.asBoolean?.orElse(null) ?:
			true
	}
}
