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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("TaxFiling.rapid")
public class TaxFilingJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testResources() throws Exception {
        ArrayNode resources = fixture.getResources();
        assertThat(resources.size(), equalTo(2));

        JsonNode taxFiling = resources.get(0);
        assertThat(taxFiling, notNullValue());
        assertThat(taxFiling.get(OBJECTTYPE).asText(), equalTo("ObjectResource"));
        assertThat(taxFiling.get(NAME).asText(), equalTo("TaxFilingObject"));

        JsonNode taxPayer = resources.get(1);
        assertThat(taxPayer, notNullValue());
        assertThat(taxPayer.get(OBJECTTYPE).asText(), equalTo("ObjectResource"));
        assertThat(taxPayer.get(NAME).asText(), equalTo("TaxPayerObject"));
    }

}
