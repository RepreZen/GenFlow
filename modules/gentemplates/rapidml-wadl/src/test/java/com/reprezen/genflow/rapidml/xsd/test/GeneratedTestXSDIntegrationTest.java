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
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasType;
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

/**
 * Integration tests for the following model: structure DataType1 // properties simpleProp : string simplePropUnbounded
 * : decimal * simplePropBounded : boolean [3..5] // references simpleRef : reference DataType2 containmentRef :
 * containing reference DataType2 simpleRefUnbounded : reference DataType2 * simpleRefUnboundedMandatory : reference
 * DataType2 + containmentRefUnbounded : containing reference DataType2 * simpleRefBounded : reference DataType2 [3..5]
 * containmentRefBounded : containing reference DataType2 [8..15]
 */
@SampleRestFile("TestXSD.rapid")
public class GeneratedTestXSDIntegrationTest {
    @Rule
    public XsdGeneratorIntegrationTestFixture fixture = new XsdGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedXsdIsValidSchema() {
        fixture.assertGeneratedXsdIsValidSchema();
    }

    @Test
    public void testComplexTypeGeneratedForDataType1() throws Exception {
        fixture.requireGlobalComplexType("DataType1");
    }

    /**
     * prop simpleProp : string
     */
    @Test
    public void testSimpleProp() throws Exception {
        Node node = fixture.requireAttributeOfComplexType("DataType1", "simpleProp");
        assertThat(node, hasType("xs:string"));
    }

    /**
     * prop simplePropUnbounded upper -1: decimal
     */
    @Test
    public void testSimplePropUnbounded() throws Exception {
        Node node = fixture.requireAllBlockElement("DataType1", "simplePropUnboundedList");
        assertThat(node, hasMinOccurs(0));
        assertThat(node, hasMaxOccurs(1));
        Node listItem = getListItem().apply(node);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());
        assertThat(listItem, hasName("item"));
        assertThat(listItem, hasType("xs:decimal"));
    }

    /**
     * prop simplePropBounded lower 3 upper 5: boolean
     */
    @Test
    public void testSimplePropBounded() throws Exception {
        Node node = fixture.requireAllBlockElement("DataType1", "simplePropBoundedList");
        assertThat(node, hasMinOccurs(1));
        assertThat(node, hasMaxOccurs(1));
        Node listItem = getListItem().apply(node);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());
        assertThat(listItem, hasName("item"));
        assertThat(listItem, hasType("xs:boolean"));
    }

    /**
     * refProp simpleRef : DataType2
     */
    @Test
    public void testDataType1_simpleRef() throws Exception {
        NodeList list = fixture.getAllBlockElements("DataType1", "simpleRef");
        assertThat("References should not be generated.", list.getLength(), equalTo(0));
    }

    /**
     * containment refProp containmentRef : DataType2
     */
    @Test
    public void testDataType1_containmentRef() throws Exception {
        NodeList list = fixture.getAllBlockElements("DataType1", "containmentRef");
        assertThat("References should not be generated.", list.getLength(), equalTo(0));
    }

    /**
     * refProp simpleRefUnbounded upper -1: DataType2
     */
    @Test
    public void testSimpleRefUnbounded() throws Exception {
        NodeList list = fixture.getAllBlockElements("DataType1", "simpleRefUnboundedList");
        assertThat("References should not be generated.", list.getLength(), equalTo(0));
    }

    /**
     * refProp simpleRefUnboundedMandatory lower 1 upper -1: DataType2
     */
    @Test
    public void testSimpleRefUnboundedMandatory() throws Exception {
        NodeList list = fixture.getAllBlockElements("DataType1", "simpleRefUnboundedMandatoryList");
        assertThat("References should not be generated.", list.getLength(), equalTo(0));

    }

    /**
     * containment refProp containmentRefUnbounded upper -1: DataType2
     */
    @Test
    public void testContainmentRefUnbounded() throws Exception {
        NodeList list = fixture.getAllBlockElements("DataType1", "containmentRefUnboundedList");
        assertThat("References should not be generated.", list.getLength(), equalTo(0));
    }

    /**
     * refProp simpleRefBounded lower 3 upper 5: DataType2
     */
    @Test
    public void testSimpleRefBounded() throws Exception {
        NodeList list = fixture.getAllBlockElements("DataType1", "simpleRefBoundedList");
        assertThat("References should not be generated.", list.getLength(), equalTo(0));

    }

    /**
     * containment refProp containmentRefBounded lower 8 upper 15 : DataType2
     */
    @Test
    public void testContainmentRefBounded() throws Exception {
        NodeList list = fixture.getAllBlockElements("DataType1", "containmentRefBoundedList");
        assertThat("References should not be generated.", list.getLength(), equalTo(0));
    }

}
