/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.test.links;

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
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getRepresentations;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getResponses;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.rapidml.wadl.test.WadlGeneratorIntegrationTestFixture;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("CollectionResource.rapid")
public class CollectionResourceHyperlinkTest {

    private static final String XPATH_LINK1 = "/personapi:personCollection/personapi:objectResourceLinkList/personapi:item/atom:link";

    private static final String XSD_PREFIX = "personapi";

    @Rule
    public WadlGeneratorIntegrationTestFixture fixture = new WadlGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedWadlIsValid() {
        fixture.assertGeneratedWadlIsValid();
    }

    @Test
    public void testReferenceToGrammar() throws Exception {
        fixture.requireGrammar("personAPI.xsd");
    }

    @Test
    public void testHyperlink() throws Exception {
        Node method = fixture.requireMethodById("PersonCollection", "getPersonCollection");
        List<Node> responses = getResponses().apply(method);
        assertThat(responses.size(), equalTo(1));

        Node response1 = responses.get(0);
        assertThat(response1, hasStatus(200));

        List<Node> representations = getRepresentations().apply(response1);
        assertThat(representations.size(), equalTo(1));
        Node representation = representations.get(0);

        assertThat(representation, notNullValue());
        assertThat(representation, hasMediaType("application/xml"));
        assertThat(representation, hasElement(XSD_PREFIX + ":PersonCollection"));

        List<Node> params = getAllParameters().apply(representation);
        assertThat(params.size(), equalTo(1));

        {
            Node param = params.get(0);
            assertThat(param, hasStyle("plain"));
            assertThat(param, hasType("xs:anyURI"));

            assertThat(param, hasName("objectResourceLink"));
            // /dataType1_containment1>dataType2_reference1
            assertThat(param, hasValue("path", XPATH_LINK1));

            List<Node> links = getLinks().apply(param);
            assertThat(links.size(), equalTo(1));
            Node link = links.get(0);
            assertThat(link, validUnsetRelatedLink());
            assertThat(link, hasValue("resource_type", "#PersonObjectType"));
        }
    }

}
