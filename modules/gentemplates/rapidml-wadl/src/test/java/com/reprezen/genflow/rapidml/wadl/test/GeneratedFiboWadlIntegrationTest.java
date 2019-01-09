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
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getParametersWithId;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getRepresentations;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getRequests;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getResponses;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("FiboEntity.rapid")
@SuppressWarnings("nls")
public class GeneratedFiboWadlIntegrationTest {

	private static final String XSD_PREFIX = "fiboentity";
	@Rule
	public WadlGeneratorIntegrationTestFixture fixture = new WadlGeneratorIntegrationTestFixture();

	@Test
	public void testGeneratedWadlIsValid() {
		fixture.assertGeneratedWadlIsValid();
	}

	@Test
	public void testNsDeclarationForGrammar() throws Exception {
		Node app = fixture.requireApplication();
		assertThat(app, hasValue("xmlns:" + XSD_PREFIX, "http://modelsolv.com/reprezen/schemas/fiboentity/fiboentity"));
	}

	@Test
	public void testReferenceToGrammar() throws Exception {
		fixture.requireGrammar("fiboEntity.xsd");
	}

	@Test
	public void testLegalPersonResource() throws Exception {
		Node node = fixture.requireResource("LegalPersonObject");
		assertThat(node, hasValue("path", "LegalPerson/{name}"));
		assertThat(node, hasType("#LegalPersonObjectType"));
	}

	@Test
	public void testLegalPersonResource_Parameters() throws Exception {
		Node resource = fixture.requireResourceType("LegalPersonObject");
		List<Node> params = getParametersWithId("LegalPersonObject_resource_name").apply(resource);
		assertThat(params.size(), equalTo(1));
		Node param = params.get(0);
		assertThat(param, hasName("name"));
		assertThat(param, hasStyle("template"));
		assertThat(param, hasType("xs:string"));
	}

	@Test
	public void testNaturalPersonResource() throws Exception {
		Node node = fixture.requireResource("NaturalPersonObject");
		assertThat(node, hasValue("path", "NaturalPerson/{name}"));
		assertThat(node, hasType("#NaturalPersonObjectType"));
	}

	@Test
	public void testNaturalPersonResource_Parameters() throws Exception {
		Node resource = fixture.requireResourceType("NaturalPersonObject");
		List<Node> params = getParametersWithId("NaturalPersonObject_resource_name").apply(resource);
		assertThat(params.size(), equalTo(1));
		Node param = params.get(0);
		assertThat(param, hasName("name"));
		assertThat(param, hasStyle("template"));
		assertThat(param, hasType("xs:string"));
	}

	@Test
	public void testBodyCorporateResource() throws Exception {
		Node node = fixture.requireResource("BodyCorporateObject");
		assertThat(node, hasValue("path", "BodyCorporate/{name}"));
		assertThat(node, hasType("#BodyCorporateObjectType"));
	}

	@Test
	public void testBodyCorporateResource_Parameters() throws Exception {
		Node resource = fixture.requireResourceType("BodyCorporateObject");
		System.out.println(resource);
		List<Node> params = getParametersWithId("BodyCorporateObject_resource_name").apply(resource);
		assertThat(params.size(), equalTo(1));
		Node param = params.get(0);
		assertThat(param, hasName("name"));
		assertThat(param, hasStyle("template"));
		assertThat(param, hasType("xs:string"));
	}

	@Test
	public void testLegalPersonResourceType() throws Exception {
		fixture.requireResourceType("LegalPersonObject");
	}

	@Test
	public void testGetLegalPerson_Method() throws Exception {
		Node method = fixture.requireMethodById("LegalPersonObject", "getLegalPerson");
		assertThat(method, hasName("GET"));

		List<Node> requests = getRequests().apply(method);
		assertThat(requests.size(), equalTo(1));
		Node request = requests.get(0);

		List<Node> requestParams = getParametersWithId("getLegalPerson_request_id").apply(request);
		assertThat(requestParams.size(), equalTo(1));
		Node requestParam = requestParams.get(0);

		assertThat(requestParam, hasName("id"));
		assertThat(requestParam, hasStyle("query"));
		assertThat(requestParam, hasType("xs:string"));
	}

	@Test
	public void testGetLegalPerson_MethodResponse() throws Exception {
		Node method = fixture.requireMethodById("LegalPersonObject", "getLegalPerson");
		List<Node> responses = getResponses().apply(method);
		assertThat(responses.size(), equalTo(2));

		Node response1 = responses.get(0);
		assertThat(response1, notNullValue());
		assertThat(response1, hasStatus(200));

		List<Node> representations = getRepresentations().apply(response1);
		assertThat(representations.size(), equalTo(1));
		Node representation = representations.get(0);
		assertThat(representation, notNullValue());
		assertThat(representation, hasMediaType("application/xml"));
		assertThat(representation, hasElement(XSD_PREFIX + ":LegalPersonObject"));

		Node response2 = responses.get(1);
		assertThat(response2, notNullValue());
		assertThat(response2, hasStatus(404));
	}

	@Test
	public void testPutLegalPerson_MethodRequest() throws Exception {
		Node method = fixture.requireMethodById("LegalPersonObject", "putLegalPerson");
		assertThat(method, hasName("PUT"));

		List<Node> requests = getRequests().apply(method);
		assertThat(requests.size(), equalTo(1));
		Node request = requests.get(0);

		List<Node> requestParams = getParametersWithId("putLegalPerson_request_id").apply(request);
		assertThat(requestParams.size(), equalTo(1));
		Node requestParam = requestParams.get(0);

		assertThat(requestParam, hasName("id"));
		assertThat(requestParam, hasStyle("query"));
		assertThat(requestParam, hasType("xs:string"));

		List<Node> representations = getRepresentations().apply(request);
		assertThat(representations.size(), equalTo(1));
		Node representation = representations.get(0);
		assertThat(representation, notNullValue());
		assertThat(representation, hasMediaType("application/xml"));
		assertThat(representation, hasElement(XSD_PREFIX + ":LegalPersonObject"));
	}

	@Test
	public void testPutLegalPerson_MethodResponse() throws Exception {
		Node method = fixture.requireMethodById("LegalPersonObject", "putLegalPerson");

		List<Node> responses = getResponses().apply(method);
		assertThat(responses.size(), equalTo(1));
		Node response = responses.get(0);
		assertThat(response, notNullValue());
		assertThat(response, hasStatus(200));
		List<Node> representations = getRepresentations().apply(response);
		assertThat(representations.size(), equalTo(0));
	}

	@Test
	public void testNaturalPersonResourceType() throws Exception {
		fixture.requireResourceType("NaturalPersonObject");
		// methods are similar to those of LegalPersonResource
	}

	@Test
	public void testBodyCorporateResourceType() throws Exception {
		fixture.requireResourceType("BodyCorporateObject");
	}

	@Test
	public void testGetBodyCorporate_MethodRequest() throws Exception {
		Node method = fixture.requireMethodById("BodyCorporateObject", "getBodyCorporate");
		assertThat(method, hasName("GET"));

		List<Node> requests = getRequests().apply(method);
		assertThat(requests.size(), equalTo(1));
		Node request = requests.get(0);

		List<Node> requestParams = getParametersWithId("getBodyCorporate_request_id").apply(request);
		assertThat(requestParams.size(), equalTo(1));
		Node requestParam = requestParams.get(0);

		assertThat(requestParam, hasName("id"));
		assertThat(requestParam, hasStyle("query"));
		assertThat(requestParam, hasType("xs:string"));

		List<Node> representations = getRepresentations().apply(request);
		assertThat(representations.size(), equalTo(0));
	}

	@Test
	public void testGetBodyCorporate_MethodResponse() throws Exception {
		Node method = fixture.requireMethodById("BodyCorporateObject", "getBodyCorporate");
		List<Node> responses = getResponses().apply(method);
		assertThat(responses.size(), equalTo(2));

		Node response1 = responses.get(0);
		assertThat(response1, notNullValue());
		assertThat(response1, hasStatus(200));

		List<Node> representations = getRepresentations().apply(response1);
		assertThat(representations.size(), equalTo(4));

		{
			Node representation = requirePresentationByMediaType(representations, "application/xml");
			assertThat(representation, hasElement(XSD_PREFIX + ":BodyCorporateObject"));
		}
		{
			Node representation = requirePresentationByMediaType(representations, "application/javascript");
			assertThat(representation, not(hasElement(XSD_PREFIX + ":BodyCorporateObject")));
		}
		{
			// super type of application/javascript
			Node representation = requirePresentationByMediaType(representations, "application/ecmascript");
			assertThat(representation, not(hasElement(XSD_PREFIX + ":BodyCorporateObject")));
		}
		{
			Node representation = requirePresentationByMediaType(representations, "application/json");
			assertThat(representation, hasElement(XSD_PREFIX + ":BodyCorporateObject"));
		}

		Node response2 = responses.get(1);
		assertThat(response2, notNullValue());
		assertThat(response2, hasStatus(404));
	}

	@Test
	public void testPutBodyCorporate_MethodRequest() throws Exception {
		Node method = fixture.requireMethodById("BodyCorporateObject", "putBodyCorporate");
		assertThat(method, hasName("PUT"));

		List<Node> requests = getRequests().apply(method);
		assertThat(requests.size(), equalTo(1));
		Node request = requests.get(0);

		List<Node> requestParams = getParametersWithId("putBodyCorporate_request_id").apply(request);
		assertThat(requestParams.size(), equalTo(1));
		Node requestParam = requestParams.get(0);

		assertThat(requestParam, hasName("id"));
		assertThat(requestParam, hasStyle("query"));
		assertThat(requestParam, hasType("xs:string"));

		List<Node> representations = getRepresentations().apply(request);
		assertThat(representations.size(), equalTo(4));

		{
			Node representation = requirePresentationByMediaType(representations, "application/xml");
			assertThat(representation, hasElement(XSD_PREFIX + ":BodyCorporateObject"));
		}
		{
			requirePresentationByMediaType(representations, "application/javascript");
		}
		{
			// super type of application/javascript
			Node representation = requirePresentationByMediaType(representations, "application/ecmascript");
			assertThat(representation, not(hasElement(XSD_PREFIX + ":BodyCorporateObject")));
		}
		{
			Node representation = requirePresentationByMediaType(representations, "application/json");
			assertThat(representation, hasElement(XSD_PREFIX + ":BodyCorporateObject"));
		}

		List<Node> responses = getResponses().apply(method);
		assertThat(responses.size(), equalTo(1));
		// empty response

	}

	@Test
	public void testArtificialPersonResourceType() throws Exception {
		fixture.requireResourceType("ArtificialPersonObject");
	}

	@Test
	public void testGetArtificialPerson_MethodRequest() throws Exception {
		Node method = fixture.requireMethodById("ArtificialPersonObject", "getArtificialPerson");
		assertThat(method, hasName("GET"));

		List<Node> requests = getRequests().apply(method);
		assertThat(requests.size(), equalTo(1));
		Node request = requests.get(0);

		List<Node> requestParams = getParametersWithId("getArtificialPerson_request_id").apply(request);
		assertThat(requestParams.size(), equalTo(1));
		Node requestParam = requestParams.get(0);

		assertThat(requestParam, hasName("id"));
		assertThat(requestParam, hasStyle("query"));
		assertThat(requestParam, hasType("xs:int"));

		List<Node> representations = getRepresentations().apply(request);
		assertThat(representations.size(), equalTo(0));
	}

	@Test
	public void testGetArtificialPerson_MethodResponse() throws Exception {
		Node method = fixture.requireMethodById("ArtificialPersonObject", "getArtificialPerson");
		List<Node> responses = getResponses().apply(method);
		assertThat(responses.size(), equalTo(2));

		Node response1 = responses.get(0);
		assertThat(response1, notNullValue());
		assertThat(response1, hasStatus(200));

		List<Node> representations = getRepresentations().apply(response1);
		assertThat(representations.size(), equalTo(1));

		{
			Node representation = representations.get(0);
			assertThat(representation, hasMediaType("application/xml"));
			assertThat(representation, hasElement(XSD_PREFIX + ":ArtificialPersonObject"));
		}

		Node response2 = responses.get(1);
		assertThat(response2, notNullValue());
		assertThat(response2, hasStatus(404));
	}

	@Test
	public void testPutArtificialPerson_MethodRequest() throws Exception {
		Node method = fixture.requireMethodById("ArtificialPersonObject", "putArtificialPerson");
		assertThat(method, hasName("PUT"));

		List<Node> requests = getRequests().apply(method);
		assertThat(requests.size(), equalTo(1));
		Node request = requests.get(0);

		List<Node> requestParams = getParametersWithId("putArtificialPerson_request_id").apply(request);
		assertThat(requestParams.size(), equalTo(1));
		Node idParam = requestParams.get(0);

		assertThat(idParam, hasName("id"));
		assertThat(idParam, hasStyle("query"));
		assertThat(idParam, hasType("xs:int"));

		requestParams = getParametersWithId("putArtificialPerson_request_registeredNumber").apply(request);
		assertThat(requestParams.size(), equalTo(1));
		Node registeredNumberParam = requestParams.get(0);

		assertThat(registeredNumberParam, hasName("registeredNumber"));
		assertThat(registeredNumberParam, hasStyle("query"));
		assertThat(registeredNumberParam, hasType("xs:long"));

		List<Node> representations = getRepresentations().apply(request);
		assertThat(representations.size(), equalTo(1));

		{
			Node representation = representations.get(0);
			assertThat(representation, hasMediaType("application/xml"));
			assertThat(representation, hasElement(XSD_PREFIX + ":ArtificialPersonObject"));
		}

		List<Node> responses = getResponses().apply(method);
		assertThat(responses.size(), equalTo(1));
		// empty response

	}

	private Node requirePresentationByMediaType(List<Node> presentations, String mediaType) {
		for (Node node : presentations) {
			if (hasMediaType(mediaType).matches(node)) {
				return node;
			}
		}
		Assert.fail("Presentation with mediaType " + mediaType + " not found");
		return null;
	}
}
