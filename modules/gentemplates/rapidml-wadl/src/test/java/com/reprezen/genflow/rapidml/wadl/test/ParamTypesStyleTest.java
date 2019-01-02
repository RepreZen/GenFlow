/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.test;

import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasName;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasStyle;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasType;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getParametersWithId;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getRequests;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getResponses;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("params/ParameterTypeStyles.rapid")
@SuppressWarnings("nls")
public class ParamTypesStyleTest {

    @Rule
    public WadlGeneratorIntegrationTestFixture fixture = new WadlGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedWadlIsValid() {
        fixture.assertGeneratedWadlIsValid();
    }

    @Test
    public void testGetParameterTypeStyles() throws Exception {
        assertResource("ParameterTypeStylesObject", "getParameterTypeStylesObject");
        assertResource("ParameterTypeStylesCollection", "getParameterTypeStylesCollection");
    }

    private void assertResource(String name, String methodName) throws Exception {
        Node method = fixture.requireMethodById(name, methodName);
        assertThat(method, hasName("GET"));

        List<Node> requests = getRequests().apply(method);
        assertThat(requests.size(), equalTo(1));
        assertRequestParameters(requests.get(0), methodName);

        List<Node> responses = getResponses().apply(method);
        assertThat(responses.size(), equalTo(1));

        assertResponseParameters(responses.get(0), methodName);
    }

    private void assertRequestParameters(Node node, String prefix) {
        assertParameter(node, prefix + "_request_param1", "param1", "query", "xs:string");
        assertParameter(node, prefix + "_request_param2", "param2", "header", "xs:integer");
        assertParameter(node, prefix + "_request_param3", "param3", "query", "xs:double");
        assertParameter(node, prefix + "_request_param4", "param4", "header", "xs:string");
        assertParameter(node, prefix + "_request_param5", "param5", "query", "xs:string");
        assertParameter(node, prefix + "_request_param6", "param6", "header", "xs:boolean");
        assertParameter(node, prefix + "_request_param7", "param7", "query", "xs:integer");
    }

    private void assertResponseParameters(Node node, String prefix) {
        assertParameter(node, prefix + "_response_param1", "param1", "header", "xs:string");
        assertParameter(node, prefix + "_response_param2", "param2", "header", "xs:integer");
        assertParameter(node, prefix + "_response_param3", "param3", "header", "xs:string");
        assertParameter(node, prefix + "_response_param4", "param4", "header", "xs:string");
        assertParameter(node, prefix + "_response_param5", "param5", "header", "xs:boolean");
    }

    private void assertParameter(Node node, String paramId, String name, String style, String type) {
        List<Node> requestParams = getParametersWithId(paramId).apply(node);
        assertThat(requestParams.size(), equalTo(1));
        Node requestParam = requestParams.get(0);

        assertThat(requestParam, hasName(name));
        assertThat(requestParam, hasStyle(style));
        assertThat(requestParam, hasType(type));
    }
}
