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
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasUse;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.isUnbounded;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getListItem;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("realization/CardinalityOverrides.rapid")
public class CardinalityOverrideXsdTest {
    @Rule
    public XsdGeneratorIntegrationTestFixture fixture = new XsdGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedXsdIsValidSchema() {
        fixture.assertGeneratedXsdIsValidSchema();
    }

    @Test
    public void testOptional2Required() throws Exception {
        String resourceName = "Optional2Required";
        Node node = fixture.requireAttributeOfComplexType(resourceName, "optionalProperty");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("required"));
        testDefaultRequiredCardinality(resourceName);
        testDefaultRequiredListCardinality(resourceName);
        testDefaultOptionalListCardinality(resourceName);
    }

    @Test
    public void RequiredList2Required() throws Exception {
        String resourceName = "RequiredList2Required";
        Node node = fixture.requireAttributeOfComplexType(resourceName, "requiredListProperty");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("required"));
        testDefaultOptionalCardinality(resourceName);
        testDefaultRequiredCardinality(resourceName);
        testDefaultOptionalListCardinality(resourceName);
    }

    @Test
    public void OptionalList2RequiredList() throws Exception {
        String resourceName = "OptionalList2RequiredList";
        testRequiredListCardinality(resourceName, "optionalListProperty");
        testDefaultOptionalCardinality(resourceName);
        testDefaultRequiredCardinality(resourceName);
        testDefaultRequiredListCardinality(resourceName);
    }

    @Test
    public void OptionalList2Required() throws Exception {
        String resourceName = "OptionalList2Required";
        Node node = fixture.requireAttributeOfComplexType(resourceName, "optionalListProperty");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("required"));
        testDefaultOptionalCardinality(resourceName);
        testDefaultRequiredCardinality(resourceName);
        testDefaultRequiredListCardinality(resourceName);
    }

    @Test
    public void OptionalList2Optional() throws Exception {
        String resourceName = "OptionalList2Optional";
        Node node = fixture.requireAttributeOfComplexType(resourceName, "optionalListProperty");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("optional"));
        testDefaultOptionalCardinality(resourceName);
        testDefaultRequiredCardinality(resourceName);
        testDefaultRequiredListCardinality(resourceName);
    }

    protected void testDefaultRequiredCardinality(String resourceName) throws Exception {
        Node node = fixture.requireAttributeOfComplexType(resourceName, "requiredProperty");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("required"));
    }

    protected void testDefaultOptionalCardinality(String resourceName) throws Exception {
        Node node = fixture.requireAttributeOfComplexType(resourceName, "optionalProperty");
        assertThat(node, hasType("xs:string"));
        assertThat(node, hasUse("optional"));
    }

    protected void testDefaultRequiredListCardinality(String resourceName) throws Exception {
        testRequiredListCardinality(resourceName, "requiredListProperty");
    }

    protected void testRequiredListCardinality(String resourceName, String propertyName) throws Exception {
        Node node = fixture.requireAllBlockElement(resourceName, propertyName + "List");
        assertThat(node, hasMinOccurs(1));
        assertThat(node, hasMaxOccurs(1));
        Node listItem = getListItem().apply(node);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());
        assertThat(listItem, hasName("item"));
        assertThat(listItem, hasType("xs:string"));
    }

    protected void testDefaultOptionalListCardinality(String resourceName) throws Exception {
        Node node = fixture.requireAllBlockElement(resourceName, "optionalListPropertyList");
        assertThat(node, hasMinOccurs(0));
        assertThat(node, hasMaxOccurs(1));
        Node listItem = getListItem().apply(node);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());
        assertThat(listItem, hasName("item"));
        assertThat(listItem, hasType("xs:string"));
    }

}
