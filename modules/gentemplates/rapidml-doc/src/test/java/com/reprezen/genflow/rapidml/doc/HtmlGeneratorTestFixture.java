/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Function;

/**
 * A test fixture for HTML create by documentation generator.
 * 
 * @author Tatiana Fesenko <tatiana.fesenko@modelsolv.com>
 * 
 */
public class HtmlGeneratorTestFixture extends DocGeneratorTestFixture {

	public static final String HREF = "href";
	public static final int RESOURCE_REFERENCELINK_DESCORATOR = 3;
	public static final int RESOURE_REFERENCELINK_PATH = 2;
	public static final int RESOURCE_FIELD_DOCUMENTATION = 2;
	public static final int RESOURCE_FIELD_TYPE = 1;
	public static final int RESOURCE_FIELD_NAME = 0;
	public static final int DATATYPE_FIELD_DOCUMENTATION = 2;
	public static final int DATATYPE_FIELD_TYPE = 1;
	public static final int DATATYPE_FIELD_NAME = 0;

	public HtmlGeneratorTestFixture() {
		super("html");
	}

	public Document getRoot() throws IOException {
		Document doc = Jsoup.parse(generatedFile, "UTF-8");
		return doc;
	}

	public Elements resource(String resourceName) throws Exception {
		Document doc = getRoot();
		// <a class="anchor" id="resourceType-IndexResource"></a>
		// <div class="panel panel-default"> ...
		// ResourceDefinition#name is EMF ID, so it's used in the URI fragment
		Elements resource = doc.select("[data-zenname=" + resourceName + "] + div");
		return resource;
	}

	public Elements datatype(String dataTypeName) throws Exception {
		Document doc = getRoot();
		// <a class="anchor" id="refProp-LineItem"></a>
		// <div class="panel panel-primary">..
		return doc.select("[data-zenname=" + dataTypeName + "] + div");
	}

	public static Element require(Elements resource) {
		assertThat(resource.size(), equalTo(1));
		return resource.first();
	}

	public static Elements resourceTable(Element resource, String tableName) {
		// <h4>$table name</h4>
		// <table class="table table-condensed">
		Elements table = resource.select("h4:contains(" + tableName + ") + table");
		return table;
	}

	public static Elements datatypesTable(Element datatype, String tableName) {
		// <div class="panel-body">
		// <h4>$table name</h4>
		// <table class="table table-condensed">
		Element div = require(datatype.select("div.panel-body"));
		Elements table = div.select("h4:contains(" + tableName + ") + table");
		return table;
	}

	public static Elements getResourceFieldsTable(Element resource) {
		return resourceTable(resource, "Data Properties");
	}

	public static Element tableRow(Element tableElements, int index) {
		Elements tableRowElements = tableElements.select(":root > tbody > tr");
		return tableRowElements.get(index + 1);
	}

	public static Element cell(Element table, int index) {
		return table.select("td").get(index);
	}

	public static String cellText(Element table, int index) {
		return getSimpleTextValue(cell(table, index));
	}

	protected static Element requireA(Element element) {
		return element.select("a").first();
	}

	public static Element datatypeProperty(Element datatype, int index) {
		Element tableElements = require(datatypesTable(datatype, "Properties"));
		return tableRow(tableElements, index);
	}

	public static Element resourceDataProperties(Element resource, int index) {
		Element tableElements = require(resourceTable(resource, "Data Properties"));
		return tableRow(tableElements, index);
	}

	public static Element method(Element resource, String methodName) {
		// <a class="anchor" id="method-MyObject-{methodName}-0"></a>
		// <ul class="list-group">
		// <li class="list-group-item">
		// <h4>Data Properties</h4>
		// <table class="table table-condensed">
		Elements method = resource.select("a.anchor[id*=" + methodName + "] ~ ul >li");
		return method.first();
	}

	public static Element methodRequest(Element resource, String methodName) {
		// <a class="anchor" id="method-MyObject-{methodName}-0"></a>
		// <ul class="list-group">
		// <li class="list-group-item"> Request
		return resource.select("a.anchor[id*=" + methodName + "] ~ ul > li:contains(Request)").get(0);
	}

	public static Element methodResponse(Element resource, String methodName, int responseIdx) {
		// <a class="anchor" id="method-MyObject-{methodName}-0"></a>
		// <ul class="list-group">
		// <li class="list-group-item"> Response
		Elements response = resource.select("a.anchor[id*=" + methodName + "] ~ ul >li:contains(Response)");
		return response.get(responseIdx);
	}

	public static Element messageMediaTypes(Element message) {
		// <h4>Properties</h4>
		// <table class="table table-condensed">
		// <tr><th>Media Types</th><td></td>
		Elements row = message.select("h4:contains(Properties) + table th:contains(Media Types) ~ td");
		return require(row);
	}

	public static String getResourceFieldPrimitiveTypeValue(Element resourceField) {
		Element element = cell(resourceField, RESOURCE_FIELD_TYPE);
		return getSimpleTextValue(element);
	}

	public static String getResourceFieldReferenceTypeName(Element resourceField) {
		// <a href="#refProp-User"> User</a>
		Element element = cell(resourceField, RESOURCE_FIELD_TYPE);
		Element a = requireA(element);
		return getSimpleTextValue(a);
	}

	public static String getResourceFieldReferenceTypeLink(Element resourceField) {
		// <a href="#refProp-User"> User</a>
		Element element = cell(resourceField, RESOURCE_FIELD_TYPE);
		Element a = requireA(element);
		return a.attr(HREF);
	}

	protected static String getSimpleTextValue(Element element) {
		String value = element.text();
		return value.trim();
	}

	private static Function<Element, String> elementToText() {
		return new Function<Element, String>() {

			@Override
			public String apply(Element element) {
				return getSimpleTextValue(element);
			}
		};
	}
}
