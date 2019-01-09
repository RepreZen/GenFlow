/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.help;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jsoup.helper.StringUtil;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeChecker {

    public static class NodeAssertionError extends AssertionError {
        private final String nodeName;

        public NodeAssertionError(String nodeName, Throwable cause) {
            super("Error in \"" + nodeName + "\"", cause); //$NON-NLS-1$ //$NON-NLS-2$
            this.nodeName = nodeName;
        }

        @Override
        public String getMessage() {
            if (getCause() instanceof NodeAssertionError) {
                return "." + nodeName + getCause().getMessage(); //$NON-NLS-1$
            } else {
                return "." + nodeName + ": " + getCause().getMessage(); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    private final Map<String, JsonNodeChecker> fieldCheckers = new HashMap<String, JsonNodeChecker>();
    private boolean skipUnknown;

    private Set<String> requiredFields = new HashSet<String>();

    /**
     * @param skipUnknown
     *            if skipUnknown is true than checker will not fail test on unknown fields. Fields is unknown if checker
     *            for that is not added by {@link #addJsonNodeChecker(String, boolean, JsonNodeChecker)
     *            addJsonNodeChecker}
     */
    public JsonNodeChecker(boolean skipUnknown) {
        this.skipUnknown = skipUnknown;
    }

    public void addJsonNodeChecker(String field, boolean required, JsonNodeChecker checker) {
        fieldCheckers.put(field, checker);
        if (required) {
            requiredFields.add(field);
        }
    }

    public JsonNodeChecker createJsonNodeChecker(String field) {
        JsonNodeChecker propChecker = new JsonNodeChecker(true);
        addJsonNodeChecker(field, skipUnknown, propChecker);
        return propChecker;
    }

    public void checkFieldAbsent(String field) {
        addJsonNodeChecker(field, false, new EmptyNodeChecker());
    }

    public void check(JsonNode node) {
        Iterator<Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Entry<String, JsonNode> entry = it.next();
            String fieldName = entry.getKey();
            requiredFields.remove(fieldName);

            JsonNodeChecker checker = fieldCheckers.get(fieldName);
            try {
                if (null != checker) {
                    checker.check(entry.getValue());
                } else if (!skipUnknown) {
                    Assert.fail("Unexpected field " + fieldName); //$NON-NLS-1$
                }
            } catch (AssertionError error) {
                throw new NodeAssertionError(fieldName, error);
            }
        }

        if (!requiredFields.isEmpty()) {
            Assert.fail("Required fields are missing: " + StringUtil.join(requiredFields, ",")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
