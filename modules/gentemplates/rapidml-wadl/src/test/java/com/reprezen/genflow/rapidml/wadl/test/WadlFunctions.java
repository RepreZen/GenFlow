/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.test;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Function;

public class WadlFunctions {
    public static Function<Node, List<Node>> getWithXPath(final String query) {
        return new Function<Node, List<Node>>() {

            @Override
            public List<Node> apply(Node input) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                List<Node> result = new ArrayList<Node>();
                try {
                    NodeList list = (NodeList) xpath.evaluate(query, input, XPathConstants.NODESET);
                    for (int i = 0; i < list.getLength(); i++) {
                        result.add(list.item(i));
                    }

                } catch (XPathExpressionException e) {
                    throw new RuntimeException(e);
                }
                return result;

            }
        };
    }

    public static Function<Node, List<Node>> getRequests() {
        return getWithXPath("request");
    }

    public static Function<Node, List<Node>> getResponses() {
        return getWithXPath("response");
    }

    public static Function<Node, List<Node>> getRepresentations() {
        return getWithXPath("representation");
    }

    public static Function<Node, List<Node>> getParametersWithId(final String id) {
        return getWithXPath("param[@id='" + id + "']");
    }

    public static Function<Node, List<Node>> getAllParameters() {
        return getWithXPath("param");
    }

    public static Function<Node, List<Node>> getLinks() {
        return getWithXPath("link");
    }
}
