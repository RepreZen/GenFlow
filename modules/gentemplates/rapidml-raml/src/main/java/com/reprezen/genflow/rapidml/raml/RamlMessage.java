/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.raml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.reprezen.rapidml.Example;
import com.reprezen.rapidml.HttpMessageParameterLocation;
import com.reprezen.rapidml.MediaType;
import com.reprezen.rapidml.MessageParameter;
import com.reprezen.rapidml.Method;
import com.reprezen.rapidml.ServiceDataResource;
import com.reprezen.rapidml.TypedMessage;

public class RamlMessage extends RamlObject {

	private Logger logger = Logger.getGlobal();
	private final ObjectMapper mapper = new ObjectMapper();

	private final TypedMessage rapidMessage;

	public RamlMessage(TypedMessage rapidMessage) {
		this.rapidMessage = rapidMessage;
	}

	public static boolean mediaTypeIsJSON(String mediaTypeName) {
		return mediaTypeName.contains("application/json");
	}

	public static boolean mediaTypeIsXML(String mediaTypeName) {
		return mediaTypeName.contains("application/xml");
	}

	public List<RamlParameter> getHeaderParameters() {
		return getMessageParameters(HttpMessageParameterLocation.HEADER);
	}

	protected List<RamlParameter> getMessageParameters(HttpMessageParameterLocation locationObject) {
		List<MessageParameter> allParameters = rapidMessage.getParameters();
		List<RamlParameter> ramlParams = new ArrayList<RamlParameter>();
		for (MessageParameter messageParameter : allParameters) {
			String locationString = messageParameter.getHttpLocation().getName();
			if (locationObject.toString().equals(locationString)) {
				ramlParams.add(new RamlParameter(messageParameter));
			}
		}
		return ramlParams;
	}

	public List<String> getMediaTypes() {
		ArrayList<String> mediaTypes = new ArrayList<String>();
		for (MediaType mediaType : rapidMessage.getMediaTypes()) {
			mediaTypes.add(mediaType.getName());
		}
		return mediaTypes;
	}

	public boolean hasExample() {
		return !rapidMessage.getAllExamples().isEmpty();
	}

	/**
	 * Returns the active example for this message. Relies on logic in the metamodel
	 * classes which ensures that, in the case where there are multiple examples
	 * available, perhaps from a number of levels, that the same example is used
	 * that would be used by the Mock Service.
	 * 
	 * @return an example if one is available for this message or null.
	 */
	public String getExample() {
		if (!hasExample()) {
			return null;
		}
		List<Example> examples = rapidMessage.getAllExamples();
		return examples.get(0).getBody().replace('\t', ' ');
	}

	/**
	 * Returns a map of schema definitions that are associated with this message
	 * keyed by media type. At the moment only JSON schema is implemented.
	 * 
	 * @return A map of schema definitions keyed by media type.
	 */
	public HashMap<String, String> getSchemas() {
		JsonSchemaForRamlGenerator jsonSchemaGenerator = new JsonSchemaForRamlGenerator();
		List<String> supportedMediaTypes = getMediaTypes();
		HashMap<String, String> messageSchemas = new HashMap<String, String>();
		for (String mediaType : supportedMediaTypes) {
			if (RamlMessage.mediaTypeIsJSON(mediaType)) {
				// System.out.println("JSON media type detected: " +mediaType +". Looking for
				// schema...");
				ObjectNode schemaNode = mapper.createObjectNode();
				if (rapidMessage.getResourceType() != null) {
					jsonSchemaGenerator.buildDefinitionNode(schemaNode,
							(ServiceDataResource) rapidMessage.getResourceType());
				} else if (rapidMessage.getDataType() != null) {
					jsonSchemaGenerator.buildDefinitionNode(schemaNode, rapidMessage.getDataType());
					// System.out.println("Schema found for data type "
					// +rapidMessage.getDataType().getName());
				}
				try {
					String schema = getNodeBody(schemaNode);
					messageSchemas.put(mediaType, schema);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (RamlMessage.mediaTypeIsXML(mediaType)) {
				try {
					logger.warning("XML media type (" + mediaType + ") detected in the method "
							+ ((Method) rapidMessage.eContainer()).getName() + ", it will be ignored.");
				} catch (Throwable e) {
				}
				// TODO: implement this. For now do nothing, meaning no schema for XML messages.
				// System.out.println("XML media type detected: " +mediaType);
				// messageSchemas.put(mediaType,
				// "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><!--NOT IMPLEMENTED
				// YET! --></xs:schema>");
			}

		}
		return messageSchemas;
	}

	public String getNodeBody(ObjectNode node) throws JsonProcessingException {
		String nodeText = mapper.writeValueAsString(node);
		// trim leading {and trailing }
		nodeText = nodeText.substring(1, nodeText.length() - 1);
		return nodeText;
	}

	public void attachLogger(Logger logger) {
		this.logger = logger;
	}
}
