package com.reprezen.genflow.swagger.doc

import com.reprezen.genflow.api.target.ParameterUtils
import com.reprezen.genflow.swagger.doc.Helper
import com.reprezen.genflow.swagger.doc.HelperHelper
import com.reprezen.genflow.swagger.doc.XSwaggerDocGenTemplate

class OptionHelper implements Helper {

	var extension ParameterUtils paramUtils

	override init() {
		paramUtils = new ParameterUtils(HelperHelper.context)
	}

	public static val URL_PREFIX_PARAM = 'urlPrefix'
	public static val ANCHOR_NAME_PARAM = 'anchorName'
	public static val PREVIEW_PARAM = 'preview'

	def getUrlPrefix() {
		URL_PREFIX_PARAM.parameter.map[asString].orElse(null)
	}

	def getAnchorName() {
		ANCHOR_NAME_PARAM.parameter.map[asString].orElse(null)
	}

	def isPreview() {
		PREVIEW_PARAM.parameter.map[asBoolean].orElse(false)
	}

	def isShowComponentModels() {
		XSwaggerDocGenTemplate::SHOW_ALL_OF_COMPONENTS_OPTION.parameter.map[asBoolean].orElse(true)
	}

	def isIncludeTOC() {
		XSwaggerDocGenTemplate::INCLUDE_TABLE_OF_CONTENTS_OPTION.parameter.map[asBoolean].orElse(true)
	}
}
