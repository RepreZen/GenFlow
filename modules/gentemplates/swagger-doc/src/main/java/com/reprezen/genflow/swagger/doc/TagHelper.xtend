/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.doc

import com.google.common.collect.Sets
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Swagger
import java.util.Collection
import java.util.Set

class TagHelper implements Helper {
    private Swagger swagger
    extension DocHelper docHelper
    extension HtmlHelper htmlHelper

    override init() {
        swagger = HelperHelper.swagger
        docHelper = HelperHelper.docHelper
        htmlHelper = HelperHelper.htmlHelper
    }

    def getTagBadges(Operation op) {
        '''
            «FOR tag : op.tags?.reorderTags ?: #[]»
                <span class="pull-right">&nbsp;<span class="badge" data-toggle="tooltip" data-title="«tag.modelTag?.
                description?.docHtml?.toString?.htmlEscape»" data-html="true">«tag»</span></span>
            «ENDFOR»
        '''
    }

    def getCommonTags(Path path) {
        var Set<String> common
        for (op : path.operationMap.values) {
            if (common == null) {
                common = Sets.newHashSet(op.tags)
            } else {
                common.retainAll(op.tags)
            }
        }
        (common ?: #[]).reorderTags
    }

    def reorderTags(Collection<String> tags) {
        val orderedTags = Sets.newLinkedHashSet
        for (tag : swagger.tags.map[it.name].filterNull) {
            if (tags.contains(tag)) {
                orderedTags.add(tag)
            }
        }
        orderedTags.addAll(tags)
        orderedTags
    }

    def getModelTag(String tagName) {
        for (tag : swagger.tags) {
            if (tag.name == tagName) {
                return tag
            }
        }
        return null
    }
}
