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

import com.reprezen.genflow.rapidml.wadl.test.WadlGeneratorIntegrationTestFixture;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("links/TwoReferences.rapid")
public class TwoReferenceLinksTest {

    public static final String TWO_REFERENCES_SAMPLE_XML = "twoReferences_dataType1Object.xml";

    private static final String XPATH_LINK1 = "/tworeferences:dataType1Object"
            + "/tworeferences:dataType1_containment1List" + "/tworeferences:item"
            + "/tworeferences:dataType2_reference1List" + "/tworeferences:item" + "/atom:link";

    private static final String XPATH_LINK2 = "/tworeferences:dataType1Object"
            + "/tworeferences:dataType1_containment2List" + "/tworeferences:item"
            + "/tworeferences:dataType2_reference1List" + "/tworeferences:item" + "/atom:link";

    private static final String XSD_PREFIX = "tworeferences";

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
                hasValue("xmlns:" + XSD_PREFIX, "http://modelsolv.com/reprezen/schemas/tworeferences/tworeferences"));
    }

    @Test
    public void testReferenceToGrammar() throws Exception {
        fixture.requireGrammar("twoReferences.xsd");
    }

    @Test
    public void testGetDataType1Resource_Method() throws Exception {
        Node method = fixture.requireMethodById("DataType1Object", "Customer");
        assertThat(method, hasName("GET"));

        List<Node> requests = getRequests().apply(method);
        assertThat(requests.size(), equalTo(1));
        Node request = requests.get(0);

        List<Node> requestParams = getAllParameters().apply(request);
        assertThat(requestParams.size(), equalTo(0));

    }

    @Test
    public void testGetDataType1Resource_MethodResponseWithLinkedResources() throws Exception {
        Node method = fixture.requireMethodById("DataType1Object", "Customer");
        List<Node> responses = getResponses().apply(method);
        assertThat(responses.size(), equalTo(1));

        Node response1 = responses.get(0);
        assertThat(response1, hasStatus(200));

        List<Node> representations = getRepresentations().apply(response1);
        assertThat(representations.size(), equalTo(1));
        Node representation = representations.get(0);

        assertThat(representation, notNullValue());
        assertThat(representation, hasMediaType("application/xml"));
        assertThat(representation, hasElement(XSD_PREFIX + ":DataType1Object"));

        List<Node> params = getAllParameters().apply(representation);
        assertThat(params.size(), equalTo(2));

        {
            Node param = params.get(0);
            assertThat(param, hasStyle("plain"));
            assertThat(param, hasType("xs:anyURI"));

            assertThat(param, hasName("dataType2_reference1"));
            // /dataType1_containment1>dataType2_reference1
            assertThat(param, hasValue("path", XPATH_LINK1));

            List<Node> links = getLinks().apply(param);
            assertThat(links.size(), equalTo(1));
            Node link = links.get(0);
            assertThat(link, validUnsetRelatedLink());
            assertThat(link, hasValue("resource_type", "#DataType3ObjectType"));
        }
        {
            Node param = params.get(1);
            assertThat(param, hasStyle("plain"));
            assertThat(param, hasType("xs:anyURI"));

            assertThat(param, hasName("dataType2_reference1"));
            // /dataType1_containment1>dataType2_reference1
            assertThat(param, hasValue("path", XPATH_LINK2));

            List<Node> links = getLinks().apply(param);
            assertThat(links.size(), equalTo(1));
            Node link = links.get(0);
            assertThat(link, validUnsetRelatedLink());
            assertThat(link, hasValue("resource_type", "#DataType3ObjectType"));
        }
    }

    @Test
    public void testRepresentationParameter1UsesValidXpathQuery() throws Exception {
        NodeList result = fixture.querySampleXml(TWO_REFERENCES_SAMPLE_XML, XPATH_LINK1);
        // one link
        assertThat(result.getLength(), equalTo(1));
    }

    @Test
    public void testRepresentationParameter2UsesValidXpathQuery() throws Exception {
        NodeList result = fixture.querySampleXml(TWO_REFERENCES_SAMPLE_XML, XPATH_LINK2);
        // single link
        assertThat(result.getLength(), equalTo(1));
    }

}
