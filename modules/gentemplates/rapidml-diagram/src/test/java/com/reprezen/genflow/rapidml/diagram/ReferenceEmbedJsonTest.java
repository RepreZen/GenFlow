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

@SampleRestFile("perspective/ReferenceEmbed.rapid")
public class ReferenceEmbedJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testTopLevelReferenceEmbed() throws Exception {
        JsonNode datatype = fixture.getResourceDataType(0);
        assertThat(datatype, notNullValue());
        assertThat(datatype.get(OBJECTTYPE).asText(), equalTo("DataType"));
        assertThat(datatype.get(NAME).asText(), equalTo("myDataType"));
        assertThat(datatype.get("id").asText(), equalTo("ReferenceEmbedObject.myDataType"));

        ArrayNode referenceEmbeds = fixture.getReferenceTreatments(datatype);
        assertThat(referenceEmbeds.size(), equalTo(2));

        JsonNode referenceEmbed1 = referenceEmbeds.get(0);
        testReferenceEmbed(referenceEmbed1, "order : DataType3");

        ArrayNode referenceLinkProperties = fixture.getProperties(referenceEmbed1);
        assertThat(referenceLinkProperties.size(), equalTo(2));

        JsonNode referenceTreatmentProperty1 = referenceLinkProperties.get(0);
        testPrimitiveProperty(referenceTreatmentProperty1, "orderID", "string");
        assertThat(referenceTreatmentProperty1.get(ID).asText(),
                equalTo("ReferenceEmbedObject.order.referenceEmbed.orderID"));

        JsonNode referenceTreatmentProperty2 = referenceLinkProperties.get(1);
        testPrimitiveProperty(referenceTreatmentProperty2, "orderDate", "string");
        assertThat(referenceTreatmentProperty2.get(ID).asText(),
                equalTo("ReferenceEmbedObject.order.referenceEmbed.orderDate"));
    }

    @Test
    public void testNestedReferenceEmbed() throws Exception {
        JsonNode datatype = fixture.getResourceDataType(0);
        ArrayNode referenceEmbeds = fixture.getReferenceTreatments(datatype);
        JsonNode referenceEmbedLevel1 = referenceEmbeds.get(0);
        // level 2
        ArrayNode referenceEmbedsLevel2 = fixture.getReferenceTreatments(referenceEmbedLevel1);
        assertThat(referenceEmbedsLevel2.size(), equalTo(1));
        JsonNode referenceEmbedLevel2 = referenceEmbedsLevel2.get(0);
        testReferenceEmbed(referenceEmbedLevel2, "lineItems : LineItem");
        // level 3
        ArrayNode referenceEmbedsLevel3 = fixture.getReferenceTreatments(referenceEmbedLevel2);
        assertThat(referenceEmbedsLevel3.size(), equalTo(1));
        JsonNode referenceEmbedLevel3 = referenceEmbedsLevel3.get(0);
        testReferenceEmbed(referenceEmbedLevel3, "product : Product");
        ArrayNode referenceEmbedLevel3Properties = fixture.getProperties(referenceEmbedLevel3);
        assertThat(referenceEmbedLevel3Properties.size(), equalTo(3));
        JsonNode referenceTreatmentProperty1 = referenceEmbedLevel3Properties.get(0);
        testPrimitiveProperty(referenceTreatmentProperty1, "productID", "string");
        JsonNode referenceTreatmentProperty2 = referenceEmbedLevel3Properties.get(1);
        testPrimitiveProperty(referenceTreatmentProperty2, "productName", "string");
        JsonNode referenceTreatmentProperty3 = referenceEmbedLevel3Properties.get(2);
        testPrimitiveProperty(referenceTreatmentProperty3, "productPrice", "string");
        // level 4
        ArrayNode referenceEmbedsLevel4 = fixture.getReferenceTreatments(referenceEmbedLevel3);
        assertThat(referenceEmbedsLevel4.size(), equalTo(1));
        JsonNode referenceEmbedLevel4 = referenceEmbedsLevel4.get(0);
        testReferenceLink(referenceEmbedLevel4, "image");
        assertThat(referenceEmbedLevel4.get(REFERENCE_RESOURCE_ID).asText(), equalTo("OrderImageOnlineObject"));
        ArrayNode referenceLinkLevel4Properties = fixture.getProperties(referenceEmbedLevel4);
        assertThat(referenceLinkLevel4Properties.size(), equalTo(0));
    }

    protected static void testReferenceEmbed(JsonNode referenceEmbed, String name) {
        assertThat(referenceEmbed.get(OBJECTTYPE).asText(), equalTo("ReferenceEmbed"));
        assertThat(referenceEmbed.get(NAME).asText(), equalTo(name));
    }

    protected static void testReferenceLink(JsonNode referenceLink, String name) {
        assertThat(referenceLink.get(OBJECTTYPE).asText(), equalTo("ReferenceLink"));
        assertThat(referenceLink.get(NAME).asText(), equalTo(name));
    }

    public static void testPrimitiveProperty(JsonNode property, String name, String type) {
        assertThat(property.get(OBJECTTYPE).asText(), equalTo("PrimitiveProperty"));
        assertThat(property.get(NAME).asText(), equalTo(name));
        assertThat(property.get(TYPE).asText(), equalTo(type));
    }

}