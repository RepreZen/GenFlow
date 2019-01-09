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
import static com.reprezen.genflow.rapidml.doc.HtmlGeneratorTestFixture.cell;
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
import static org.junit.Assert.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.junit.Rule;
import org.junit.Test;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("perspective/ReferenceEmbed.rapid")
public class ReferenceEmbedDocTest {

	@Rule
	public HtmlGeneratorTestFixture fixture = new HtmlGeneratorTestFixture();

	@Test
	public void testIsValidHtml() throws Exception {
		fixture.getRoot();
	}

	@Test
	public void testReferenceTreatments() throws Exception {
		Element resource = require(fixture.resource("ReferenceEmbedObject"));
		/*
		 * order DataType3 orderID string orderDate string lineItems LineItem product
		 * Product productID string productName string productPrice string image
		 * OrderImageOnlineResource
		 */
		int orderIndex = 2;
		Element order = resourceDataProperties(resource, orderIndex);
		testReferenceProperty(order, "order", "DataType3", 1, false);

		Element orderId = resourceDataProperties(resource, orderIndex + 1);
		testPrimitiveProperty(orderId, "orderID", "string");
		assertHasIndent(orderId, 1);

		Element orderDate = resourceDataProperties(resource, orderIndex + 2);
		testPrimitiveProperty(orderDate, "orderDate", "string");
		assertHasIndent(orderDate, 1);

		Element lineItems = resourceDataProperties(resource, orderIndex + 3);
		testReferenceProperty(lineItems, "lineItems", "LineItem", 2, false);
		assertHasIndent(lineItems, 1);

		int productIndex = orderIndex + 5;
		Element product = resourceDataProperties(resource, productIndex);
		testReferenceProperty(product, "product", "Product", 3, false);
		assertHasIndent(product, 2);

		Element productID = resourceDataProperties(resource, productIndex + 1);
		testPrimitiveProperty(productID, "productID", "string");
		assertHasIndent(productID, 3);

		Element productName = resourceDataProperties(resource, productIndex + 2);
		testPrimitiveProperty(productName, "productName", "string");
		assertHasIndent(productName, 3);

		Element productPrice = resourceDataProperties(resource, productIndex + 3);
		testPrimitiveProperty(productPrice, "productPrice", "string");
		assertHasIndent(productPrice, 3);

		Element image = resourceDataProperties(resource, productIndex + 4);
		testReferenceProperty(image, "image", "OrderImageOnlineObject", 1, true);
		assertHasIndent(image, 3);
	}

	protected void assertHasIndent(Element element, int expectedIndent) {
		Element cellValue = cell(element, RESOURCE_FIELD_NAME);
		// example of style - <td style="text-indent: 1em;">orderID</td>
		String style = cellValue.attr("style");

		Pattern pattern = Pattern.compile("text-indent: (\\d)em;");
		Matcher matcher = pattern.matcher(style);
		if (!matcher.find()) {
			fail();
		}
		String indent = matcher.group(1);
		assertThat(indent, equalTo(String.valueOf(expectedIndent)));
	}

	protected void testPrimitiveProperty(Element primitiveProperty, String propertyName, String primType) {
		String name = cellText(primitiveProperty, RESOURCE_FIELD_NAME);
		assertThat(name, equalTo(propertyName));
		String type = getResourceFieldPrimitiveTypeValue(primitiveProperty);
		assertThat(type, equalTo(primType));
	}

	private void testReferenceProperty(Element referenceProperty, String propertyName, String datatypeOrResourceName,
			int typeIndex, boolean linkNotResource) {
		String name = cellText(referenceProperty, RESOURCE_FIELD_NAME);
		assertThat(name, equalTo(propertyName));
		String typeName = getResourceFieldReferenceTypeName(referenceProperty);
		assertThat(typeName, equalTo(datatypeOrResourceName));
		String typeLink = getResourceFieldReferenceTypeLink(referenceProperty);
		String htmlPrefix = linkNotResource ? "//@resourceAPIs.0/@ownedResourceDefinitions."
				: "//@dataModels.0/@ownedDataTypes.";
		assertThat(typeLink, endsWith(sanitizeLink(htmlPrefix + typeIndex)));
	}

}
