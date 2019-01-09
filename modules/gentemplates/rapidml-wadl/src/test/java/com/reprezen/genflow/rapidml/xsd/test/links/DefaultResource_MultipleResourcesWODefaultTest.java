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
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getChildAttributeWithName;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.reprezen.genflow.rapidml.xsd.test.XsdGeneratorIntegrationTestFixture;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("links/DefaultResource_MultipleResourcesWODefault.rapid")
public class DefaultResource_MultipleResourcesWODefaultTest {
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
    public void testGlobalTypeForDataType1Resource() throws Exception {
        Node complexType = fixture.requireGlobalComplexType("DataType1Object");
        assertNotNull(getChildAttributeWithName("dataType1ID").apply(complexType));
    }

    @Test
    public void testGlobalTypeForDataType1() throws Exception {
        Node complexType = fixture.getGlobalComplexType("DataType1");
        assertNull(complexType);
    }

    @Test
    public void testGlobalTypeForDataType2() throws Exception {
        /*
         * There is a complext type because there is no DEFAULT definition, none of them is marked as default:
         * objectResource DataType2Resource1 type DataType2 ... objectResource DataType2Resource2 type DataType2 ...
         */
        Node complexType = fixture.requireGlobalComplexType("DataType2");
        assertNotNull(getChildAttributeWithName("dataType2ID").apply(complexType));
    }

    @Test
    public void testDataType1_NonContainmentReference1() throws Exception {
        NodeList node = fixture.getAllBlockElements("DataType1Object", "dataType1_reference1List");
        // because there is no assigned or default resource for the type, a containment will be created
        // assertThat(node, isEmpty());

    }

}
