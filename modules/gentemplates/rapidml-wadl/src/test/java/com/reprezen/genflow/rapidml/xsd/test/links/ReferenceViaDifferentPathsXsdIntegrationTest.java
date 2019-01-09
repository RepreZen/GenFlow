/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test.links;

import static com.reprezen.genflow.rapidml.wadl.test.links.TwoReferenceLinksTest.TWO_REFERENCES_SAMPLE_XML;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasAtomNsDeclaration;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasMaxOccurs;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasMinOccurs;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasName;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasTargetNamespace;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasType;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.isUnbounded;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getListItem;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.rapidml.xsd.test.XsdGeneratorIntegrationTestFixture;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("links/TwoReferences.rapid")
public class ReferenceViaDifferentPathsXsdIntegrationTest {
    @Rule
    public XsdGeneratorIntegrationTestFixture fixture = new XsdGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedXsdIsValidSchema() {
        fixture.assertGeneratedXsdIsValidSchema();
    }

    @Test
    public void testTargetNamespace() throws Exception {
        Node schema = fixture.requireSchema();
        assertThat(schema, hasTargetNamespace("http://modelsolv.com/reprezen/schemas/tworeferences/tworeferences"));
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
        Node sequence = fixture.requireAllBlockElement("DataType1Object", "dataType1_containment1List");
        assertThat(sequence, hasMinOccurs(0));
        assertThat(sequence, hasMaxOccurs(1));
        Node listItem = getListItem().apply(sequence);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());
        assertThat(listItem, hasName("item"));
        assertThat(listItem, hasType("DataType1Object_dataType1_containment1"));
    }

    @Test
    public void testDataType1_ContainmentReference2() throws Exception {
        Node sequence = fixture.requireAllBlockElement("DataType1Object", "dataType1_containment2List");
        assertThat(sequence, hasMinOccurs(0));
        assertThat(sequence, hasMaxOccurs(1));
        Node listItem = getListItem().apply(sequence);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());
        assertThat(listItem, hasName("item"));
        assertThat(listItem, hasType("DataType1Object_dataType1_containment2"));
    }

    @Test
    public void testDataType1Resource_dataType1_containment1_compexType() throws Exception {
        fixture.requireGlobalComplexType("DataType1Object_dataType1_containment1");
    }

    @Test
    public void testDataType1Resource_dataType1_containment2_compexType() throws Exception {
        fixture.requireGlobalComplexType("DataType1Object_dataType1_containment2");
    }

    @Test
    public void testDataType3Resource() throws Exception {
        fixture.requireGlobalComplexType("DataType3Object");
    }

    @Test
    public void testDataType1ResourceXmlInstance() throws Exception {
        fixture.validateXmlAgainstGeneratedSchema(TWO_REFERENCES_SAMPLE_XML);
    }

}
