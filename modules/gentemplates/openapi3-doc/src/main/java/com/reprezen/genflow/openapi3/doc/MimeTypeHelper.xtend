/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi3.doc

import java.util.Collections
import java.util.List

class MimeTypeHelper implements Helper {

    override init() {}

    def static getHtml(List<String> types) {
        '''
            «IF (types ?: Collections::emptyList).empty »
                <em>None</em>
            «ELSE»
                «FOR type : types»<div><code>«type»</code></div>«ENDFOR»
            «ENDIF»
        '''
    }
}
