/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemaKeywords;
import com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemeGeneratorTestFixture;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("TaxBlaster.rapid")
public class CollectionResourceTest {
    @Rule
    public JSONSchemeGeneratorTestFixture fixture = new JSONSchemeGeneratorTestFixture();

    @Test
    public void testIsValidJSONSchema() throws Exception {
        fixture.isValidJsonSchema();
    }

    @Test
    public void hasArrayInCollectionResource() throws Exception {
        JsonNode objectNode = fixture.getDefinition("PersonCollection"); //$NON-NLS-1$
        JSONSchemeGeneratorTestFixture.checkFieldValue(JSONSchemaKeywords.TYPE, objectNode,
                JSONSchemaKeywords.TYPE_ARRAY);
    }

    @Test
    public void noArrayInObjectResource() throws Exception {
        JsonNode objectNode = fixture.getDefinition("Person"); //$NON-NLS-1$
        JSONSchemeGeneratorTestFixture.checkFieldValue(JSONSchemaKeywords.TYPE, objectNode,
                JSONSchemaKeywords.TYPE_OBJECT);
    }
}
