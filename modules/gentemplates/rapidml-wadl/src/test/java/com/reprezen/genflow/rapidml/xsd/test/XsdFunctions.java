/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test;

import static com.google.common.base.Objects.equal;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Function;

public class XsdFunctions {
    public static Function<Node, Node> getChildAttributeWithName(final String name) {
        return new Function<Node, Node>() {

            @Override
            public Node apply(Node input) {
                NodeList childNodes = input.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node next = childNodes.item(i);
                    if ("xs:attribute".equals(next.getNodeName()) && equal(name, getValueOf("name").apply(next))) {
                        return next;
                    }
                }
                return null;
            }
        };

    }

    public static Function<Node, String> getValueOf(final String attributeName) {
        return new Function<Node, String>() {

            @Override
            public String apply(Node input) {
                Node item = input.getAttributes().getNamedItem(attributeName);
                return item == null ? null : item.getNodeValue();
            }
        };
    }

    public static Function<Node, Node> getListItem() {
        return new Function<Node, Node>() {

            @Override
            public Node apply(Node input) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                try {
                    return (Node) xpath.evaluate("complexType/sequence/element", input, XPathConstants.NODE);
                } catch (XPathExpressionException e) {
                    throw new RuntimeException(e);
                }

            }
        };
    }

    public static Function<Node, Node> getAllBlockItem() {
        return new Function<Node, Node>() {

            @Override
            public Node apply(Node input) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                try {
                    return (Node) xpath.evaluate("complexType/all/element", input, XPathConstants.NODE);
                } catch (XPathExpressionException e) {
                    throw new RuntimeException(e);
                }

            }
        };
    }

    public static Function<Node, Node> getRestrictionElement() {
        return new Function<Node, Node>() {

            @Override
            public Node apply(Node input) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                try {
                    return (Node) xpath.evaluate("complexType/complexContent/restriction", input, XPathConstants.NODE);
                } catch (XPathExpressionException e) {
                    throw new RuntimeException(e);
                }

            }
        };
    }

    public static Function<Node, Node> getAtomLinkAttribute(final String attributeName) {
        return new Function<Node, Node>() {

            @Override
            public Node apply(Node input) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                try {
                    return (Node) xpath.evaluate("complexType/attribute[@name='" + attributeName + "']", input,
                            XPathConstants.NODE);
                } catch (XPathExpressionException e) {
                    throw new RuntimeException(e);
                }

            }
        };
    }

}
