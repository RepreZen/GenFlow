/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.help;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;

public class StringArrayNodeChecker extends JsonNodeChecker {

    private final String[] stringArray;

    public StringArrayNodeChecker(String[] stringArray) {
        super(true);
        this.stringArray = stringArray;
    }

    @Override
    public void check(JsonNode node) {
        Assert.assertArrayEquals("Invalid string values", stringArray, //$NON-NLS-1$
                JSONSchemeGeneratorTestFixture.jsonNodeAsStringArray(node));
    }
}
