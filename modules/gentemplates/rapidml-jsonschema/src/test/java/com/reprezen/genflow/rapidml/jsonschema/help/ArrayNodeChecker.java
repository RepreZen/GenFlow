/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.help;

public class ArrayNodeChecker extends JsonNodeChecker {

    public ArrayNodeChecker(boolean skipUnknown, Integer min, Integer max, JsonNodeChecker itemsNodeChecker) {
        super(skipUnknown);
        addJsonNodeChecker(JSONSchemaKeywords.TYPE, true, new StringFieldNodeChecker(JSONSchemaKeywords.TYPE_ARRAY));
        addJsonNodeChecker(JSONSchemaKeywords.DESCRIPTION, false, new EmptyNodeChecker());
        if (null != min) {
            addJsonNodeChecker(JSONSchemaKeywords.MIN_ITEMS, true, new IntegerFieldNodeChecker(min));
        }
        if (null != max) {
            addJsonNodeChecker(JSONSchemaKeywords.MAX_ITEMS, true, new IntegerFieldNodeChecker(max));
        }
        addJsonNodeChecker(JSONSchemaKeywords.ITEMS, true, itemsNodeChecker);
    }

}
