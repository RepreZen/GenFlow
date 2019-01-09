/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl;

import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getValueOf;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Objects;

public class XmlDomMatchers {
	public static Matcher<Node> hasValue(final String attributeName, final String expectedValue) {
		return new BaseMatcher<Node>() {
			@Override
			public boolean matches(Object item) {
				String value = (item instanceof Node) ? getValueOf(attributeName).apply((Node) item) : null;
				return Objects.equal(value, expectedValue);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("attribute \"").appendText(attributeName).appendText("\" is set to \"")
						.appendValue(expectedValue).appendText("\"");
			}

			@Override
			public void describeMismatch(Object item, Description description) {
				String value = (item instanceof Node) ? getValueOf(attributeName).apply((Node) item) : null;
				description.appendText("was ").appendValue(value);
			}

		};
	}

	public static Matcher<NodeList> isEmpty() {
		return new BaseMatcher<NodeList>() {
			@Override
			public boolean matches(Object item) {
				return (item instanceof NodeList) && Objects.equal(((NodeList) item).getLength(), 0);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is empty");
			}
		};
	}

}
