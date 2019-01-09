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
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.PROPERTY_ID;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.RESOURCE_TYPE;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.STATUS_CODE;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SuppressWarnings("nls")
@SampleRestFile("CustomerWithDefaultMethods.rapid")
public class CustomerWithDefaultMethodsJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testCustomerResource() throws Exception {
        ArrayNode resources = fixture.getResources();
        assertThat(resources.size(), equalTo(1));

        JsonNode customer = resources.get(0);
        assertThat(customer, notNullValue());
        assertThat(customer.get(OBJECTTYPE).asText(), equalTo("ObjectResource"));
        assertThat(customer.get(NAME).asText(), equalTo("CustomerObject"));
        assertThat(customer.get("id").asText(), equalTo("CustomerObject"));
        // test URI
        JsonNode uri = fixture.getURI(customer);
        assertThat(uri, notNullValue());
    }

    @Test
    public void testGetMethod() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 0);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("defaultGet"));
        assertThat(method.get(TYPE).asText(), equalTo("GET"));

        JsonNode request = fixture.getRequest(method);
        assertThat(request, notNullValue());
        assertThat(request.get(OBJECTTYPE).asText(), equalTo("Request"));
        assertThat(request.get(NAME).asText(), equalTo("(empty request)"));

        ArrayNode responses = fixture.getResponses(method);
        assertThat(responses.size(), equalTo(1));

        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(RESOURCE_TYPE).asText(), equalTo("CustomerObject"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("200 - OK"));
    }

    @Test
    public void testPutMethod() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 1);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("defaultPut"));
        assertThat(method.get(TYPE).asText(), equalTo("PUT"));

        JsonNode request = fixture.getRequest(method);
        assertThat(request, notNullValue());
        assertThat(request.get(OBJECTTYPE).asText(), equalTo("Request"));
        assertThat(request.get(RESOURCE_TYPE).asText(), equalTo("CustomerObject"));
        assertFalse(request.has(NAME));

        ArrayNode responses = fixture.getResponses(method);
        assertThat(responses.size(), equalTo(1));

        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("200 - OK"));
    }

    @Test
    public void testPostMethod() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 2);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("defaultPost"));
        assertThat(method.get(TYPE).asText(), equalTo("POST"));

        JsonNode request = fixture.getRequest(method);
        assertThat(request, notNullValue());
        assertThat(request.get(OBJECTTYPE).asText(), equalTo("Request"));
        assertThat(request.get(RESOURCE_TYPE).asText(), equalTo("CustomerObject"));
        assertFalse(request.has(NAME));

        ArrayNode responses = fixture.getResponses(method);
        assertThat(responses.size(), equalTo(1));

        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("200 - OK"));
    }

    @Test
    public void testConnectMethod() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 3);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("defaultConnect"));
        assertThat(method.get(TYPE).asText(), equalTo("CONNECT"));

        JsonNode request = fixture.getRequest(method);
        assertThat(request, notNullValue());
        assertThat(request.get(OBJECTTYPE).asText(), equalTo("Request"));
        assertThat(request.get(RESOURCE_TYPE), nullValue());
        assertThat(request.get(NAME).asText(), equalTo("(empty request)"));

        ArrayNode responses = fixture.getResponses(method);
        assertThat(responses.size(), equalTo(1));

        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("200 - OK"));
    }

    @Test
    public void testGetWithRequestParamsMethod() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 4);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("getWithParameters"));
        assertThat(method.get(TYPE).asText(), equalTo("GET"));

        JsonNode request = fixture.getRequest(method);
        assertThat(request, notNullValue());
        assertThat(request.get(OBJECTTYPE).asText(), equalTo("Request"));
        assertThat(request.get(NAME), nullValue());

        JsonNode params = request.get("parameters");
        assertThat(params.size(), equalTo(2));

        JsonNode param1 = params.get(0);
        assertThat(param1.get(NAME).asText(), equalTo("p1:string"));
        assertThat(param1.get(OBJECTTYPE).asText(), equalTo("QueryParameter"));
        assertThat(param1.get("isProperty").asBoolean(), equalTo(false));
        // assertThat(param1.get(FEATURE_REFERENCE_ID), nullValue());
        // assertThat(param1.get("type").asText(), equalTo("string"));

        JsonNode param2 = params.get(1);
        assertThat(param2.get(NAME).asText(), equalTo("p2:string"));
        assertThat(param2.get(OBJECTTYPE).asText(), equalTo("QueryParameter"));
        assertThat(param2.get("isProperty").asBoolean(), equalTo(true));
        assertThat(param2.get("property").asText(), equalTo("CustomerID"));
        assertThat(param2.get(PROPERTY_ID).asText(), equalTo("CustomerObject.Customer.CustomerID"));

        ArrayNode responses = fixture.getResponses(method);
        assertThat(responses.size(), equalTo(1));

        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(RESOURCE_TYPE).asText(), equalTo("CustomerObject"));
        assertThat(response1.get(STATUS_CODE).asText(), equalTo("200 - OK"));
    }

    @Test
    public void testGetWithResponseParamsMethod() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 5);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("getWithResponseParameters"));
        assertThat(method.get(TYPE).asText(), equalTo("GET"));

        ArrayNode responses = fixture.getResponses(method);
        assertThat(responses.size(), equalTo(1));
        JsonNode response = responses.get(0);
        assertThat(response.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response.get(NAME), nullValue());

        JsonNode params = response.get("parameters");
        assertThat(params.size(), equalTo(2));

        JsonNode param1 = params.get(0);
        assertThat(param1.get(NAME).asText(), equalTo("p3:int"));
        assertThat(param1.get(OBJECTTYPE).asText(), equalTo("HeaderParameter"));
        // assertThat(param1.get(FEATURE_REFERENCE_ID), nullValue());
        // assertThat(param1.get("type").asText(), equalTo("string"));

        JsonNode param2 = params.get(1);
        assertThat(param2.get(NAME).asText(), equalTo("p4:string"));
        assertThat(param2.get(OBJECTTYPE).asText(), equalTo("HeaderParameter"));
        assertThat(param2.get(PROPERTY_ID).asText(), equalTo("CustomerObject.Customer.CustomerName"));
        assertThat(param2.get("property").asText(), equalTo("CustomerName"));

    }

    @Test
    public void testEmptyResposeMethod() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 6);
        assertThat(method, notNullValue());
        assertThat(method.get(OBJECTTYPE).asText(), equalTo("Method"));
        assertThat(method.get(NAME).asText(), equalTo("emptyResponse"));
        assertThat(method.get(TYPE).asText(), equalTo("PUT"));

        ArrayNode responses = fixture.getResponses(method);
        assertThat(responses.size(), equalTo(1));

        JsonNode response1 = responses.get(0);
        assertThat(response1.get(OBJECTTYPE).asText(), equalTo("Response"));
        assertThat(response1.get(NAME).asText(), equalTo("(empty response)"));
        assertThat(response1.get(RESOURCE_TYPE), nullValue());
        assertThat(response1.get(STATUS_CODE), nullValue());
    }
}
