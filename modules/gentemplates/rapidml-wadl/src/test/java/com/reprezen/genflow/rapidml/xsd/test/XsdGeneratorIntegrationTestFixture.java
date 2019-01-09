/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test;

import static com.reprezen.genflow.rapidml.wadl.XmlDomMatchers.hasValue;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasMaxOccurs;
import static com.reprezen.genflow.rapidml.xsd.test.XsdDomMatchers.hasMinOccurs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.xml.validation.Schema;
import javax.xml.xpath.XPathConstants;

import org.junit.runner.Description;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.reprezen.genflow.rapidml.wadl.GeneratorIntegrationTestFixture;

public class XsdGeneratorIntegrationTestFixture extends GeneratorIntegrationTestFixture {

	public static final String UNBOUNDED = "unbounded";

	public XsdGeneratorIntegrationTestFixture() {
		super("xsd");
	}

	@Override
	protected void starting(Description description) {
		super.starting(description);
		try {
			copySpecFile("atom.xsd");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void copySpecFile(String specFileName) throws IOException {
		File file = new File(specFileName);
		if (!file.isAbsolute()) {
			file = new File(getTemporaryDir(), specFileName);
		}
		InputStream srcFile = getSpecFile(specFileName);
		try {
			ByteStreams.copy(srcFile, Files.asByteSink(file).openStream());
		} finally {
			Closeables.closeQuietly(srcFile);
		}
	}

	public void assertGeneratedXsdIsValidSchema() {
		assertTrue(getGeneratedFile().exists());
		loadAndValidateXsdSchema(getGeneratedFile());
	}

	public Node requireSchemaImport(String namespace) throws Exception {
		NodeList list = (NodeList) query("/schema/import[@namespace='" + namespace + "']", XPathConstants.NODESET);
		assertThat(list.getLength(), equalTo(1));
		return list.item(0);
	}

	public Node getGlobalComplexType(String name) throws Exception {
		Node node = (Node) query("/schema/complexType[@name='" + name + "']", XPathConstants.NODE);
		return node;
	}

	public Node requireGlobalComplexType(String name) throws Exception {
		Node node = getGlobalComplexType(name);
		assertNotNull(node);
		return node;
	}

	public Node getAttributeOfComplexType(String complextTypeName, String attributeName) throws Exception {
		Node node = (Node) query(
				"/schema/complexType[@name='" + complextTypeName + "']/attribute[@name='" + attributeName + "']",
				XPathConstants.NODE);
		return node;
	}

	public Node requireAttributeOfComplexType(String complextTypeName, String attributeName) throws Exception {
		Node node = getAttributeOfComplexType(complextTypeName, attributeName);
		assertThat("Cannot locate an attribute with the name " + attributeName, node, notNullValue());
		return node;
	}

	public NodeList getAllBlockElements(String complextTypeName, String elementName) throws Exception {
		NodeList list = (NodeList) query(
				"/schema/complexType[@name='" + complextTypeName + "']/all/element[@name='" + elementName + "']",
				XPathConstants.NODESET);
		return list;
	}

	public Node requireAllBlockElement(String complextTypeName, String elementName) throws Exception {
		NodeList list = getAllBlockElements(complextTypeName, elementName);
		assertThat("Cannot locate an all block element with the name " + elementName, list.getLength(), equalTo(1));
		return list.item(0);
	}

	public NodeList getSequenceElementsInAllBlock(String complexTypeName, String elementName, String subElementName)
			throws Exception {
		NodeList list = (NodeList) query("/schema/complexType[@name='" + complexTypeName + "']/all/element[@name='"
				+ elementName + "']/complexType/sequence/element[@name='" + subElementName + "']",
				XPathConstants.NODESET);
		return list;
	}

	public Node requireSequenceElementInAllBlock(String complexTypeName, String elementName, String subElementName)
			throws Exception {
		NodeList list = getSequenceElementsInAllBlock(complexTypeName, elementName, subElementName);
		assertThat("Cannot locate a squence element named " + subElementName + " in an all-block named " + elementName
				+ " in complex type " + complexTypeName, list.getLength(), equalTo(1));
		return list.item(0);
	}

	public void assertIsCorrectAtomLink(Node atomLink) {
		assertIsCorrectAtomLink(atomLink, null);
	}

	public void assertIsCorrectAtomLink(Node atomLink, String referenceName) {
		assertThat(atomLink, hasValue("ref", "atom:link"));
		assertThat(atomLink, hasMinOccurs(1));
		assertThat(atomLink, hasMaxOccurs(1));
	}

	public Node requireSchema() throws Exception {
		NodeList list = (NodeList) query("/schema", XPathConstants.NODESET);
		assertThat(list.getLength(), equalTo(1));
		return list.item(0);
	}

	public void validateXmlAgainstGeneratedSchema(String xmlSampleFileName) throws URISyntaxException {
		validateXmlAgainstGeneratedSchema(getSampleXmlFile(xmlSampleFileName));
	}

	public void validateXmlAgainstGeneratedSchema(File xmlFile) {
		Schema generatedSchema = loadAndValidateXsdSchema(getGeneratedFile());
		validateXmlAgainstXsd(generatedSchema, xmlFile);
	}

}
