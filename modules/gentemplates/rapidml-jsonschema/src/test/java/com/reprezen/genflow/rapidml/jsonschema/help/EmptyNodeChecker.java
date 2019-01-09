/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.help;

import com.fasterxml.jackson.databind.JsonNode;

public class EmptyNodeChecker extends JsonNodeChecker {

    public EmptyNodeChecker() {
        super(true);
    }

    @Override
    public void check(JsonNode node) {
    }
}
