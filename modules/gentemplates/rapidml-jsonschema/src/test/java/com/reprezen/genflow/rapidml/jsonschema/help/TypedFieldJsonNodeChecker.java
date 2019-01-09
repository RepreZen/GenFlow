/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.help;

public class TypedFieldJsonNodeChecker extends JsonNodeChecker {

    public TypedFieldJsonNodeChecker(boolean skipUnknown, String type) {
        super(skipUnknown);
        addJsonNodeChecker(JSONSchemaKeywords.TYPE, true, new StringFieldNodeChecker(type));
        addJsonNodeChecker(JSONSchemaKeywords.DESCRIPTION, false, new EmptyNodeChecker());
    }
}
