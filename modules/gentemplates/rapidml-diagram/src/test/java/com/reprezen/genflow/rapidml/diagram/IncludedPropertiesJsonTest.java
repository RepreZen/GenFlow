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
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("realization/Customer_includedPropertiesWCardinality.rapid")
public class IncludedPropertiesJsonTest {

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

}
