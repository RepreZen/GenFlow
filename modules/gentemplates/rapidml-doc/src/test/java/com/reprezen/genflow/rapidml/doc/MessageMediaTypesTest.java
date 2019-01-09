/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc;

import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.getSimpleTextValue;
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.require;
import static com.reprezen.genflow.rapidml.doc.LinkHelper.sanitizeLink;

import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SuppressWarnings("nls")
@SampleRestFile("realization/Customer_messageMediaTypes.rapid")
public class MessageMediaTypesTest {

	@Rule
	public HtmlGeneratorTestFixture fixture = new HtmlGeneratorTestFixture();

	@Test
	public void testIsValidHtml() throws Exception {
		fixture.getRoot();
	}

	@Test
	public void testRequestMediaTypes() throws Exception {
		Element resource = require(fixture.resource("CustomerObject"));
		Element request = HtmlGeneratorTestFixture.methodRequest(resource,
				sanitizeLink("//@resourceAPIs.0/@ownedResourceDefinitions.0/@methods.0"));
		Element mediaTypes = HtmlGeneratorTestFixture.messageMediaTypes(request);
		Assert.assertEquals("application/json, text/plain", getSimpleTextValue(mediaTypes));
	}

	@Test
	public void testNoRequestMediaTypes() throws Exception {
		Element resource = require(fixture.resource("CustomerObject"));
		Element request = HtmlGeneratorTestFixture.methodRequest(resource,
				sanitizeLink("//@resourceAPIs.0/@ownedResourceDefinitions.0/@methods.1"));
		Assert.assertTrue(request.select("h4:contains(Properties)").isEmpty());
	}

	@Test
	public void testResponseMediaTypes() throws Exception {
		Element resource = require(fixture.resource("CustomerObject"));
		Element response = HtmlGeneratorTestFixture.methodResponse(resource,
				sanitizeLink("//@resourceAPIs.0/@ownedResourceDefinitions.0/@methods.1"), 0);
		Element mediaTypes = HtmlGeneratorTestFixture.messageMediaTypes(response);
		Assert.assertEquals("application/json, text/plain", getSimpleTextValue(mediaTypes));
	}

	@Test
	public void testNoResponseMediaTypes() throws Exception {
		Element resource = require(fixture.resource("CustomerObject"));
		Element response = HtmlGeneratorTestFixture.methodResponse(resource,
				sanitizeLink("//@resourceAPIs.0/@ownedResourceDefinitions.0/@methods.0"), 0);
		Assert.assertTrue(response.select("h4:contains(Properties)").isEmpty());
	}
}
