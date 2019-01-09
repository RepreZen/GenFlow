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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SuppressWarnings("nls")
@SampleRestFile("params/ParameterTypeStyles.rapid")
public class ParameterTypeStylesJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testRequestParamTypes() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 0);
        assertThat(method, notNullValue());
        JsonNode request = fixture.getRequest(method);
        assertThat(request, notNullValue());
        JsonNode params = request.get("parameters");
        assertThat(params.size(), equalTo(7));
        assertQueryParameter(params.get(0), "param1:string");
        assertHeaderParameter(params.get(1), "param2:integer");
        assertQueryParameter(params.get(2), "param3:double");
        assertHeaderParameter(params.get(3), "param4:string");
        assertQueryParameter(params.get(4), "param5:string");
        assertHeaderParameter(params.get(5), "param6:boolean");
        assertQueryParameter(params.get(6), "param7:integer");
    }

    @Test
    public void testResponseParamTypes() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 0);
        assertThat(method, notNullValue());
        JsonNode request = fixture.getResponses(method).get(0);
        assertThat(request, notNullValue());
        JsonNode params = request.get("parameters");
        assertThat(params.size(), equalTo(5));
        assertHeaderParameter(params.get(0), "param1:string");
        assertHeaderParameter(params.get(1), "param2:integer");
        assertHeaderParameter(params.get(2), "param3:string");
        assertHeaderParameter(params.get(3), "param4:string");
        assertHeaderParameter(params.get(4), "param5:boolean");
    }

    private void assertQueryParameter(JsonNode paramNode, String name) {
        assertParameter(paramNode, name, "QueryParameter");
    }

    private void assertHeaderParameter(JsonNode paramNode, String name) {
        assertParameter(paramNode, name, "HeaderParameter");
    }

    private void assertParameter(JsonNode paramNode, String name, String type) {
        assertThat(paramNode.get(NAME).asText(), equalTo(name));
        assertThat(paramNode.get(OBJECTTYPE).asText(), equalTo(type));
    }
}
