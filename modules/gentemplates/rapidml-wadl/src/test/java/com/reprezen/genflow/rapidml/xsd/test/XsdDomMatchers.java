/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test;

import static com.reprezen.genflow.rapidml.wadl.XmlDomMatchers.hasValue;
import static com.reprezen.genflow.rapidml.xsd.test.XsdGeneratorIntegrationTestFixture.UNBOUNDED;

import org.hamcrest.Matcher;
import org.w3c.dom.Node;

public class XsdDomMatchers {

	public static Matcher<Node> hasTargetNamespace(String expectedValue) {
		return hasValue("targetNamespace", expectedValue);
	}

	public static Matcher<Node> hasAtomNsDeclaration() {
		return hasValue("xmlns:atom", "http://www.w3.org/2005/Atom");
	}

	public static Matcher<Node> hasName(String expectedValue) {
		return hasValue("name", expectedValue);
	}

	public static Matcher<Node> hasType(String expectedValue) {
		return hasValue("type", expectedValue);
	}

	public static Matcher<Node> hasUse(String expectedValue) {
		return hasValue("use", expectedValue);
	}

	public static Matcher<Node> hasMaxOccurs(Integer expectedValue) {
		return hasMaxOccurs(expectedValue.toString());
	}

	public static Matcher<Node> isUnbounded() {
		return hasValue("maxOccurs", UNBOUNDED);
	}

	public static Matcher<Node> hasMaxOccurs(String expectedValue) {
		return hasValue("maxOccurs", expectedValue);
	}

	public static Matcher<Node> hasMinOccurs(Integer expectedValue) {
		return hasValue("minOccurs", expectedValue.toString());
	}

}
