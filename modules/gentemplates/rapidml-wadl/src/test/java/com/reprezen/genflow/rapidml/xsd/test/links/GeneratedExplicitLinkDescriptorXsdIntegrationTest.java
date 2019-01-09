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
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasTargetNamespace;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.isUnbounded;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getListItem;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.rapidml.xsd.test.XsdGeneratorIntegrationTestFixture;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("links/ExplicitLinkDescriptor.rapid")
public class GeneratedExplicitLinkDescriptorXsdIntegrationTest {
    @Rule
    public XsdGeneratorIntegrationTestFixture fixture = new XsdGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedXsdIsValidSchema() {
        fixture.assertGeneratedXsdIsValidSchema();
    }

    @Test
    public void testTargetNamespace() throws Exception {
        Node schema = fixture.requireSchema();
        assertThat(schema, hasTargetNamespace(
                "http://modelsolv.com/reprezen/schemas/testexplicitlinkdescriptor/testexplicitlinkdescriptor"));
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
    public void testDataType1Resource_Reference() throws Exception {
        Node sequence = fixture.requireAllBlockElement("DataType1Object", "dataType1_reference1List");
        assertThat(sequence, hasMinOccurs(0));
        assertThat(sequence, hasMaxOccurs(1));
        Node listItem = getListItem().apply(sequence);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());
    }

    @Test
    public void testDataType2Resource() throws Exception {
        fixture.requireGlobalComplexType("DataType2Object");
    }

}
