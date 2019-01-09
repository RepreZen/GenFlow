/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.help;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public class JSONStructureChecker {

    public void checkStructureIdentical(JsonNode expected, JsonNode actual) {
        doCheckStructureIdentical(new LinkedList<>(), expected, actual);
    }

    protected void doCheckStructureIdentical(LinkedList<Object> trace, JsonNode expected, JsonNode actual) {
        if (expected == actual) {
            return;
        }

        String tracePath = String.valueOf(trace);
        if (expected == null) {
            throw new IllegalStateException("Expected can't be null: path: " + tracePath);
        }
        Assert.assertNotNull("Actual node not found at: " + tracePath, actual);
        Assert.assertNotSame("Actual node not found at: " + tracePath, JsonNodeType.MISSING, actual);

        Assert.assertSame("type at: " + tracePath, expected.getNodeType(), actual.getNodeType());

        switch (expected.getNodeType()) {
        case BINARY:
        case MISSING:
            Assert.fail("Unexpected node found at: " + tracePath + ", type: " + expected.getNodeType());
            break;
        case BOOLEAN:
        case NUMBER:
        case STRING:
        case NULL:
            Assert.assertEquals("toString at: " + tracePath, expected.toString(), actual.toString());
            break;
        case ARRAY:
            Assert.assertEquals("array size at: " + tracePath, //
                    expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                JsonNode expectedChild = expected.get(i);
                JsonNode actualChild = actual.get(i);
                addToStackTraceAndCheck(trace, "[" + i + "]", expectedChild, actualChild);
            }
            break;
        case OBJECT:
        case POJO:
            for (Iterator<Entry<String, JsonNode>> fields = expected.fields(); fields.hasNext();) {
                Entry<String, JsonNode> next = fields.next();
                String nextFieldName = next.getKey();
                JsonNode expectedField = next.getValue();
                JsonNode actualField = actual.get(nextFieldName);
                addToStackTraceAndCheck(trace, "[" + nextFieldName + "]", expectedField, actualField);
            }
            break;
        }
    }

    private void addToStackTraceAndCheck(LinkedList<Object> stack, Object traceId, JsonNode expected, JsonNode actual) {
        stack.addLast(traceId);
        doCheckStructureIdentical(stack, expected, actual);
        Object selfCheck = stack.removeLast();
        Assert.assertSame("problem in checking, stack-trace does not match", selfCheck, traceId);
    }
}