/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test;

import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasType;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasUse;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getAllBlockItem;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getListItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("realization/Customer_includedPropertiesWCardinality.rapid")
public class IncludedPropertiesXsdTest {
    @Rule
    public XsdGeneratorIntegrationTestFixture fixture = new XsdGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedXsdIsValidSchema() {
        fixture.assertGeneratedXsdIsValidSchema();
    }

    @Test
    public void testComplexType() throws Exception {
        fixture.requireGlobalComplexType("CustomerObject");
    }

    @Test
    public void testIncludedPropertyWithOverridenCardinality() throws Exception {
        Node node = fixture.requireAttributeOfComplexType("CustomerObject", "customerID");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("required"));
    }

    @Test
    public void testIncludedPropertyWithDefaultCardinality() throws Exception {
        Node node = fixture.requireAttributeOfComplexType("CustomerObject", "customerName");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("optional"));
    }

    @Test
    public void testExcludedProperties() throws Exception {
        Node node = fixture.getAttributeOfComplexType("CustomerObject", "notIncludedProp1");
        assertThat(node, nullValue());
        node = fixture.getAttributeOfComplexType("CustomerObject", "notIncludedProp2");
        assertThat(node, nullValue());
    }

    /**
     * Tests for the following request
     * 
     * <pre>
     * <code>method POST Customer
     *                 request type Customer
     *                     with all properties
     *                     including
     *                         CustomerName!
     *                     excluding
     *                         NotIncludedProp1
     *                     referenceLink > Orders
     *                         targetResource OrderObject
     * </code>
     * </pre>
     */

    @Test
    public void testRequestComplexType() throws Exception {
        fixture.requireGlobalComplexType("PostCustomerObject_Customer_1");
    }

    @Test
    public void testRequestIncludedPropertyWithOverridenCardinality() throws Exception {
        Node node = fixture.requireAttributeOfComplexType("PostCustomerObject_Customer_1", "customerName");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("required"));
    }

    @Test
    public void testRequestIncludedPropertyWithDefaultCardinality() throws Exception {
        Node node = fixture.requireAttributeOfComplexType("PostCustomerObject_Customer_1", "customerID");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("optional"));
        node = fixture.getAttributeOfComplexType("PostCustomerObject_Customer_1", "notIncludedProp2");
        assertThat(node, notNullValue());
    }

    @Test
    public void testRequestExcludedProperties() throws Exception {
        Node node = fixture.getAttributeOfComplexType("PostCustomerObject_Customer_1", "notIncludedProp1");
        assertThat(node, nullValue());
    }

    @Test
    public void testRequestReferenceLink() throws Exception {
        Node sequence = fixture.requireAllBlockElement("PostCustomerObject_Customer_1", "ordersList");
        Node listItem = getListItem().apply(sequence);
        Node atomLink = getAllBlockItem().apply(listItem);
        assertNotNull(atomLink);
        fixture.assertIsCorrectAtomLink(atomLink);
    }

    /**
     * Tests for the following response
     * 
     * <pre>
     * <code>response type Customer statusCode 200
     *                     with all properties
     *                     including
     *                         CustomerID!
     *                     excluding
     *                         NotIncludedProp2
     * </code>
     * </pre>
     */

    @Test
    public void testResponseComplexType() throws Exception {
        fixture.requireGlobalComplexType("PostCustomerObject_Customer_2");
    }

    @Test
    public void testResponseIncludedPropertyWithOverridenCardinality() throws Exception {
        Node node = fixture.requireAttributeOfComplexType("PostCustomerObject_Customer_2", "customerID");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("required"));
    }

    @Test
    public void testResponseIncludedPropertyWithDefaultCardinality() throws Exception {
        Node node = fixture.requireAttributeOfComplexType("PostCustomerObject_Customer_2", "customerName");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("optional"));
        node = fixture.getAttributeOfComplexType("PostCustomerObject_Customer_2", "notIncludedProp1");
        assertThat(node, notNullValue());
    }

    @Test
    public void testResponseExcludedProperties() throws Exception {
        Node node = fixture.getAttributeOfComplexType("PostCustomerObject_Customer_2", "notIncludedProp2");
        assertThat(node, nullValue());
    }

    @Test
    public void testResponseReferenceEmbed() throws Exception {
        Node sequence = fixture.requireAllBlockElement("PostCustomerObject_Customer_2", "ordersList");
        Node listItem = getListItem().apply(sequence);
        assertThat(listItem, hasType("PostCustomerObject_Customer_2_Orders"));
    }

    protected Node requireHyperlink(String complexTypeName, String sequenceElementName) throws Exception {
        Node sequence = fixture.requireAllBlockElement(complexTypeName, sequenceElementName);
        Node atomLink = getListItem().apply(sequence);
        assertNotNull(atomLink);
        fixture.assertIsCorrectAtomLink(atomLink);
        return atomLink;
    }

    /**
     * Tests for the following request
     * 
     * <pre>
     * <code>method POST PostWithReferenceCardinalityOverride
     *                 request type Customer
     *                     with all properties
     *                     including
     *                         Orders!
     *                     referenceLink > Orders
     *                         targetResource OrderObject
     * </code>
     * </pre>
     */

    @Test
    public void testRequestReferenceLinkWithCardinalityOverride() throws Exception {
        fixture.requireGlobalComplexType("PostCustomerObject_Customer_3");
        Node sequence = fixture.requireSequenceElementInAllBlock("PostCustomerObject_Customer_3", "ordersList", "item");
        Node atomLink = getAllBlockItem().apply(sequence);
        assertNotNull(atomLink);
        fixture.assertIsCorrectAtomLink(atomLink);
    }

}
