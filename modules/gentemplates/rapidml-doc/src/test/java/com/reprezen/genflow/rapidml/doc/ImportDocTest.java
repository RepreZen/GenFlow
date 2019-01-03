/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc;

import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.getResourceFieldReferenceTypeName;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.require;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.resourceDataProperties;

import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("import/JsonSchemaImportTest1.rapid")
public class ImportDocTest {

	@Rule
	public HtmlGeneratorTestFixture fixture = new HtmlGeneratorTestFixture();

	@Test
	public void testIsValidHtml() throws Exception {
		fixture.getRoot();
	}

	@Test
	public void importTest() throws Exception {
		checkTypeName("owEnum1fromModel1", "enum1");
		checkTypeName("owEnum3fromModel1", "enum3");
		checkTypeName("owEnum2fromModel2", "enum2");
		checkTypeName("owEnum3fromModel2", "enum3");
		checkTypeName("owEnum4fromModel2", "enum4");
		checkTypeName("owEnum4fromModel3", "enum4");
		checkTypeName("owEnum3fromModel12", "enum3");
	}

	private void checkTypeName(String resourceName, String typeName) throws Exception {
		Element resource = require(fixture.resource(resourceName));
		Element field = resourceDataProperties(resource, 0);
		Assert.assertEquals(typeName, getResourceFieldReferenceTypeName(field));
	}
}
