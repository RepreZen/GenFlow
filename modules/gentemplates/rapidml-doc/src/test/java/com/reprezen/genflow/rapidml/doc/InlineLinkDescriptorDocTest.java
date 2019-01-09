/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc;

import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.RESOURCE_FIELD_NAME;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.cellText;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.getResourceFieldPrimitiveTypeValue;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.getResourceFieldReferenceTypeLink;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.getResourceFieldReferenceTypeName;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.require;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.resourceDataProperties;
import static com.reprezen.genflow.rapidml.doc.LinkHelper.sanitizeLink;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.jsoup.nodes.Element;
import org.junit.Rule;
import org.junit.Test;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("realization/Customer_inlineLinkDescriptor_simple.rapid")
public class InlineLinkDescriptorDocTest {

	@Rule
	public HtmlGeneratorTestFixture fixture = new HtmlGeneratorTestFixture();

	@Test
	public void testIsValidHtml() throws Exception {
		fixture.getRoot();
	}

	@Test
	public void testCustomerResource() throws Exception {
		require(fixture.resource("CustomerObject"));
	}

	@Test
	public void testInlineLinkDescriptor() throws Exception {
		Element resource = require(fixture.resource("CustomerObject"));
		int productIndex = 3;
		{
			Element productRefLink = resourceDataProperties(resource, productIndex);
			String name = cellText(productRefLink, RESOURCE_FIELD_NAME);
			assertThat(name, equalTo("Product"));
			String typeName = getResourceFieldReferenceTypeName(productRefLink);
			assertThat(typeName, equalTo("ProductObject"));
			String typeLink = getResourceFieldReferenceTypeLink(productRefLink);
			assertThat(typeLink, endsWith(sanitizeLink("//@resourceAPIs.0/@ownedResourceDefinitions.1")));
		}
		{
			Element productId = resourceDataProperties(resource, productIndex + 1);
			String name = cellText(productId, RESOURCE_FIELD_NAME);
			assertThat(name, equalTo("ProductID"));
			String type = getResourceFieldPrimitiveTypeValue(productId);
			assertThat(type, equalTo("string"));
		}
		{
			Element productName = resourceDataProperties(resource, productIndex + 2);
			String name = cellText(productName, RESOURCE_FIELD_NAME);
			assertThat(name, equalTo("ProductName"));
			String type = getResourceFieldPrimitiveTypeValue(productName);
			assertThat(type, equalTo("string"));
		}
	}

}
