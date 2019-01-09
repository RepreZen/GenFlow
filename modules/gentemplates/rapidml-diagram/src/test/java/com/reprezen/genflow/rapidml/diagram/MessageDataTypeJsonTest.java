/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram;

import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.RESOURCE_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SuppressWarnings("nls")
@SampleRestFile("InclusivePropertiesOverrides.rapid")
public class MessageDataTypeJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testRequestDataType() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 0);
        assertThat(method, notNullValue());
        JsonNode request = fixture.getRequest(method);

        assertThat(request.get(RESOURCE_TYPE).asText(), equalTo("TaxFiling"));
    }

    @Test
    public void testResponseDataType() throws Exception {
        JsonNode method = fixture.getResourceMethod(0, 0);
        assertThat(method, notNullValue());
        JsonNode response = fixture.getResponses(method).get(0);

        assertThat(response.get(RESOURCE_TYPE).asText(), equalTo("TaxFiling"));
    }
}
