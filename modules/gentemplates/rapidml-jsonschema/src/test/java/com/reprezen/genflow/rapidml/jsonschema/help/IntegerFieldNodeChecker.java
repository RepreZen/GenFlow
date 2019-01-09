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

public class IntegerFieldNodeChecker extends JsonNodeChecker {
    public final Integer integerValue;

    public IntegerFieldNodeChecker(Integer integerValue) {
        super(true);
        this.integerValue = integerValue;
    }

    @Override
    public void check(JsonNode node) {
        Assert.assertEquals("Unexpected integer node value", integerValue, (Integer) node.asInt()); //$NON-NLS-1$
    }
}
