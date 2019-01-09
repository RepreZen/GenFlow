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

public class StringFieldNodeChecker extends JsonNodeChecker {

    public final String stringValue;

    public StringFieldNodeChecker(String stringValue) {
        super(true);
        this.stringValue = stringValue;
    }

    @Override
    public void check(JsonNode node) {
        Assert.assertEquals("Unexpected string node value", stringValue, node.asText()); //$NON-NLS-1$
    }
}
