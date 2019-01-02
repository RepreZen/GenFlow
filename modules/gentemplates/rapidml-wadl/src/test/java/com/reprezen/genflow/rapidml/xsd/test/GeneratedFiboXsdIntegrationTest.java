/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test;

import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasMaxOccurs;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasMinOccurs;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasName;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasTargetNamespace;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.isUnbounded;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getListItem;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("FiboEntity.rapid")
public class GeneratedFiboXsdIntegrationTest {
    @Rule
    public XsdGeneratorIntegrationTestFixture fixture = new XsdGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedXsdIsValidSchema() {
        fixture.assertGeneratedXsdIsValidSchema();
    }

    @Test
    public void testComplexTypeGeneratedForAutonomousEntityDataType() throws Exception {
        fixture.requireGlobalComplexType("AutonomousEntity");
    }

    @Test
    public void testAutonomousEntity_legalPersonReference() throws Exception {
        NodeList list = fixture.getAllBlockElements("AutonomousEntity", "legalPerson");
        assertThat("References should not be generated.", list.getLength(), equalTo(0));
    }

    @Test
    public void testListType() throws Exception {
        Node node = fixture.requireAllBlockElement("RegisteredAddress", "addressLineList");
        assertThat(node, hasMinOccurs(1));
        assertThat(node, hasMaxOccurs(1));
        Node listItem = getListItem().apply(node);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());
        assertThat(listItem, hasName("item"));
    }

    @Test
    public void testReferenceListType() throws Exception {
        // references should not be generated
        NodeList list = fixture.getAllBlockElements("LegalEntityObject", "hasCapacityOfList");
        assertThat("References should not be generated.", list.getLength(), equalTo(0));
    }

    @Test
    public void testTargetNamespace() throws Exception {
        Node schema = fixture.requireSchema();
        assertThat(schema, hasTargetNamespace("http://modelsolv.com/reprezen/schemas/fiboentity/fiboentity"));
    }

    @Test
    public void testLegalPersonXmlInstance() throws Exception {
        fixture.validateXmlAgainstGeneratedSchema("fibo_legalPerson.xml");
    }
}
