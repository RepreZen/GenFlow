/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc;

import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.require;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.resourceDataProperties;

import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SuppressWarnings("nls")
@SampleRestFile("examples/cardinalityoverrides/TaxBlaster.rapid")
public class CardinalityDocTest {

	@Rule
	public HtmlGeneratorTestFixture fixture = new HtmlGeneratorTestFixture();

	@Test
	public void testIsValidHtml() throws Exception {
		fixture.getRoot();
	}

	@Test
	public void cardinalityTest() throws Exception {
		checkCardinality("PersonCollection", 8, "TaxFilingCollection");
		checkCardinality("PersonCollection", 9, "Address+");
	}

	private void checkCardinality(String resourceName, int fieldIdx, String typeWithCardinality) throws Exception {
		Element resource = require(fixture.resource(resourceName));
		Element field = resourceDataProperties(resource, fieldIdx);
		Assert.assertEquals(typeWithCardinality, HtmlGeneratorTestFixture.getResourceFieldPrimitiveTypeValue(field));
	}
}
