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

@SampleRestFile("TaxBlaster.rapid")
public class TaxBlasterJsonTest {

    @Rule
    public JsonGeneratorTestFixture fixture = new JsonGeneratorTestFixture();

    @Test
    public void testIsValidJson() throws Exception {
        fixture.getRoot();
    }

    @Test
    public void testIndexResource() throws Exception {
        fixture.assertObjectResourceHasName(0, "IndexObject");
    }

    @Test
    public void testTaxFilingsResource() throws Exception {
        fixture.assertCollectionResourceHasName(1, "TaxFilingCollection");
    }

    @Test
    public void testUsersResource() throws Exception {
        fixture.assertCollectionResourceHasName(3, "PersonCollection");
    }

    @Test
    public void testTaxFilingResource() throws Exception {
        fixture.assertObjectResourceHasName(2, "TaxFilingObject");
    }

    @Test
    public void testUserResource() throws Exception {
        fixture.assertObjectResourceHasName(4, "PersonObject");
    }

}
