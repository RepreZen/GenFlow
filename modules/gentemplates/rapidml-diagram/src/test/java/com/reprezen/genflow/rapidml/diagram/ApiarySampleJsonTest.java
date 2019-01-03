/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram;

import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.NAME;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.OBJECTTYPE;
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

@SampleRestFile("ApiarySample.rapid")
public class ApiarySampleJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testResource() throws Exception {
        ArrayNode resources = fixture.getResources();
        assertThat(resources.size(), equalTo(1));

        JsonNode taxFiling = resources.get(0);
        assertThat(taxFiling, notNullValue());
        assertThat(taxFiling.get(OBJECTTYPE).asText(), equalTo("CollectionResource"));
        assertThat(taxFiling.get(NAME).asText(), equalTo("ShoppingCartCollection"));
        assertThat(taxFiling.get("id").asText(), equalTo("ShoppingCartCollection"));
    }

    @Test
    public void testResourceDataType() throws Exception {
        JsonNode datatype = fixture.getResourceDataType(0);
        assertThat(datatype, notNullValue());
        assertThat(datatype.get(OBJECTTYPE).asText(), equalTo("DataType"));
        assertThat(datatype.get(NAME).asText(), equalTo("ShoppingCart*"));
        assertThat(datatype.get("id").asText(), equalTo("ShoppingCartCollection.ShoppingCart"));
    }

    @Test
    public void testResourceMethod() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 0);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo(""));
        assertThat(method.get(TYPE).asText(), equalTo("GET"));

        JsonNode request = fixture.getRequest(method);
        assertThat(request, notNullValue());
        assertThat(request.get(OBJECTTYPE).asText(), equalTo("Request"));
        assertThat(request.get(RESOURCE_TYPE).asText(), equalTo("ShoppingCartCollection"));

        ArrayNode responses = fixture.getResponses(method);
        assertThat(responses.size(), equalTo(2));

        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(RESOURCE_TYPE).asText(), equalTo("ShoppingCartCollection"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("200 - OK"));

        JsonNode response2 = responses.get(1);
        assertThat(response2.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response2.get(RESOURCE_TYPE).asText(), equalTo("ShoppingCartCollection"));
        assertThat(response2.get(STATUS_CODE).asText(), equalTo("404 - Not Found"));
    }
}
