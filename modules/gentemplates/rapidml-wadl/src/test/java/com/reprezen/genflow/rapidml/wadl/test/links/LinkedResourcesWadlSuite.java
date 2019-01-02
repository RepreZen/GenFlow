/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.test.links;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.reprezen.genflow.rapidml.wadl.test.CustomerIntegrationTest;

@RunWith(Suite.class)
@SuiteClasses({ //
        CustomerIntegrationTest.class, //
        // all classes from the links packages
        ReferenceLinksWithDifferentLengthTest.class, //
        TwoChainedReferenceLinksTest.class, //
        TwoReferenceLinksTest.class, //
})
public class LinkedResourcesWadlSuite {

}
