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

public class BooleanFieldNodeChecker extends JsonNodeChecker {

    public final Boolean booleanValue;

    public BooleanFieldNodeChecker(Boolean booleanValue) {
        super(true);
        this.booleanValue = booleanValue;
    }

    @Override
    public void check(JsonNode node) {
        Assert.assertEquals("Unexpected boolean node value", booleanValue, node.asBoolean()); //$NON-NLS-1$
    }
}
