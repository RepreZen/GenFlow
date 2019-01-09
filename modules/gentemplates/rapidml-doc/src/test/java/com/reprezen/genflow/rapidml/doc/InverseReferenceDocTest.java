/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc;

import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.DATATYPE_FIELD_DOCUMENTATION;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.DATATYPE_FIELD_NAME;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.DATATYPE_FIELD_TYPE;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.HREF;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.cell;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.cellText;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.datatypeProperty;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.require;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.requireA;
import static com.reprezen.genflow.rapidml.doc.LinkHelper.sanitizeLink;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.jsoup.nodes.Element;
import org.junit.Rule;
import org.junit.Test;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("perspective/InverseReference_valid.rapid")
public class InverseReferenceDocTest {

	@Rule
	public HtmlGeneratorTestFixture fixture = new HtmlGeneratorTestFixture();

	@Test
	public void testIsValidHtml() throws Exception {
		fixture.getRoot();
	}

	@Test
	public void testSimpleInverseProperty() throws Exception {
		Element datatype = require(fixture.datatype("LineItem"));
		Element field = datatypeProperty(datatype, 1);
		String name = cellText(field, DATATYPE_FIELD_NAME);
		assertThat(name, equalTo("Product"));
		String type = cellText(field, DATATYPE_FIELD_TYPE);
		assertThat(type, equalTo("Product inverse of lineItem"));
		String doc = cellText(field, DATATYPE_FIELD_DOCUMENTATION);
		assertThat(doc, equalTo(""));
	}

	@Test
	public void testContainingInverseProperty() throws Exception {
		Element datatype = require(fixture.datatype("LineItem"));
		Element field = datatypeProperty(datatype, 2);
		String name = cellText(field, DATATYPE_FIELD_NAME);
		assertThat(name, equalTo("containmentProduct"));
		String type = cellText(field, DATATYPE_FIELD_TYPE);
		assertThat(type, equalTo("containing Product inverse of lineItem2"));
		Element typeCell = cell(field, DATATYPE_FIELD_TYPE);
		Element typeLink = requireA(typeCell);
		assertThat(typeLink.attr(HREF), endsWith(sanitizeLink("//@dataModels.0/@ownedDataTypes.3")));
		Element inverseLink = typeCell.select("a").get(1);
		assertThat(inverseLink.attr(HREF), endsWith(sanitizeLink("//@dataModels.0/@ownedDataTypes.3")));
		String doc = cellText(field, DATATYPE_FIELD_DOCUMENTATION);
		assertThat(doc, equalTo(""));
	}

	@Test
	public void testContainerInverseProperty() throws Exception {
		Element datatype = require(fixture.datatype("Product"));
		Element field = datatypeProperty(datatype, 1);
		String name = cellText(field, DATATYPE_FIELD_NAME);
		assertThat(name, equalTo("lineItem2"));
		String type = cellText(field, DATATYPE_FIELD_TYPE);
		assertThat(type, equalTo("container LineItem inverse of containmentProduct"));
		String doc = cellText(field, DATATYPE_FIELD_DOCUMENTATION);
		assertThat(doc, equalTo(""));
	}

}
