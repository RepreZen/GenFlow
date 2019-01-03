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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("TaxFiling_multipleParams.rapid")
public class ParametersJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testTemplateParameters() throws Exception {
        JsonNode resource = fixture.getResource(0);
        JsonNode uri = resource.get("URI");
        ArrayNode names = (ArrayNode) uri.get(NAME);
        // assertTrue(names.size() >= 4);

        ArrayNode params = (ArrayNode) uri.get("parameters");
        // assertTrue(params.size() >= 3);

        JsonNode name0 = names.get(0);
        assertThat(name0.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name0.get(LABEL).asText(), equalTo("/taxFiling"));
        assertThat(name0.get(ID).asText(), equalTo("TaxFilingObject.URI.1"));

        JsonNode name1 = names.get(1);
        assertThat(name1.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name1.get(LABEL).asText(), equalTo("/{taxyear}"));
        assertThat(name1.get(ID).asText(), equalTo("TaxFilingObject.URI.2"));

        JsonNode name2 = names.get(2);
        assertThat(name2.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name2.get(LABEL).asText(), equalTo("/segment3"));
        assertThat(name2.get(ID).asText(), equalTo("TaxFilingObject.URI.3"));

        JsonNode name3 = names.get(3);
        assertThat(name3.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name3.get(LABEL).asText(), equalTo("/{jurisdiction}"));
        assertThat(name3.get(ID).asText(), equalTo("TaxFilingObject.URI.4"));

        JsonNode name4 = names.get(4);
        assertThat(name4.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name4.get(LABEL).asText(), equalTo("/{id}"));
        assertThat(name4.get(ID).asText(), equalTo("TaxFilingObject.URI.5"));

        JsonNode param1 = params.get(0);
        assertThat(param1.get(NAME).asText(), equalTo("taxyear"));
        assertThat(param1.get(OBJECTTYPE).asText(), equalTo("TemplateParameter"));
        assertThat(param1.get(PROPERTY_ID).asText(), equalTo("TaxFilingObject.TaxFiling.taxYear"));
        assertThat(param1.get(URI_FRAGMENT).asText(), equalTo("TaxFilingObject.URI.2"));

        JsonNode param2 = params.get(1);
        assertThat(param2.get(NAME).asText(), equalTo("jurisdiction"));
        assertThat(param2.get(OBJECTTYPE).asText(), equalTo("TemplateParameter"));
        assertThat(param2.get(PROPERTY_ID).asText(), equalTo("TaxFilingObject.TaxFiling.jurisdiction"));
        assertThat(param2.get("isProperty").asBoolean(), equalTo(true));
        assertThat(param2.get(URI_FRAGMENT).asText(), equalTo("TaxFilingObject.URI.4"));

        JsonNode param3 = params.get(2);
        assertThat(param3.get(NAME).asText(), equalTo("id"));
        assertThat(param3.get(OBJECTTYPE).asText(), equalTo("TemplateParameter"));
        assertThat(param3.get(PROPERTY_ID).asText(), equalTo("TaxFilingObject.TaxFiling.filingID"));
        assertThat(param3.get(URI_FRAGMENT).asText(), equalTo("TaxFilingObject.URI.5"));

    }

    @Test
    public void testMatrixParameters() throws Exception {
        JsonNode resource = fixture.getResource(0);
        JsonNode uri = resource.get("URI");
        ArrayNode names = (ArrayNode) uri.get(NAME);
        assertTrue(names.size() >= 6);

        ArrayNode params = (ArrayNode) uri.get("parameters");
        assertTrue(params.size() >= 5);

        JsonNode name0 = names.get(5);
        assertThat(name0.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name0.get(LABEL).asText(), equalTo("@mParam1, "));
        assertThat(name0.get(ID).asText(), equalTo("TaxFilingObject.URI.6"));

        JsonNode name1 = names.get(6);
        assertThat(name1.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name1.get(LABEL).asText(), equalTo("@mParam2"));
        assertThat(name1.get(ID).asText(), equalTo("TaxFilingObject.URI.7"));

        JsonNode param1 = params.get(3);
        assertThat(param1.get(NAME).asText(), equalTo("mParam1"));
        assertThat(param1.get(OBJECTTYPE).asText(), equalTo("MatrixParameter"));
        assertThat(param1.get(PROPERTY_ID).asText(), equalTo("TaxFilingObject.TaxFiling.firstName"));
        assertThat(param1.get(URI_FRAGMENT).asText(), equalTo("TaxFilingObject.URI.6"));

        JsonNode param2 = params.get(4);
        assertThat(param2.get(NAME).asText(), equalTo("mParam2"));
        assertThat(param2.get(OBJECTTYPE).asText(), equalTo("MatrixParameter"));
        assertThat(param2.get(PROPERTY_ID).asText(), equalTo("TaxFilingObject.TaxFiling.lastName"));
        assertThat(param2.get(URI_FRAGMENT).asText(), equalTo("TaxFilingObject.URI.7"));

    }

    @Test
    public void testCollectionParameters() throws Exception {
        JsonNode resource = fixture.getResource(1);
        JsonNode uri = resource.get("URI");
        ArrayNode names = (ArrayNode) uri.get(NAME);
        assertThat(names.size(), equalTo(4));

        ArrayNode params = (ArrayNode) uri.get("parameters");
        // assertThat(params.size(), equalTo(3));

        JsonNode name0 = names.get(2);
        assertThat(name0.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name0.get(LABEL).asText(), equalTo("*cParam1, "));
        assertThat(name0.get(ID).asText(), equalTo("TaxPayerCollection.URI.3"));

        JsonNode name1 = names.get(3);
        assertThat(name1.get(OBJECTTYPE).asText(), equalTo("URISegment"));
        assertThat(name1.get(LABEL).asText(), equalTo("*cParam2"));
        assertThat(name1.get(ID).asText(), equalTo("TaxPayerCollection.URI.4"));

        JsonNode param1 = params.get(1);
        assertThat(param1.get(NAME).asText(), equalTo("cParam1"));
        assertThat(param1.get(OBJECTTYPE).asText(), equalTo("CollectionParameter"));
        assertThat(param1.get(PROPERTY_ID).asText(), equalTo("TaxPayerCollection.TaxPayer.firstName"));
        assertThat(param1.get(URI_FRAGMENT).asText(), equalTo("TaxPayerCollection.URI.3"));

        JsonNode param2 = params.get(2);
        assertThat(param2.get(NAME).asText(), equalTo("cParam2"));
        assertThat(param2.get(OBJECTTYPE).asText(), equalTo("CollectionParameter"));
        assertThat(param2.get(PROPERTY_ID).asText(), equalTo("TaxPayerCollection.TaxPayer.lastName"));
        assertThat(param2.get(URI_FRAGMENT).asText(), equalTo("TaxPayerCollection.URI.4"));
    }

    @Test
    public void testXsdTemplateParameters() throws Exception {
        JsonNode resource = fixture.getResource(1);
        JsonNode uri = resource.get("URI");
        ArrayNode names = (ArrayNode) uri.get(NAME);

        ArrayNode params = (ArrayNode) uri.get("parameters");

        JsonNode param1 = params.get(0);
        assertThat(param1.get(NAME).asText(), equalTo("id"));
        assertThat(param1.get("isProperty").asBoolean(), equalTo(false));
        assertThat(param1.get("type").asText(), equalTo("int"));
        assertThat(param1.get(OBJECTTYPE).asText(), equalTo("TemplateParameter"));
        assertThat(param1.get(PROPERTY_ID).asText(), equalTo("<undefined>"));
        assertThat(param1.get(URI_FRAGMENT).asText(), equalTo("TaxPayerCollection.URI.2"));

    }
}
