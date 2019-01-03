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
import static com.reprezen.genflow.rapidml.diagram.JsonGeneratorTestFixture.REFERENCE_RESOURCE_ID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("realization/LinkRelations.rapid")
public class LinkRelationsJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testIanaLinkRelation() throws Exception {
        JsonNode referenceLink = requireReferenceLink(0);
        assertThat(referenceLink.get(OBJECTTYPE).asText(), equalTo("ReferenceLink"));
        assertThat(referenceLink.get(NAME).asText(), equalTo("selfRef"));
        assertThat(referenceLink.get(REFERENCE_RESOURCE_ID).asText(), equalTo("LinkRelationsObject"));
        assertThat(referenceLink.get("linkRelation").asText(), equalTo("rel: memento"));
    }

    @Test
    public void testCustomLinkRelation() throws Exception {
        JsonNode referenceLink = requireReferenceLink(1);
        assertThat(referenceLink.get(OBJECTTYPE).asText(), equalTo("ReferenceLink"));
        assertThat(referenceLink.get(NAME).asText(), equalTo("selfRef2"));
        assertThat(referenceLink.get(REFERENCE_RESOURCE_ID).asText(), equalTo("LinkRelationsObject"));
        assertThat(referenceLink.get("linkRelation").asText(), equalTo("rel: myLinkRel1"));
    }

    @Test
    public void testUnsetLinkRelation() throws Exception {
        JsonNode referenceLink = requireReferenceLink(2);
        assertThat(referenceLink.get(OBJECTTYPE).asText(), equalTo("ReferenceLink"));
        assertThat(referenceLink.get(NAME).asText(), equalTo("selfRef3"));
        assertThat(referenceLink.get(REFERENCE_RESOURCE_ID).asText(), equalTo("LinkRelationsObject"));
        assertThat(referenceLink.get("linkRelation"), equalTo(null));
    }

    protected JsonNode requireReferenceLink(int index) throws Exception {
        JsonNode datatype = fixture.getResourceDataType(0);
        assertThat(datatype, notNullValue());

        ArrayNode referenceLinks = fixture.getReferenceTreatments(datatype);
        JsonNode referenceLink1 = referenceLinks.get(index);
        return referenceLink1;
    }

}
