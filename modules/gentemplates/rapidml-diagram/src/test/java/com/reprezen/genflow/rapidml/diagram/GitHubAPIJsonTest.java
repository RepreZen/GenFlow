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
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.LABEL;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.NAME;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.OBJECTTYPE;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.PROPERTY_ID;
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.URI_FRAGMENT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("GitHubAPI.rapid")
public class GitHubAPIJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testIssueResource() throws Exception {
        fixture.assertObjectResourceHasName(0, "IssueObject");
    }

    @Test
    public void testCommentsResource() throws Exception {
        fixture.assertCollectionResourceHasName(1, "Comments");
    }

    @Test
    public void testIssueURI() throws Exception {
        ArrayNode resources = fixture.getResources();
        assertThat(resources.size(), equalTo(2));

        JsonNode customer = resources.get(0);
        assertThat(customer.get(OBJECTTYPE).asText(), equalTo("ObjectResource"));
        assertThat(customer.get(NAME).asText(), equalTo("IssueObject"));
        assertThat(customer.get(ID).asText(), equalTo("IssueObject"));
        // test URI
        JsonNode uri = fixture.getURI(customer);
        assertThat(uri, notNullValue());

        ArrayNode names = (ArrayNode) uri.get(NAME);
        assertThat(names.size(), equalTo(3));

        JsonNode name1 = names.get(0);
        assertThat(name1.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name1.get(LABEL).asText(), equalTo("/{organization}"));
        assertThat(name1.get(ID).asText(), equalTo("IssueObject.URI.1"));

        JsonNode name2 = names.get(1);
        assertThat(name2.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name2.get(LABEL).asText(), equalTo("/{repository}"));
        assertThat(name2.get(ID).asText(), equalTo("IssueObject.URI.2"));

        JsonNode name3 = names.get(2);
        assertThat(name3.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name3.get(LABEL).asText(), equalTo("/{issue}"));
        assertThat(name3.get(ID).asText(), equalTo("IssueObject.URI.3"));

        ArrayNode parameters = (ArrayNode) uri.get("parameters");
        assertThat(parameters.size(), equalTo(1));

        // Commented parameters, see ZEN-452 URI parameters bound to properties of a data type which is not directly
        // referenced by the resource
        // JsonNode parameter1 = parameters.get(0);
        // assertThat(parameter1.get(OBJECTTYPE).asText(), equalTo("TemplateParameter"));
        // assertThat(parameter1.get(NAME).asText(), equalTo("organization"));
        // assertThat(parameter1.get(URI_FRAGMENT).asText(), equalTo("Issue.URI.1"));
        // assertThat(parameter1.get(FEATURE_REFERENCE_ID).asText(), equalTo("Issue.Organization.id"));
        //
        // JsonNode parameter2 = parameters.get(1);
        // assertThat(parameter2.get(OBJECTTYPE).asText(), equalTo("TemplateParameter"));
        // assertThat(parameter2.get(NAME).asText(), equalTo("repository"));
        // assertThat(parameter2.get(URI_FRAGMENT).asText(), equalTo("Issue.URI.2"));
        // assertThat(parameter2.get(FEATURE_REFERENCE_ID).asText(), equalTo("Issue.Repository.id"));

        JsonNode parameter3 = parameters.get(0);
        assertThat(parameter3.get(OBJECTTYPE).asText(), equalTo("TemplateParameter"));
        assertThat(parameter3.get(NAME).asText(), equalTo("issue"));
        assertThat(parameter3.get(URI_FRAGMENT).asText(), equalTo("IssueObject.URI.3"));
        assertThat(parameter3.get(PROPERTY_ID).asText(), equalTo("IssueObject.Issue.id"));
    }
}
