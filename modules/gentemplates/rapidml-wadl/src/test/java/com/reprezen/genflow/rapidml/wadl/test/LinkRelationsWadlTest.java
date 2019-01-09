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
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.validUnsetSelfLink;
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

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("realization/LinkRelations.rapid")
public class LinkRelationsWadlTest {

    private static final String XPATH_LINK1 = "/linkrelationsinterface:linkRelationsObject/linkrelationsinterface:selfRef/atom:link";

    private static final String XPATH_LINK2 = "/linkrelationsinterface:linkRelationsObject/linkrelationsinterface:selfRef2/atom:link";

    private static final String XPATH_LINK3 = "/linkrelationsinterface:linkRelationsObject/linkrelationsinterface:selfRef3/atom:link";

    private static final String XSD_PREFIX = "linkrelationsinterface";

    @Rule
    public WadlGeneratorIntegrationTestFixture fixture = new WadlGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedWadlIsValid() {
        fixture.assertGeneratedWadlIsValid();
    }

    @Test
    public void testNsDeclarationForGrammar() throws Exception {
        Node app = fixture.requireApplication();
        assertThat(app, hasValue("xmlns:" + XSD_PREFIX,
                "http://modelsolv.com/reprezen/schemas/linkrelations/linkrelationsinterface"));
    }

    @Test
    public void testReferenceToGrammar() throws Exception {
        fixture.requireGrammar("linkRelationsInterface.xsd");
    }

    @Test
    public void testIanaLinkRelation() throws Exception {
        Node param = requireMethodResponseParam(0);
        assertThat(param, hasStyle("plain"));
        assertThat(param, hasType("xs:anyURI"));

        assertThat(param, hasName("selfRef"));
        assertThat(param, hasValue("path", XPATH_LINK1));

        List<Node> links = getLinks().apply(param);
        assertThat(links.size(), equalTo(1));
        Node link = links.get(0);
        assertThat(link, hasValue("rel", "memento"));
        assertThat(link, hasValue("resource_type", "#LinkRelationsObjectType"));
    }

    @Test
    public void testCustomLinkRelation() throws Exception {
        Node param = requireMethodResponseParam(1);
        assertThat(param, hasStyle("plain"));
        assertThat(param, hasType("xs:anyURI"));

        assertThat(param, hasName("selfRef2"));
        assertThat(param, hasValue("path", XPATH_LINK2));

        List<Node> links = getLinks().apply(param);
        assertThat(links.size(), equalTo(1));
        Node link = links.get(0);
        assertThat(link, hasValue("rel", "myLinkRel1"));
        assertThat(link, hasValue("resource_type", "#LinkRelationsObjectType"));
    }

    @Test
    public void testUnsetLinkRelation() throws Exception {
        Node param = requireMethodResponseParam(2);
        assertThat(param, hasStyle("plain"));
        assertThat(param, hasType("xs:anyURI"));

        assertThat(param, hasName("selfRef3"));
        assertThat(param, hasValue("path", XPATH_LINK3));

        List<Node> links = getLinks().apply(param);
        assertThat(links.size(), equalTo(1));
        Node link = links.get(0);
        assertThat(link, hasValue("resource_type", "#LinkRelationsObjectType"));
        assertThat(link, validUnsetSelfLink());
    }

    private Node requireMethodResponseParam(int index) throws Exception {
        Node method = fixture.requireMethodById("LinkRelationsObject", "retrieveLinkRelationsObject");
        List<Node> responses = getResponses().apply(method);
        assertThat(responses.size(), equalTo(1));

        Node response1 = responses.get(0);
        assertThat(response1, hasStatus(200));

        List<Node> representations = getRepresentations().apply(response1);
        assertThat(representations.size(), equalTo(1));
        Node representation = representations.get(0);

        assertThat(representation, notNullValue());
        assertThat(representation, hasMediaType("application/xml"));
        assertThat(representation, hasElement(XSD_PREFIX + ":LinkRelationsObject"));

        List<Node> params = getAllParameters().apply(representation);
        return params.get(index);
    }

}
