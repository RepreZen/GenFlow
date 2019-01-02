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
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getRepresentations;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getRequests;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("GitHubUserAPI.rapid")
public class GitHubUserAPIIntegrationTest {

    @Rule
    public WadlGeneratorIntegrationTestFixture fixture = new WadlGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedWadlIsValid() {
        fixture.assertGeneratedWadlIsValid();
    }

    /**
     * ZEN-106 Update WADL Generator to set parameter type according to the RESTful API model
     */
    @Test
    public void testGetUsers_MethodRequest() throws Exception {
        Node method = fixture.requireMethodById("Users", "getAllUsers");
        assertThat(method, hasName("GET"));

        List<Node> requests = getRequests().apply(method);
        assertThat(requests.size(), equalTo(1));
        Node request = requests.get(0);

        List<Node> requestParams = getParametersWithId("getAllUsers_request_since").apply(request);
        assertThat("Fix ZEN-106", requestParams.size(), equalTo(1));
        Node requestParam = requestParams.get(0);

        assertThat(requestParam, hasName("since"));
        assertThat(requestParam, hasStyle("query"));
        assertThat(requestParam, hasType("xs:string"));

        List<Node> representations = getRepresentations().apply(request);
        assertThat(representations.size(), equalTo(0));
    }
}
