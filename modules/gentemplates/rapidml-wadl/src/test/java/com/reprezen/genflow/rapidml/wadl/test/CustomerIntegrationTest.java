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
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasElement;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasMediaType;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasName;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasStatus;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasStyle;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasType;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.validUnsetRelatedLink;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getAllParameters;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getLinks;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getParametersWithId;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getRepresentations;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getRequests;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getResponses;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("Customer.rapid")
public class CustomerIntegrationTest {

    public static final String XPATH_PRODUCT_LINK = "/customer:customerObject/customer:ordersList/customer:item/customer:lineItemsList/customer:item/customer:product/atom:link";

    protected static final String XSD_PREFIX = "customer";

    @Rule
    public WadlGeneratorIntegrationTestFixture fixture = new WadlGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedWadlIsValid() {
        fixture.assertGeneratedWadlIsValid();
    }

    @Test
    public void testNsDeclarationForGrammar() throws Exception {
        Node app = fixture.requireApplication();
        assertThat(app, hasValue("xmlns:" + XSD_PREFIX, "http://modelsolv.com/reprezen/schemas/customer/customer"));
    }

    @Test
    public void testReferenceToGrammar() throws Exception {
        fixture.requireGrammar("customer.xsd");
    }

    @Test
    public void testGetCustomerResource_Method() throws Exception {
        Node method = fixture.requireMethodById("CustomerObject", "Customer");
        assertThat(method, hasName("GET"));

        List<Node> requests = getRequests().apply(method);
        assertThat(requests.size(), equalTo(1));
        Node request = requests.get(0);

        List<Node> requestParams = getParametersWithId("getLegalPerson_request_id").apply(request);
        assertThat(requestParams.size(), equalTo(0));

    }

    @Test
    public void testGetCustomerResource_MethodResponseWithLinkedResources() throws Exception {
        Node method = fixture.requireMethodById("CustomerObject", "Customer");
        List<Node> responses = getResponses().apply(method);
        assertThat(responses.size(), equalTo(1));

        Node response1 = responses.get(0);
        assertThat(response1, hasStatus(200));

        List<Node> representations = getRepresentations().apply(response1);
        assertThat(representations.size(), equalTo(1));
        Node representation = representations.get(0);

        assertThat(representation, notNullValue());
        assertThat(representation, hasMediaType("application/xml"));
        assertThat(representation, hasElement(XSD_PREFIX + ":CustomerObject"));

        List<Node> params = getAllParameters().apply(representation);
        assertThat(params.size(), equalTo(1));

        Node param = params.get(0);
        assertThat(param, hasStyle("plain"));
        assertThat(param, hasType("xs:anyURI"));

        assertThat(param, hasName("product"));
        /// Orders/LineItems>Product
        assertThat(param, hasValue("path", XPATH_PRODUCT_LINK));

        List<Node> links = getLinks().apply(param);
        assertThat(links.size(), equalTo(1));
        Node link = links.get(0);
        assertThat(link, validUnsetRelatedLink());
        assertThat(link, hasValue("resource_type", "#ProductObjectType"));
    }

    @Test
    public void testRepresentationParameterUsesValidXpathQuery() throws Exception {
        NodeList result = fixture.querySampleXml("customer_customerObject.xml", XPATH_PRODUCT_LINK);
        // two links
        assertThat(result.getLength(), equalTo(2));
    }

}
