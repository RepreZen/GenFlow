/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi3.doc

import com.google.common.collect.Sets
import java.util.Collection
import java.util.Set
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path

class TagHelper implements Helper {
	var  OpenApi3 model
	extension DocHelper docHelper
	extension HtmlHelper htmlHelper

	override init() {
		model = HelperHelper.model
		docHelper = HelperHelper.docHelper
		htmlHelper = HelperHelper.htmlHelper
	}

	def getTagBadges(Operation op) {
		'''
			«FOR tag : op.tags?.reorderTags ?: #[]»
			<span class="float-right">&nbsp;
			<span class="badge badge-dark" data-toggle="tooltip" data-title="«tag.modelTag?.description?.docHtml?.toString?.htmlEscape»" data-html="true">«tag»</span>
			</span>
			«ENDFOR»
		'''
	}

	def getCommonTags(Path path) {
		var Set<String> common
		for (op : path.operations.values) {
			if (common === null) {
				common = Sets.newHashSet(op.tags)
			} else {
				common.retainAll(op.tags)
			}
		}
		(common ?: #[]).reorderTags
	}

	def reorderTags(Collection<String> tags) {
		val orderedTags = Sets.newLinkedHashSet
		for (tag : model.tags.map[it.name].filterNull) {
			if (tags.contains(tag)) {
				orderedTags.add(tag)
			}
		}
		orderedTags.addAll(tags)
		orderedTags
	}

	def getModelTag(String tagName) {
		for (tag : model.tags) {
			if (tag.name == tagName) {
				return tag
			}
		}
		return null
	}
}
