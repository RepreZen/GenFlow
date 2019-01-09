/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test;

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

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("Customer.rapid")
public class GeneratedCustomerXsdIntegrationTest {
    @Rule
    public XsdGeneratorIntegrationTestFixture fixture = new XsdGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedXsdIsValidSchema() {
        fixture.assertGeneratedXsdIsValidSchema();
    }

    @Test
    public void testTargetNamespace() throws Exception {
        Node schema = fixture.requireSchema();
        assertThat(schema, hasTargetNamespace("http://modelsolv.com/reprezen/schemas/customer/customer"));
    }

    @Test
    public void testAtomNsDeclaration() throws Exception {
        Node schema = fixture.requireSchema();
        assertThat(schema, hasAtomNsDeclaration());
    }

    @Test
    public void testGlobalTypeForCustomerResource() throws Exception {
        fixture.requireGlobalComplexType("CustomerObject");
    }

    /**
     * Expected content: <xs:complexType name="CustomerResource"> <xs:sequence>
     * <xs:element name="ordersList" minOccurs="0" maxOccurs="1"> <xs:complexType> <xs:sequence>
     * <xs:element name="order" type="CustomerResource_Order" minOccurs="1" maxOccurs="unbounded" /> </xs:sequence>
     * </xs:complexType> </xs:element> </xs:sequence> <xs:attribute name="customerID" type="xs:string" use="required" />
     * <xs:attribute name="customerName" type="xs:string" use="required" />
     * 
     * </xs:complexType>
     */
    @Test
    public void testCustomerResource_OrderReference() throws Exception {
        Node node = fixture.requireAllBlockElement("CustomerObject", "ordersList");
        assertThat(node, hasMinOccurs(0));
        assertThat(node, hasMaxOccurs(1));
        Node listItem = getListItem().apply(node);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());
        assertThat(listItem, hasName("item"));
        assertThat(listItem, hasType("CustomerObject_Orders"));
    }

    /**
     * Expected content: <xs:complexType name="CustomerResource_Orders"> <xs:sequence>
     * <xs:element name="lineItemsList" minOccurs="0" maxOccurs="1"> <xs:complexType> <xs:sequence> <!-- The difference
     * - the type is different LineItem -->
     * <xs:element name="lineItem" type="CustomerResource_Order_LineItem_With_ProductLink" minOccurs="1" maxOccurs=
     * "unbounded" /> </xs:sequence> </xs:complexType> </xs:element> </xs:sequence>
     * <xs:attribute name="orderID" type="xs:string" use="required" />
     * <xs:attribute name="quantity" type="xs:decimal" use="required" />
     * <xs:attribute name="price" type="xs:decimal" use="required" />
     * <xs:attribute name="currency" type="xs:string" use="required" />
     * 
     * </xs:complexType>
     */
    @Test
    public void testCustomerResource_Order_Type() throws Exception {
        fixture.requireGlobalComplexType("CustomerObject_Orders");
        Node node = fixture.requireAllBlockElement("CustomerObject_Orders", "lineItemsList");
        assertThat(node, hasMinOccurs(0));
        assertThat(node, hasMaxOccurs(1));
        Node listItem = getListItem().apply(node);
        assertNotNull(listItem);
        assertThat(listItem, hasMinOccurs(1));
        assertThat(listItem, isUnbounded());
        assertThat(listItem, hasName("item"));
        assertThat(listItem, hasType("CustomerObject_Orders_LineItems"));
    }

    /**
     * Expected content: <xs:complexType name="CustomerResource_Orders_LineItems__With_Product_Link">
     * <xs:complexContent> <xs:extension base="LineItem"> <xs:sequence>
     * <xs:element name="product" minOccurs="0" maxOccurs="1"> <xs:complexType> <xs:sequence>
     * <xs:element ref="atom:link" minOccurs="1" maxOccurs="1" /> </xs:sequence> <!-- metadata properties as specified
     * in the LinkDescriptor --> <xs:attribute name="productID" type="xs:string" use="required" /> </xs:complexType>
     * </xs:element> </xs:sequence> </xs:extension> </xs:complexContent> </xs:complexType>
     */
    @Test
    public void testCustomerResource_Order_LineItem_With_ProductLink_Type() throws Exception {
        fixture.requireGlobalComplexType("CustomerObject_Orders_LineItems");
    }

    @Test
    public void testCustomerResourceXmlInstance() throws Exception {
        fixture.validateXmlAgainstGeneratedSchema("customer_customerObject.xml");
    }

}
