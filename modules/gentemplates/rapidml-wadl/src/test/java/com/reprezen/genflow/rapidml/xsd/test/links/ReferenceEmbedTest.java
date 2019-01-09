/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test.links;

import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasType;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasUse;
import static com.reprezen.genflow.rapidml.xsd.test.XsdFunctions.getAllBlockItem;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.rapidml.xsd.test.XsdGeneratorIntegrationTestFixture;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("perspective/ReferenceEmbed.rapid")
public class ReferenceEmbedTest {
    @Rule
    public XsdGeneratorIntegrationTestFixture fixture = new XsdGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedXsdIsValidSchema() {
        fixture.assertGeneratedXsdIsValidSchema();
    }

    @Test
    public void testReferenceEmbedObject() throws Exception {
        String complexTypeName = "ReferenceEmbedObject";
        fixture.requireGlobalComplexType(complexTypeName);
        // attributes
        requireAttribute(complexTypeName, "prop1", "xs:string");
        requireAttribute(complexTypeName, "prop2", "xs:string");
        // embeds
        Node sequence = fixture.requireAllBlockElement(complexTypeName, "order");
        assertThat(sequence, hasType("ReferenceEmbedObject_order"));
        // references
        requireLink(complexTypeName, "target");
    }

    @Test
    public void testReferenceEmbedObject_order() throws Exception {
        String complexTypeName = "ReferenceEmbedObject_order";
        fixture.requireGlobalComplexType(complexTypeName);
        // attributes
        requireAttribute(complexTypeName, "orderID", "xs:string");
        requireAttribute(complexTypeName, "orderDate", "xs:string");
        // embeds
        Node sequence = fixture.requireAllBlockElement(complexTypeName, "lineItems");
        assertThat(sequence, hasType("ReferenceEmbedObject_order_lineItems"));
        // no references
    }

    @Test
    public void testReferenceEmbedObject_order_lineItems() throws Exception {
        String complexTypeName = "ReferenceEmbedObject_order_lineItems";
        fixture.requireGlobalComplexType(complexTypeName);
        // attributes
        requireAttribute(complexTypeName, "id", "xs:string");
        // embeds
        Node sequence = fixture.requireAllBlockElement(complexTypeName, "product");
        assertThat(sequence, hasType("ReferenceEmbedObject_order_lineItems_product"));
        // no references
    }

    @Test
    public void testReferenceEmbedObject_order_lineItems_product() throws Exception {
        String complexTypeName = "ReferenceEmbedObject_order_lineItems_product";
        fixture.requireGlobalComplexType(complexTypeName);
        // attributes
        requireAttribute(complexTypeName, "productID", "xs:string");
        requireAttribute(complexTypeName, "productName", "xs:string");
        requireAttribute(complexTypeName, "productPrice", "xs:string");
        // embeds
        // references
        requireLink(complexTypeName, "image");
    }

    @Test
    public void testReferenceEmbedXmlInstance() throws Exception {
        fixture.validateXmlAgainstGeneratedSchema("ReferenceEmbed.xml");
    }

    protected Node requireAttribute(String complexTypeName, String attributeName, String attributeType)
            throws Exception {
        Node node = fixture.requireAttributeOfComplexType(complexTypeName, attributeName);
        assertThat(node, hasType(attributeType));
        assertThat(node, hasUse("optional"));
        return node;
    }

    protected Node requireLink(String complexTypeName, String sequenceElementName) throws Exception {
        Node sequence = fixture.requireAllBlockElement(complexTypeName, sequenceElementName);
        Node atomLink = getAllBlockItem().apply(sequence);
        assertNotNull(atomLink);
        fixture.assertIsCorrectAtomLink(atomLink);
        return atomLink;
    }

}