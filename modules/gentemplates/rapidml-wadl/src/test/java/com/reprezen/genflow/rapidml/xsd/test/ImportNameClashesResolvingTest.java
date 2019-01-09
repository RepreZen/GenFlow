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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.xml.xpath.XPathConstants;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("import/dataModelNameClashes/dependent_model.rapid")
public class ImportNameClashesResolvingTest {

	@Rule
	public XsdGeneratorIntegrationTestFixture fixture = new MultipleXsdGenerationIntegrationTextFixture(
			"interface.xsd");

	@Test
	public void testGeneratedXsdIsValidSchema() {
		fixture.assertGeneratedXsdIsValidSchema();
	}

	@Test
	public void testPrefixes() throws Exception {
		Node schemaNode = (Node) fixture.query("/schema", XPathConstants.NODE);
		Assert.assertThat(schemaNode, hasValue("xmlns:al", "http://modelsolv.com/reprezen/schemas/aliased/dm2"));
		Assert.assertThat(schemaNode,
				hasValue("xmlns:clashWithImported.dm3", "http://modelsolv.com/reprezen/schemas/clashwithimported/dm3"));
		Assert.assertThat(schemaNode,
				hasValue("xmlns:clashWithLocal.dm", "http://modelsolv.com/reprezen/schemas/clashwithlocal/dm"));
		Assert.assertThat(schemaNode, hasValue("xmlns:dm", "http://modelsolv.com/reprezen/schemas/dependent/dm"));
		Assert.assertThat(schemaNode,
				hasValue("xmlns:dm2", "http://modelsolv.com/reprezen/schemas/clashwithaliased/dm2"));
		Assert.assertThat(schemaNode,
				hasValue("xmlns:imported.dm3", "http://modelsolv.com/reprezen/schemas/imported/dm3"));
		Assert.assertThat(schemaNode, hasValue("xmlns:ns.clashWithImported.dm3", "http://ns/clashwithimported/dm3"));
	}

	@Test
	public void testImport() throws Exception {
		voidCheckImportNamespaceFromFile("http://modelsolv.com/reprezen/schemas/dependent/dm", "dm.xsd");
		voidCheckImportNamespaceFromFile("http://ns/clashwithimported/dm3", "ns-clashWithImported-dm3.xsd");
		voidCheckImportNamespaceFromFile("http://modelsolv.com/reprezen/schemas/imported/dm3", "imported-dm3.xsd");
		voidCheckImportNamespaceFromFile("http://modelsolv.com/reprezen/schemas/aliased/dm2", "aliased-dm2.xsd");
		voidCheckImportNamespaceFromFile("http://modelsolv.com/reprezen/schemas/clashwithaliased/dm2",
				"clashWithAliased-dm2.xsd");
		voidCheckImportNamespaceFromFile("http://modelsolv.com/reprezen/schemas/clashwithlocal/dm",
				"clashWithLocal-dm.xsd");
		voidCheckImportNamespaceFromFile("http://modelsolv.com/reprezen/schemas/clashwithimported/dm3",
				"clashWithImported-dm3.xsd");
	}

	public void voidCheckImportNamespaceFromFile(String namespace, String fileName) throws Exception {
		NodeList list = (NodeList) fixture.query("/schema/import[@namespace='" + namespace + "']",
				XPathConstants.NODESET);
		assertThat(list.getLength(), equalTo(1));
		assertThat(list.item(0), hasValue("schemaLocation", fileName));
	}
}
