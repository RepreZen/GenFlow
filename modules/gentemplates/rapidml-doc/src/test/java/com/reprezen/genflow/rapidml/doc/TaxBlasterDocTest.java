/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc;

import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.RESOURCE_FIELD_DOCUMENTATION;
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

@SampleRestFile("TaxBlaster.rapid")
public class TaxBlasterDocTest {

	@Rule
	public HtmlGeneratorTestFixture fixture = new HtmlGeneratorTestFixture();

	@Test
	public void testIsValidHtml() throws Exception {
		fixture.getRoot();
	}

	@Test
	public void testIndexResource() throws Exception {
	}

	@Test
	public void testTaxFilingsResource() throws Exception {
		require(fixture.resource("TaxFilingCollection"));
	}

	@Test
	public void testCollectionResourceLinksProperty() throws Exception {
		Element resource = require(fixture.resource("TaxFilingCollection"));
		// filingID string A unique, system-assigned identifier for the tax filing.
		Element field = resourceDataProperties(resource, -1); // -1 because we have not header row
		String name = cellText(field, RESOURCE_FIELD_NAME);
		assertThat(name, equalTo("TaxFiling"));
		String type = getResourceFieldReferenceTypeName(field);
		assertThat(type, equalTo("TaxFilingObject*"));
	}

	@Test
	public void testResourcePrimitiveProperty() throws Exception {
		Element resource = require(fixture.resource("TaxFilingObject"));
		// filingID string A unique, system-assigned identifier for the tax filing.
		Element field = resourceDataProperties(resource, 0);
		String name = cellText(field, RESOURCE_FIELD_NAME);
		assertThat(name, equalTo("filingID"));
		String type = getResourceFieldPrimitiveTypeValue(field);
		assertThat(type, equalTo("string"));
		String doc = cellText(field, RESOURCE_FIELD_DOCUMENTATION);
		assertThat(doc, equalTo("A unique, system-assigned identifier for the tax filing."));
	}

	@Test
	public void testResourceReferenceProperty() throws Exception {
		Element resource = require(fixture.resource("TaxFilingObject"));
		// user User Reference to the user who owns this filing.
		Element field = resourceDataProperties(resource, 7);
		String name = cellText(field, RESOURCE_FIELD_NAME);
		assertThat(name, equalTo("taxpayer"));
		String typeName = getResourceFieldReferenceTypeName(field);
		assertThat(typeName, equalTo("PersonObject"));
		String typeLink = getResourceFieldReferenceTypeLink(field);
		assertThat(typeLink, endsWith(sanitizeLink("//@resourceAPIs.0/@ownedResourceDefinitions.4")));
		String doc = cellText(field, RESOURCE_FIELD_DOCUMENTATION);
		assertThat(doc, equalTo("Reference to the person who owns this filing."));
	}

	@Test
	public void testUsersResource() throws Exception {
		require(fixture.resource("PersonCollection"));
	}

	@Test
	public void testTaxFilingResource() throws Exception {
		require(fixture.resource("TaxFilingObject"));
	}

	@Test
	public void testUserResource() throws Exception {
		require(fixture.resource("PersonObject"));
	}

}
