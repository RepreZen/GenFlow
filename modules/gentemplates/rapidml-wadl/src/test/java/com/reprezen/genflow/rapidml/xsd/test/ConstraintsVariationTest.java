/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.junit4.util.ParseHelper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.FakeGenTemplateContext;
import com.reprezen.genflow.api.zenmodel.ZenModelSource;
import com.reprezen.genflow.rapidml.xsd.ResourceApiSchemaGenerator;
import com.reprezen.genflow.test.common.RapidMLInjectorProvider;
import com.reprezen.rapidml.ZenModel;
import com.reprezen.rapidml.implicit.ZenModelNormalizer;

@InjectWith(RapidMLInjectorProvider.class)
@RunWith(XtextRunner.class)
public class ConstraintsVariationTest extends Assert {

    @Inject
    ParseHelper<ZenModel> modelParser;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void stringLength_FromTest() throws Exception {
        assertStringConstraint("string", "length from 1", "1", null, null);
    }

    @Test
    public void stringLength_ToTest() throws Exception {
        assertStringConstraint("string", "length to 10", null, "10", null);
    }

    @Test
    public void stringLength_FromToEqualsTest() throws Exception {
        assertStringConstraint("string", "length from 10 to 10", "10", "10", null);
    }

    @Test
    public void stringLength_FromToTest() throws Exception {
        assertStringConstraint("string", "length from 0 to 10", null, "10", null);
    }

    @Test
    public void regexTest() throws Exception {
        assertStringConstraint("string", "matching regex \"[A-Z]+\"", null, null, "[A-Z]+");
    }

    @Test
    public void valueRange_FromExToIncTest() throws Exception {
        assertNumberConstraint("double", "valueRange from '1.0' exclusive to '2.0' inclusive", "1.0", "2.0", true,
                null);
    }

    @Test
    public void valueRange_FromIncToExTest() throws Exception {
        assertNumberConstraint("double", "valueRange from '1.0' inclusive to '2.0' exclusive", "1.0", "2.0", null,
                true);
    }

    @Test
    public void valueRange_FromTest() throws Exception {
        assertNumberConstraint("double", "valueRange from minimum '1.0'", "1.0", null, null, null);
    }

    @Test
    public void valueRange_ToTest() throws Exception {
        assertNumberConstraint("double", "valueRange up to '2.0'", null, "2.0", null, null);
    }

    private void assertNumberConstraint(String type, String constraint, String minimum, String maximum,
            Boolean minExclusive, Boolean maxExclusive) throws Exception {
        NodeList nodeList = getConstraints(type, constraint);
        int count = 0;
        if (minimum != null) {
            count++;
            assertConstraint(nodeList, Boolean.TRUE.equals(minExclusive) ? "minExclusive" : "minInclusive", minimum);
        }
        if (maximum != null) {
            count++;
            assertConstraint(nodeList, Boolean.TRUE.equals(maxExclusive) ? "maxExclusive" : "maxInclusive", maximum);
        }
        assertEquals(count, nodeList.getLength());
    }

    private void assertStringConstraint(String type, String constraint, String minLength, String maxLength,
            String pattern) throws Exception {
        NodeList nodeList = getConstraints(type, constraint);
        int count = 0;
        if (minLength != null && minLength.equals(maxLength)) {
            count++;
            assertConstraint(nodeList, "length", minLength);
        } else {
            if (minLength != null) {
                count++;
                assertConstraint(nodeList, "minLength", minLength);
            }
            if (maxLength != null) {
                count++;
                assertConstraint(nodeList, "maxLength", maxLength);
            }
        }
        if (pattern != null) {
            count++;
            assertConstraint(nodeList, "pattern", pattern);
        }
        assertEquals(count, nodeList.getLength());
    }

    private void assertConstraint(NodeList nodeList, String nodeName, String nodeValue) {
        boolean found = false;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if (item.getNodeName().equals("xs:" + nodeName)) {
                assertEquals(nodeValue, item.getAttributes().getNamedItem("value").getNodeValue());
                found = true;
            }
        }
        if (!found) {
            fail("Constraint '" + nodeName + "' not found");
        }
    }

    private NodeList getConstraints(String type, String constraint) throws Exception {
        ZenModel zenModel = modelParser.parse(getModel(type, constraint));
        new ZenModelNormalizer().normalize(zenModel);
        FakeGenTemplateContext context = new FakeGenTemplateContext(new ZenModelSource(new File("dummy.rapid")) {

            @Override
            public ZenModel load(File inFile) throws GenerationException {
                return zenModel;
            }
        });
        context.setupTraces();

        ResourceApiSchemaGenerator genXsd = new ResourceApiSchemaGenerator();
        genXsd.init(context);
        String content = genXsd.generate(zenModel, zenModel.getResourceAPIs().get(0));

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.newSchema(new StreamSource(new ByteArrayInputStream(content.getBytes())));
        NodeList list = (NodeList) query(content,
                "/schema/complexType[@name='MyObject']/attribute[@name='prop1']/simpleType/restriction/*[@value]",
                XPathConstants.NODESET);
        if (list == null) {
            fail("Constraints not found in content: " + content);
        }
        return list;
    }

    private Object query(String content, String query, QName returnType) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(content.getBytes()));
        XPathFactory xFactory = XPathFactory.newInstance();
        XPath xpath = xFactory.newXPath();
        XPathExpression expr = xpath.compile(query);
        Object result = expr.evaluate(doc, returnType);
        return result;
    }

    /**
     * @param constraints
     * @return
     */
    private String getModel(String type, String constraint) {
        StringBuilder sb = new StringBuilder();
        sb.append("rapidModel ResourceRealizationConstraints \n");
        sb.append("\tresourceAPI ResourceAPI baseURI \"baseURI\"\n");
        sb.append("\t\tobjectResource MyObject type Structure\n");
        sb.append("\tdataModel DataModel\n");
        sb.append("\t\tstructure Structure\n");
        sb.append("\t\t\tprop1: " + type + "\n");
        sb.append("\t\t\t\t" + constraint + "\n");
        return sb.toString();
    }
}
