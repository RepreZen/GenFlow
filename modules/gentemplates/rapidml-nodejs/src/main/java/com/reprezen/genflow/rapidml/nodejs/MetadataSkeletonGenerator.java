package com.reprezen.genflow.rapidml.nodejs;

import com.fasterxml.jackson.core.JsonProcessingException;
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

public class MetadataSkeletonGenerator implements Generator {

	private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	private ZenModel model;
	private ObjectNode structureSkeleton;
	private ObjectNode primFieldSkeleton;
	private ObjectNode refFieldSkeleton;

	public MetadataSkeletonGenerator(ZenModel model, Object structSkeleton, Object propFieldSkeleton,
			Object refFieldSkeleton) {
		this.model = model;
		this.structureSkeleton = mapper.convertValue(structSkeleton, ObjectNode.class);
		this.primFieldSkeleton = mapper.convertValue(propFieldSkeleton, ObjectNode.class);
		this.refFieldSkeleton = mapper.convertValue(refFieldSkeleton, ObjectNode.class);
	}

	@Override
	public String generate() throws GenerationException {
		ObjectNode root = newObjectNode();
		ObjectNode dataModelRoot = root.putObject("models").putObject(model.getName()).putObject("dataModels");
		for (DataModel dm : model.getDataModels()) {
			ObjectNode structsRoot = dataModelRoot.putObject(dm.getName()).putObject("structures");
			for (DataType s : dm.getOwnedDataTypes()) {
				if (s instanceof Structure) {
					ObjectNode typeNode = structsRoot.putObject(s.getName());
					if (structureSkeleton != null) {
						typeNode.setAll(structureSkeleton);
					}
					ObjectNode fieldRoot = typeNode.putObject("fields");
					if (primFieldSkeleton != null) {
						for (PrimitiveProperty prop : ((Structure) s).getAllPrimitiveProperties()) {
							ObjectNode propNode = fieldRoot.putObject(prop.getName());
							if (primFieldSkeleton != null) {
								propNode.setAll(primFieldSkeleton);
							}
						}
					}
					if (refFieldSkeleton != null) {
						for (ReferenceProperty prop : ((Structure) s).getReferenceProperties()) {
							ObjectNode propNode = fieldRoot.putObject(prop.getName());
							if (refFieldSkeleton != null) {
								propNode.setAll(refFieldSkeleton);
							}
						}
					}
				}
			}
		}
		try {
			return mapper.writeValueAsString(root);
		} catch (JsonProcessingException e) {
			throw new GenerationException("Failed to generate metadata skeleton file", e);
		}

	}

	private ObjectNode newObjectNode() {
		return JsonNodeFactory.instance.objectNode();
	}
}
