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
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.STATUS_CODE;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.STATUS_CODE_GROUP;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("request_response/StatusCodes.rapid")
public class ResponseStatusCodeJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void test100Codes() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 0);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("oneHundred"));

        ArrayNode responses = fixture.getResponses(method);
        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("100"));
        assertThat(response1.get(STATUS_CODE_GROUP).asText(), equalTo("Informational"));
    }

    @Test
    public void test200Codes() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 1);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("twoHundred"));

        ArrayNode responses = fixture.getResponses(method);
        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("200 - OK"));
        assertThat(response1.get(STATUS_CODE_GROUP).asText(), equalTo("Success"));
    }

    @Test
    public void test300Codes() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 2);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("threeHundred"));

        ArrayNode responses = fixture.getResponses(method);
        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("301 - Moved Permanently"));
        assertThat(response1.get(STATUS_CODE_GROUP).asText(), equalTo("Redirection"));
    }

    @Test
    public void test400Codes() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 3);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("fourHundred"));

        ArrayNode responses = fixture.getResponses(method);
        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("403 - Forbidden"));
        assertThat(response1.get(STATUS_CODE_GROUP).asText(), equalTo("Client Error"));
    }

    @Test
    public void test500Codes() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 4);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("fiveHundred"));

        ArrayNode responses = fixture.getResponses(method);
        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("500 - Internal Server Error"));
        assertThat(response1.get(STATUS_CODE_GROUP).asText(), equalTo("Server Error"));
    }

}
