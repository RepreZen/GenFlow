/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test.links;

import org.junit.Ignore;
import org.junit.Test;

import com.reprezen.genflow.rapidml.xsd.test.GeneratedCustomerXsdIntegrationTest;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("realization/Customer_inlineLinkDescriptor2.rapid")
public class InlineLinkDescriptorXsdTest extends GeneratedCustomerXsdIntegrationTest {
    // The realization/Customer_inlineLinkDescriptor.zen model is similar to the customer.zen model and many tests for
    // the customer.zen, including the tests for reference links, are applicable here

    @Test
    @Override
    @Ignore
    public void testCustomerResourceXmlInstance() throws Exception {
        // the test case from the superclass is not applicable
    }
}
