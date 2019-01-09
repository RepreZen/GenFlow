/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test.links;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.reprezen.genflow.rapidml.xsd.test.GeneratedCustomerXsdIntegrationTest;

@RunWith(Suite.class)
@SuiteClasses({ //
        GeneratedCustomerXsdIntegrationTest.class, //
        // all classes from the links packages
        DefaultLinkDescriptor_Assigned.class, //
        DefaultLinkDescriptor_Default.class, //
        DefaultLinkDescriptor_DefaultImplicit.class, //
        DefaultLinkDescriptor_WODefault.class, //
        DefaultResource_AssignedTargetResource.class, //
        DefaultResource_MultipleResourcesWDefaultTest.class, //
        DefaultResource_MultipleResourcesWODefaultTest.class, //
        DefaultResource_NoResourceTest.class, //
        DefaultResource_SingleResourceTest.class, //
        GeneratedExplicitLinkDescriptorXsdIntegrationTest.class, //
        GeneratedReferenceLinkXsdIntegrationTest.class, //
        ReferenceViaDifferentPathsTest2.class, //
        ReferenceViaDifferentPathsXsdIntegrationTest.class, //
        SeveralDirectReferencesTest.class, ReferencesWithDifferentDepthTest.class })
public class LinkedResourcesSuite {

}
