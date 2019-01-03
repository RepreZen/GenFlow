/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc;

import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.cellText;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.datatypeProperty;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.getSimpleTextValue;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.require;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.resourceTable;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.tableRow;
import static com.reprezen.genflow.rapidml.doc.LinkHelper.sanitizeLink;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SuppressWarnings("nls")
@SampleRestFile("constraints/ResourceRealizationConstraints.rapid")
public class ConstraintsDocTest {

	@Rule
	public HtmlGeneratorTestFixture fixture = new HtmlGeneratorTestFixture();

	@Test
	public void testIsValidHtml() throws Exception {
		fixture.getRoot();
	}

	@Test
	public void testDataModelUserType() throws Exception {
		Element constraints = requireUserType("StringLenBaseType", "string");
		assertStringLenConstraint(constraints, -1, "from 0 to 100");

		Element constraints2 = requireUserType("StringComplexType", "string");
		assertStringLenConstraint(constraints2, -1, "from 0 to 50");
		assertStringRegexConstraint(constraints2, 0, "matching regex \"[A-Z]+\"");
	}

	@Test
	public void testDataModelStructure() throws Exception {
		Element structure = require(fixture.datatype("Structure"));

		Element field = datatypeProperty(structure, 0);
		Element constraints = require(field.select("table"));
		assertStringLenConstraint(constraints, 0, "from 1 to 40");
		assertStringRegexConstraint(constraints, 1, "matching regex \"[A-Za-z0-9\\w]*\"");

		Element field2 = datatypeProperty(structure, 4);
		Element constraints2 = require(field2.select("table"));
		assertValueRangeConstraint(constraints2, 0, "from -1.23 to 1.23");
	}

	@Test
	public void testResource() throws Exception {
		Element resource = require(fixture.resource("MyObject"));

		Element field = tableRow(resourceTable(resource, "Data Properties").first(), 0);
		Element constraints = require(field.select("table"));
		assertStringLenConstraint(constraints, 0, "from 5 to 10");
		assertStringRegexConstraint(constraints, 1, "matching regex \"[A-Za-z0-9\\w]*\"");
	}

	@Test
	public void testResourceMethod() throws Exception {
		Element resource = require(fixture.resource("MyObject"));

		Element method = HtmlGeneratorTestFixture.method(resource,
				sanitizeLink("//@resourceAPIs.0/@ownedResourceDefinitions.0/@methods.0"));
		assertNotNull(method);
		// Don't show the Data Properties table for messages with a resourceType
		Elements table = method.select("> h4:contains(Data Properties)");
		assertTrue(table.isEmpty());
	}

	private Element requireUserType(String type, String baseType) throws Exception {
		Element datatype = require(fixture.datatype(type));
		Element title = require(datatype.select("h3.panel-title"));
		Assert.assertEquals(type + " " + baseType, getSimpleTextValue(title));

		Element constraints = require(datatype.select("table"));
		return constraints;
	}

	private void assertStringLenConstraint(Element constraints, int idx, String value) {
		assertConstraint(constraints, idx, "String Length", value);
	}

	private void assertStringRegexConstraint(Element constraints, int idx, String value) {
		assertConstraint(constraints, idx, "Regular Expression", value);
	}

	private void assertValueRangeConstraint(Element constraints, int idx, String value) {
		assertConstraint(constraints, idx, "Value Range", value);
	}

	private void assertConstraint(Element constraints, int idx, String type, String value) {
		Element row = tableRow(constraints, idx);
		assertThat(cellText(row, 0), equalTo(type));
		assertThat(cellText(row, 1), equalTo(value));
	}
}
