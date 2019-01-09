/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.test;

import static com.reprezen.genflow.rapidml.wadl.XmlDomMatchers.hasValue;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("ApiarySample.rapid")
public class ApiarySampleIntegrationTest {

    private static final String XSD_PREFIX = "sample_api_v2";
    @Rule
    public WadlGeneratorIntegrationTestFixture fixture = new WadlGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedWadlIsValid() {
        fixture.assertGeneratedWadlIsValid();
    }

    @Test
    public void testNsDeclarationForGrammar() throws Exception {
        Node app = fixture.requireApplication();
        assertThat(app,
                hasValue("xmlns:" + XSD_PREFIX, "http://modelsolv.com/reprezen/schemas/apiarysample/sample_api_v2"));
    }

    @Test
    public void testReferenceToGrammar() throws Exception {
        fixture.requireGrammar("sample_API_v2.xsd");
    }

}
