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
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("Enumerations.rapid")
public class EnumerationsJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testEnumerationsResource() throws Exception {
        fixture.assertObjectResourceHasName(0, "EnumerationsObject");
    }

    @Test
    public void testEnumerationTypes() throws Exception {
        JsonNode datatype = fixture.getResourceDataType(0);
        assertThat(datatype, notNullValue());
        assertThat(datatype.get(OBJECTTYPE).asText(), equalTo("DataType"));
        assertThat(datatype.get(NAME).asText(), equalTo("UseEnum"));

        ArrayNode properties = fixture.getProperties(datatype);
        assertThat(properties.size(), equalTo(2));

        JsonNode property1 = properties.get(0);
        assertThat(property1.get(OBJECTTYPE).asText(), equalTo("PrimitiveProperty"));
        assertThat(property1.get(NAME).asText(), equalTo("useIntEnum"));
        assertThat(property1.get(TYPE).asText(), equalTo("Record"));

        JsonNode property2 = properties.get(1);
        assertThat(property2.get(OBJECTTYPE).asText(), equalTo("PrimitiveProperty"));
        assertThat(property2.get(NAME).asText(), equalTo("useStringEnum"));
        assertThat(property2.get(TYPE).asText(), equalTo("Suit"));
    }

}