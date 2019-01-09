/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram;

import org.junit.Rule;
import org.junit.Test;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("FiboEntity.rapid")
public class FiboEntityJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testLegalPersonResource() throws Exception {
        fixture.assertObjectResourceHasName(0, "LegalPersonObject");
    }

    @Test
    public void testNaturalPersonResource() throws Exception {
        fixture.assertObjectResourceHasName(2, "NaturalPersonObject");
    }

    @Test
    public void testBodyCorporateResource() throws Exception {
        fixture.assertObjectResourceHasName(3, "BodyCorporateObject");
    }

    @Test
    public void testArtificialPersonResource() throws Exception {
        fixture.assertObjectResourceHasName(1, "ArtificialPersonObject");
    }

}
