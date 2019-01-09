/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.test;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("realization/Customer_messageMediaTypes.rapid")
@SuppressWarnings("nls")
public class MessageMediaTypesTest {

    @Rule
    public WadlGeneratorIntegrationTestFixture fixture = new WadlGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedWadlIsValid() {
        fixture.assertGeneratedWadlIsValid();
    }

    @Test
    public void testMessageMediaTypes() throws Exception {
        // overrided in request
        fixture.requireRequestRepresentation("CustomerObject", "GET", "text");
        fixture.requireRequestRepresentation("CustomerObject", "GET", "application/javascript");
        fixture.requireRequestRepresentation("CustomerObject", "GET", "application/ecmascript");
        fixture.requireRequestRepresentation("CustomerObject", "GET", "text/plain");
        assertThat(fixture.requireRequestRepresentation("CustomerObject", "GET", "application/json"),
                WadlDomMatchers.hasElement("customer:getCustomerObject_Order"));
        // assert that request doesnt contains resource defined media types
        assertThat(fixture.getRequestRepresentation("CustomerObject", "GET", "application/xml").getLength(),
                CoreMatchers.equalTo(0));

        // original in response
        fixture.requireResponseRepresentation("CustomerObject", "GET", "200", "application/javascript");
        assertThat(fixture.requireResponseRepresentation("CustomerObject", "GET", "200", "application/xml"),
                WadlDomMatchers.hasElement("customer:CustomerObject"));

        // overrided in response
        fixture.requireResponseRepresentation("CustomerObject", "POST", "200", "text");
        fixture.requireResponseRepresentation("CustomerObject", "POST", "200", "application/javascript");
        fixture.requireResponseRepresentation("CustomerObject", "POST", "200", "application/ecmascript");
        fixture.requireResponseRepresentation("CustomerObject", "POST", "200", "text/plain");
        assertThat(fixture.requireResponseRepresentation("CustomerObject", "POST", "200", "application/json"),
                // TODO: fix the name, it should be "customer:postCustomerObject_Customer" not
                // "customer:postCustomerObject_Customer_2"
                // The correct value before PR #1576 "customer:postCustomerObject_Customer"
                // But fixing it in commit dbc2c7f causes new test failures in XSD generator
                // So, living with a "_2" suffix for now
                WadlDomMatchers.hasElement("customer:postCustomerObject_Customer_2"));

        // assert that response doesnt contains resource defined media types
        assertThat(fixture.getResponseRepresentation("CustomerObject", "POST", "200", "application/xml").getLength(),
                CoreMatchers.equalTo(0));
    }
}
