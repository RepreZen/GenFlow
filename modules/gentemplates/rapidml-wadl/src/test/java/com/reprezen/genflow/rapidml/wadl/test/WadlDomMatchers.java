/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.test;

import static com.reprezen.genflow.rapidml.wadl.XmlDomMatchers.hasValue;

import org.hamcrest.Matcher;
import org.w3c.dom.Node;

public class WadlDomMatchers {

    public static Matcher<Node> hasStatus(Integer expectedValue) {
        return hasValue("status", expectedValue.toString());
    }

    public static Matcher<Node> hasType(String expectedValue) {
        return hasValue("type", expectedValue);
    }

    public static Matcher<Node> hasName(String expectedValue) {
        return hasValue("name", expectedValue);
    }

    public static Matcher<Node> hasStyle(String expectedValue) {
        return hasValue("style", expectedValue);
    }

    public static Matcher<Node> hasMaxOccurs(String expectedValue) {
        return hasValue("maxOccurs", expectedValue);
    }

    public static Matcher<Node> hasMinOccurs(String expectedValue) {
        return hasValue("minOccurs", expectedValue);
    }

    public static Matcher<Node> hasMediaType(String expectedValue) {
        return hasValue("mediaType", expectedValue);
    }

    public static Matcher<Node> hasElement(String expectedValue) {
        return hasValue("element", expectedValue);
    }

    public static Matcher<Node> validUnsetSelfLink() {
        return hasValue("rel", null);
    }

    public static Matcher<Node> validUnsetRelatedLink() {
        return hasValue("rel", null);
    }
}
