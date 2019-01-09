/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test.links;

import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasAtomNsDeclaration;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasMaxOccurs;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasMinOccurs;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasName;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasType;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getAllBlockItem;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getChildAttributeWithName;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.rapidml.xsd.test.XsdGeneratorIntegrationTestFixture;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("links/ReferencesLinksWithDifferentDepth.rapid")
public class ReferencesWithDifferentDepthTest {
    @Rule
    public XsdGeneratorIntegrationTestFixture fixture = new XsdGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedXsdIsValidSchema() {
        fixture.assertGeneratedXsdIsValidSchema();
    }

    @Test
    public void testAtomNsDeclaration() throws Exception {
        Node schema = fixture.requireSchema();
        assertThat(schema, hasAtomNsDeclaration());
    }

    @Test
    public void testGlobalTypeForCDataType1Resource() throws Exception {
        fixture.requireGlobalComplexType("DataType1Object");
    }

    @Test
    public void testDataType1_ContainmentReference1() throws Exception {
        Node element = fixture.requireAllBlockElement("DataType1Object", "dataType1_containment1");
        assertThat(element, hasMinOccurs(0));
        assertThat(element, hasMaxOccurs(1));
        assertThat(element, hasName("dataType1_containment1"));
        assertThat(element, hasType("DataType1Object_dataType1_containment1"));
    }

    @Test
    public void testDataType1_NonContainmentReference1() throws Exception {
        Node element = fixture.requireAllBlockElement("DataType1Object", "dataType1_reference1");
        Node atomLink = getAllBlockItem().apply(element);
        assertNotNull(atomLink);
        fixture.assertIsCorrectAtomLink(atomLink, "dataType1_reference1");
    }

    @Test
    public void testDataType1Resource_dataType1_containment1_complexType() throws Exception {
        fixture.requireGlobalComplexType("DataType1Object_dataType1_containment1");
    }

    @Test
    public void testDataType1Resource_dataType1_containment1_containment1() throws Exception {
        Node element = fixture.requireAllBlockElement("DataType1Object_dataType1_containment1",
                "dataType2_containmente1");
        assertThat(element, hasMinOccurs(0));
        assertThat(element, hasMaxOccurs(1));
        assertThat(element, hasName("dataType2_containmente1"));
        assertThat(element, hasType("DataType1Object_dataType1_containment1_dataType2_containmente1"));
    }

    @Test
    public void testDataType1Resource_dataType1_containment1_reference1() throws Exception {
        Node element = fixture.requireAllBlockElement("DataType1Object_dataType1_containment1", "dataType2_reference1");
        Node atomLink = getAllBlockItem().apply(element);
        assertNotNull(atomLink);
        fixture.assertIsCorrectAtomLink(atomLink, "dataType2_reference1");
    }

    @Test
    public void testDataType1Resource_dataType1_containment1_dataType2_containmente1_complexType() throws Exception {
        Node complexType = fixture
                .requireGlobalComplexType("DataType1Object_dataType1_containment1_dataType2_containmente1");
        Node attribute = getChildAttributeWithName("dataType3ID").apply(complexType);
        assertNotNull(attribute);
    }

    @Test
    public void testDataType3Resource() throws Exception {
        fixture.requireGlobalComplexType("DataType3Object");
    }

    @Test
    public void testDataType4Resource() throws Exception {
        fixture.requireGlobalComplexType("DataType4Object");
    }

    @Test
    public void testDataType1ResourceXml() throws Exception {
        fixture.validateXmlAgainstGeneratedSchema("referencesLinksWithDifferentDepth_DataType1Object.xml");
    }

}
