/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.util.Monitor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.FakeGenTemplateContext;
import com.reprezen.genflow.api.zenmodel.ZenModelSource;
import com.reprezen.genflow.rapidml.diagram.xtend.XGenerateJSON;
import com.reprezen.rapidml.ZenModel;
import com.reprezen.rapidml.implicit.ZenModelNormalizer;

/**
 * A test fixture for JSON create by d3.js visualization generator.
 * 
 * @author Tatiana Fesenko <tatiana.fesenko@modelsolv.com>
 * 
 */
public class JsonGeneratorTestFixture extends D3GeneratorTestFixture {

	public static final String TYPE = "type";
	public static final String NAME = "name";
	public static final String STATUS_CODE = "statusCode";
	public static final String STATUS_CODE_GROUP = "statusCodeGroup";
	public static final String RESOURCE_TYPE = "resource_type";
	public static final String OBJECTTYPE = "objecttype";
	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String REFERENCE_RESOURCE_ID = "referencedResourceId";
	public static final String URI_FRAGMENT = "uriFragment";
	public static final String PROPERTY_ID = "propertyId";

	public JsonGeneratorTestFixture() {
		super("json");
	}

	@Override
	protected Map<String, String> doGenerate(ZenModel zenModel, File dir, Monitor progressMonitor) throws IOException {
		new ZenModelNormalizer().normalize(zenModel);
		XGenerateJSON generator = new XGenerateJSON();
		FakeGenTemplateContext context = new FakeGenTemplateContext();
		context.setPrimarySource(new ZenModelSource(new File("test.rapid")) {
			@Override
			public ZenModel load(File inFile) throws GenerationException {
				return zenModel;
			}
		});
		generator.init(context);
		String result = generator.generateJSON(zenModel);
		return Collections.singletonMap("test.json", result); //$NON-NLS-1$
	}

	public void assertObjectResourceHasName(int indexOfResource, String expectedName) throws Exception {
		JsonNode testedResource = getResource(indexOfResource);
		assertThat(testedResource, notNullValue());
		assertThat(testedResource.get(OBJECTTYPE).asText(), equalTo("ObjectResource"));
		assertThat(testedResource.get(NAME).asText(), equalTo(expectedName));
		assertThat(testedResource.get("id").asText(), equalTo(expectedName));
		// test URI
		JsonNode uri = getURI(testedResource);
		assertThat(uri, notNullValue());
	}

	public void assertCollectionResourceHasName(int indexOfResource, String expectedName) throws Exception {
		JsonNode testedResource = getResource(indexOfResource);
		assertThat(testedResource, notNullValue());
		assertThat(testedResource.get(NAME).asText(), equalTo(expectedName));
		assertThat(testedResource.get(OBJECTTYPE).asText(), equalTo("CollectionResource"));
		assertThat(testedResource.get("id").asText(), equalTo(expectedName));
		// test URI
		JsonNode uri = getURI(testedResource);
		assertThat(uri, notNullValue());
	}

	public JsonNode getRoot() throws JsonParseException, IOException {
		// JsonFactory factory = new JsonFactory();
		// JsonParser parser = factory.createJsonParser(generatedFile);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(generatedFile);
		return node;
	}

	public ArrayNode getResources() throws JsonParseException, IOException {
		return ((ArrayNode) getRoot().get("ResourceAPI").get("resources"));
	}

	public JsonNode getResource(int indexOfResource) throws JsonParseException, IOException {
		return ((ArrayNode) getRoot().get("ResourceAPI").get("resources")).get(indexOfResource);
	}

	public JsonNode getDataType(JsonNode resource) throws JsonParseException, IOException {
		return resource.get("dataType");
	}

	public JsonNode getResourceDataType(int indexOfResource) throws JsonParseException, IOException {
		return getDataType(getResource(indexOfResource));
	}

	public JsonNode getURI(JsonNode resource) throws JsonParseException, IOException {
		return resource.get("URI");
	}

	public JsonNode getResourceURI(int indexOfResource) throws JsonParseException, IOException {
		return getURI(getResource(indexOfResource));
	}

	public ArrayNode getMethods(JsonNode resource) throws JsonParseException, IOException {
		return (ArrayNode) resource.get("methods");
	}

	public ArrayNode getProperties(JsonNode dataType) throws JsonParseException, IOException {
		return (ArrayNode) dataType.get("properties");
	}

	public ArrayNode getResourceMethods(int indexOfResource) throws JsonParseException, IOException {
		return getMethods(getResource(indexOfResource));
	}

	public JsonNode getRequest(JsonNode method) throws JsonParseException, IOException {
		return method.get("request");
	}

	public ArrayNode getResponses(JsonNode method) throws JsonParseException, IOException {
		return (ArrayNode) method.get("responses");
	}

	public ArrayNode getReferenceTreatments(JsonNode dataType) throws JsonParseException, IOException {
		return (ArrayNode) dataType.get("referenceTreatments");
	}

	public JsonNode getResourceMethod(int indexOfResource, int indexOfMethod) throws JsonParseException, IOException {
		return getResourceMethods(indexOfResource).get(indexOfMethod);
	}
}
