package com.reprezen.genflow.rapidml.nodejs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.rapidml.nodejs.NodejsGenerator.Generator;
import com.reprezen.rapidml.DataModel;
import com.reprezen.rapidml.DataType;
import com.reprezen.rapidml.PrimitiveProperty;
import com.reprezen.rapidml.ReferenceProperty;
import com.reprezen.rapidml.Structure;
import com.reprezen.rapidml.ZenModel;

public class MetadataGenerator implements Generator {

	private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	private ZenModel model;

	public MetadataGenerator(ZenModel model) {
		this.model = model;
	}

	@Override
	public String generate() throws GenerationException {
		JsonNode metadata = createMetadata(model);
		try {
			return mapper.writeValueAsString(metadata);
		} catch (JsonProcessingException e) {
			throw new GenerationException("Failed to generate metadata file", e);
		}
	}

	private JsonNode createMetadata(ZenModel model) {
		ObjectNode root = newObjectNode();
		ObjectNode modelNode = root.putObject("models").putObject(model.getName());
		ObjectNode dmsNode = modelNode.putObject("dataModels");
		for (DataModel dm : model.getDataModels()) {
			dmsNode.set(dm.getName(), createDataModelMetadata(dm));
		}
		return root;
	}

	private JsonNode createDataModelMetadata(DataModel dm) {
		ObjectNode dmRoot = newObjectNode();
		ObjectNode structs = dmRoot.putObject("structures");
		for (DataType type : dm.getOwnedDataTypes()) {
			if (type instanceof Structure) {
				Structure struct = (Structure) type;
				structs.set(struct.getName(), createStructureMetadata(struct));
			}
			// TODO fill in enums object
			// TODO fill in simpleTypes object
		}
		return dmRoot;
	}

	private JsonNode createStructureMetadata(Structure struct) {
		ObjectNode root = newObjectNode();
		root.put("name", struct.getName());
		ObjectNode fields = root.putObject("fields");
		for (PrimitiveProperty prop : struct.getPrimitiveProperties()) {
			ObjectNode field = fields.putObject(prop.getName());
			field.put("name", prop.getName());
			field.put("type", prop.getPrimitiveType().getName());
			field.put("multi", prop.getMaxOccurs() < 0);
		}
		for (ReferenceProperty prop : struct.getReferenceProperties()) {
			ObjectNode field = fields.putObject(prop.getName());
			field.put("name", prop.getName());
			field.put("type", "ref");
			field.put("multi", prop.getMaxOccurs() < 0);
			field.put("refType", getRefTypeName(prop));
		}
		return root;
	}

	private String getRefTypeName(ReferenceProperty prop) {
		DataModel thisDataModel = (DataModel) prop.getContainingDataType().eContainer();
		ZenModel thisModel = (ZenModel) thisDataModel.eContainer();
		Structure refType = prop.getType();
		DataModel refDataModel = (DataModel) refType.eContainer();
		ZenModel refModel = (ZenModel) refDataModel.eContainer();
		if (thisDataModel == refDataModel) {
			return refType.getName();
		} else if (thisModel == refModel) {
			return String.format("%s.%s", refDataModel.getName(), refType.getName());
		} else {
			return String.format("%s.%s.%s", refModel.getName(), refDataModel.getName(), refType.getName());
		}
	}

	private static ObjectNode newObjectNode() {
		return JsonNodeFactory.instance.objectNode();
	}
}