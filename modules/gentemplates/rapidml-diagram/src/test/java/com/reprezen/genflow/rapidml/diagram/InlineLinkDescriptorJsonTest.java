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

@SampleRestFile("realization/Customer_inlineLinkDescriptor.rapid")
public class InlineLinkDescriptorJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testInlineLinkDescriptor() throws Exception {
        JsonNode datatype = fixture.getResourceDataType(0);
        assertThat(datatype, notNullValue());
        assertThat(datatype.get(OBJECTTYPE).asText(), equalTo("DataType"));
        assertThat(datatype.get(NAME).asText(), equalTo("Customer"));
        assertThat(datatype.get("id").asText(), equalTo("CustomerObject.Customer"));

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
        assertThat(referenceLinkProperties.size(), equalTo(2));

        JsonNode referenceLinkProperty1 = referenceLinkProperties.get(0);
        assertThat(referenceLinkProperty1.get(OBJECTTYPE).asText(), equalTo("PrimitiveProperty"));
        assertThat(referenceLinkProperty1.get(NAME).asText(), equalTo("ProductID"));
        assertThat(referenceLinkProperty1.get(TYPE).asText(), equalTo("string"));
        assertThat(referenceLinkProperty1.get(ID).asText(),
                equalTo("CustomerObject.Orders.LineItems.Product.referenceLink.ProductID"));

        JsonNode referenceLinkProperty2 = referenceLinkProperties.get(1);
        assertThat(referenceLinkProperty2.get(OBJECTTYPE).asText(), equalTo("PrimitiveProperty"));
        assertThat(referenceLinkProperty2.get(NAME).asText(), equalTo("ProductName"));
        assertThat(referenceLinkProperty2.get(TYPE).asText(), equalTo("string"));
        assertThat(referenceLinkProperty2.get(ID).asText(),
                equalTo("CustomerObject.Orders.LineItems.Product.referenceLink.ProductName"));

    }

}
