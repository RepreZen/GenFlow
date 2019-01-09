/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.validation.Schema;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.reprezen.genflow.rapidml.wadl.GeneratorIntegrationTestFixture;

@SuppressWarnings("nls")
public class WadlGeneratorIntegrationTestFixture extends GeneratorIntegrationTestFixture {

	private static Schema wadlXmlSchema = null;

	public WadlGeneratorIntegrationTestFixture() {
		super("wadl");
	}

	public void assertGeneratedWadlIsValid() {
		assertTrue(getGeneratedFile().exists());
		validateXmlAgainstXsd(getWadlXmlSchema(), getGeneratedFile());
	}

	public Node requireResource(String resourceName) throws Exception {
		NodeList list = (NodeList) query("/application/resources/resource[@id='" + resourceName + "']",
				XPathConstants.NODESET);
		assertThat("Cannot locate a sequence element with the name " + resourceName, list.getLength(), equalTo(1));
		return list.item(0);
	}

	public Node requireResourceType(String resourceTypeName) throws Exception {
		NodeList list = (NodeList) query(getResourceTypeXpathQuery(resourceTypeName), XPathConstants.NODESET);
		assertThat("Cannot locate a resource type with the name " + resourceTypeName, list.getLength(), equalTo(1));
		return list.item(0);
	}

	public Node requireApplication() throws Exception {
		Node node = (Node) query("/application", XPathConstants.NODE);
		assertThat("Cannot locate an application.", node, not(nullValue()));
		return node;
	}

	public Node requireGrammar(String grammarHref) throws Exception {
		NodeList list = (NodeList) query("/application/grammars/include[@href='" + grammarHref + "']",
				XPathConstants.NODESET);
		assertThat("Cannot locate a sequence element with the name " + grammarHref, list.getLength(), equalTo(1));
		return list.item(0);
	}

	public Node requireMethod(String resourceName, String methodType) throws Exception {
		NodeList list = (NodeList) query(getMethodXpathQuery(resourceName, methodType), XPathConstants.NODESET);
		assertThat("Cannot locate a sequence element with the name " + methodType, list.getLength(), equalTo(1));
		return list.item(0);
	}

	public Node requireMethodById(String resourceName, String methodId) throws Exception {
		Node node = (Node) query(getMethodByIdXpathQuery(resourceName, methodId), XPathConstants.NODE);
		assertThat("Cannot locate method with the id ", notNullValue());
		return node;
	}

	public Node requireMethodResponse(String resourceName, String methodType, String responseStatus) throws Exception {
		NodeList list = (NodeList) query(
				getMethodXpathQuery(resourceName, methodType) + "/response[@status='" + responseStatus + "']",
				XPathConstants.NODESET);
		assertThat("Cannot locate a sequence element with the name " + responseStatus, list.getLength(), equalTo(1));
		return list.item(0);
	}

	public Node requireParam(String resourceName, String paramName) throws Exception {
		String paramId = resourceName + "_resource_" + paramName;
		NodeList list = (NodeList) query(
				"/application/resources/resource[@id='" + resourceName + "']/param[@id='" + paramId + "']",
				XPathConstants.NODESET);
		assertThat("Cannot locate a parameter element with the id " + paramId, list.getLength(), equalTo(1));
		return list.item(0);
	}

	public Node requireRequestRepresentation(String resourceName, String methodType, String representation)
			throws Exception {
		NodeList list = getRequestRepresentation(resourceName, methodType, representation);
		assertThat("Cannot locate a sequence element with the name " + representation, list.getLength(), equalTo(1));
		return list.item(0);
	}

	public NodeList getRequestRepresentation(String resourceName, String methodType, String representation)
			throws Exception {
		return (NodeList) query(getMethodXpathQuery(resourceName, methodType) + "/request/representation[@mediaType='"
				+ representation + "']", XPathConstants.NODESET);
	}

	public Node requireResponseRepresentation(String resourceName, String methodType, String responseStatus,
			String representation) throws Exception {
		NodeList list = getResponseRepresentation(resourceName, methodType, responseStatus, representation);
		assertThat("Cannot locate a sequence element with the name " + representation, list.getLength(), equalTo(1));
		return list.item(0);
	}

	public NodeList getResponseRepresentation(String resourceName, String methodType, String responseStatus,
			String representation) throws Exception {
		return (NodeList) query(getMethodXpathQuery(resourceName, methodType) + "/response[@status='" + responseStatus
				+ "']/representation[@mediaType='" + representation + "']", XPathConstants.NODESET);
	}

	private String getMethodXpathQuery(String resourceName, String methodType) {
		return getResourceTypeXpathQuery(resourceName) + "/method[@name='" + methodType + "']";
	}

	private String getMethodByIdXpathQuery(String resourceName, String methodId) {
		return getResourceTypeXpathQuery(resourceName) + "/method[@id='" + methodId + "']";
	}

	private String getResourceTypeXpathQuery(String resourceName) {
		return "/application/resource_type[@id='" + resourceName + "Type" + "']";
	}

	public NodeList querySampleXml(String sampleXmlFileName, String query) throws Exception {
		File customerSample = getSampleXmlFile(sampleXmlFileName);
		String xPathPattern = "(/)?(\\w+)(:)(\\w+)";
		String queryWoNs = query.replaceAll(xPathPattern, "$1$4");
		NodeList result = (NodeList) query(customerSample, queryWoNs, XPathConstants.NODESET);
		return result;
	}

	/**
	 * @return the wadlXmlSchema
	 */
	public Schema getWadlXmlSchema() {
		if (wadlXmlSchema == null) {
			wadlXmlSchema = loadAndValidateXsdSchema(getSpecFile("wadl.xsd"));
		}
		return wadlXmlSchema;
	}
}
