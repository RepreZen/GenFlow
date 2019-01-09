/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.help;

public class EnumNodeChecker extends JsonNodeChecker {

    public EnumNodeChecker(String enumName) {
        super(false);
        addJsonNodeChecker(JSONSchemaKeywords.REF_FIELD_NAME, true,
                new StringFieldNodeChecker(JSONSchemaKeywords.PROPERTIES_DEF_PREFIX + enumName));
        addJsonNodeChecker(JSONSchemaKeywords.DESCRIPTION, false, new EmptyNodeChecker());
    }

}
