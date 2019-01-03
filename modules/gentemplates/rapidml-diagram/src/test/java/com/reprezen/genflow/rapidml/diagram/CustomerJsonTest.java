/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram;

import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.ID;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.NAME;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.OBJECTTYPE;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.REFERENCE_RESOURCE_ID;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.RESOURCE_TYPE;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.STATUS_CODE;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("Customer.rapid")
public class CustomerJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testCustomerResource() throws Exception {
        fixture.assertObjectResourceHasName(0, "CustomerObject");
    }

    @Test
    public void testProductResource() throws Exception {
        fixture.assertObjectResourceHasName(1, "ProductObject");
    }

    @Test
    public void testOrderResource() throws Exception {
        fixture.assertObjectResourceHasName(2, "OrderObject");
    }

    @Test
    public void testOrderResourceReferenceLink() throws Exception {
        JsonNode datatype = fixture.getResourceDataType(2);
        ArrayNode referenceLinks = fixture.getReferenceTreatments(datatype);
        assertThat(referenceLinks.size(), equalTo(1));

        JsonNode referenceEmbed = referenceLinks.get(0);
        assertThat(referenceEmbed.get(OBJECTTYPE).asText(), equalTo("ReferenceEmbed"));
        assertThat(referenceEmbed.get(ID).asText(), equalTo("OrderObject.LineItems.referenceEmbed"));
        ArrayNode nestedRreferenceLinks = fixture.getReferenceTreatments(referenceEmbed);
        assertThat(nestedRreferenceLinks.size(), equalTo(1));
        JsonNode referenceLink = nestedRreferenceLinks.get(0);
        assertThat(referenceLink.get(OBJECTTYPE).asText(), equalTo("ReferenceLink"));
        assertThat(referenceLink.get(NAME).asText(), equalTo("Product"));
        assertThat(referenceLink.get(REFERENCE_RESOURCE_ID).asText(), equalTo("ProductObject"));
    }

    @Test
    public void testOrderListResource() throws Exception {
        fixture.assertObjectResourceHasName(3, "BigOrderObject");
    }

    @Test
    public void testOrderListResourceReferenceLink() throws Exception {
        JsonNode datatype = fixture.getResourceDataType(3);
        ArrayNode referenceLinks = fixture.getReferenceTreatments(datatype);
        assertThat(referenceLinks.size(), equalTo(1));

        JsonNode referenceEmbed = referenceLinks.get(0);
        assertThat(referenceEmbed.get(OBJECTTYPE).asText(), equalTo("ReferenceEmbed"));
        assertThat(referenceEmbed.get(ID).asText(), equalTo("BigOrderObject.LineItems.referenceEmbed"));
        ArrayNode nestedRreferenceLinks = fixture.getReferenceTreatments(referenceEmbed);
        assertThat(nestedRreferenceLinks.size(), equalTo(1));
        JsonNode referenceLink = nestedRreferenceLinks.get(0);
        assertThat(referenceLink.get(OBJECTTYPE).asText(), equalTo("ReferenceLink"));
        assertThat(referenceLink.get(NAME).asText(), equalTo("Product"));
        assertThat(referenceLink.get(REFERENCE_RESOURCE_ID).asText(), equalTo("ProductObject"));
    }

    @Test
    public void testCustomerResourceDataType() throws Exception {
        JsonNode datatype = fixture.getResourceDataType(0);
        assertThat(datatype, notNullValue());
        assertThat(datatype.get(OBJECTTYPE).asText(), equalTo("DataType"));
        assertThat(datatype.get(NAME).asText(), equalTo("Customer"));
        assertThat(datatype.get("id").asText(), equalTo("CustomerObject.Customer"));

        ArrayNode properties = fixture.getProperties(datatype);
        assertThat(properties.size(), equalTo(2));

        JsonNode property1 = properties.get(0);
        assertThat(property1.get(OBJECTTYPE).asText(), equalTo("PrimitiveProperty"));
        assertThat(property1.get(NAME).asText(), equalTo("CustomerID"));
        assertThat(property1.get(TYPE).asText(), equalTo("string"));
        assertThat(property1.get(ID).asText(), equalTo("CustomerObject.Customer.CustomerID"));

        JsonNode property2 = properties.get(1);
        assertThat(property2.get(OBJECTTYPE).asText(), equalTo("PrimitiveProperty"));
        assertThat(property2.get(NAME).asText(), equalTo("CustomerName"));
        assertThat(property2.get(TYPE).asText(), equalTo("string"));
        assertThat(property2.get(ID).asText(), equalTo("CustomerObject.Customer.CustomerName"));
    }

    @Test
    public void testCustomerResourceReferenceLinks() throws Exception {
        JsonNode datatype = fixture.getResourceDataType(0);
        ArrayNode referenceTreatments = fixture.getReferenceTreatments(datatype);
        assertThat(referenceTreatments.size(), equalTo(1));

        JsonNode orderReferenceEmbed = referenceTreatments.get(0);
        JsonNode lineItemsReferenceEmbed = fixture.getReferenceTreatments(orderReferenceEmbed).get(0);

        JsonNode referenceLink1 = fixture.getReferenceTreatments(lineItemsReferenceEmbed).get(0);
        assertThat(referenceLink1.get(OBJECTTYPE).asText(), equalTo("ReferenceLink"));
        assertThat(referenceLink1.get(NAME).asText(), equalTo("Product"));
        assertThat(referenceLink1.get(REFERENCE_RESOURCE_ID).asText(), equalTo("ProductObject"));
        assertThat(referenceLink1.get(ID).asText(), equalTo("CustomerObject.Orders.LineItems.Product.referenceLink"));

        ArrayNode referenceLinkProperties = fixture.getProperties(referenceLink1);
        assertThat(referenceLinkProperties.size(), equalTo(1));

        JsonNode referenceLinkProperty1 = referenceLinkProperties.get(0);
        assertThat(referenceLinkProperty1.get(OBJECTTYPE).asText(), equalTo("PrimitiveProperty"));
        assertThat(referenceLinkProperty1.get(NAME).asText(), equalTo("ProductID"));
        assertThat(referenceLinkProperty1.get(TYPE).asText(), equalTo("string"));
        assertThat(referenceLinkProperty1.get(ID).asText(),
                equalTo("CustomerObject.Orders.LineItems.Product.referenceLink.ProductID"));

    }

    @Test
    public void testCustomerResourceMethod() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 0);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("Customer"));
        assertThat(method.get(TYPE).asText(), equalTo("GET"));

        JsonNode request = fixture.getRequest(method);
        assertThat(request, notNullValue());
        assertThat(request.get(OBJECTTYPE).asText(), equalTo("Request"));
        // assertThat(request.get(RESOURCE_TYPE).asText(), equalTo("ShoppingCartResource"));

        ArrayNode responses = fixture.getResponses(method);
        assertThat(responses.size(), equalTo(1));

        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(RESOURCE_TYPE).asText(), equalTo("CustomerObject"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("200 - OK"));

    }
}
