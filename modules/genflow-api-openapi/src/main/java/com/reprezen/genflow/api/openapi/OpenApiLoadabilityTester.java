package com.reprezen.genflow.api.openapi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.reprezen.genflow.api.loadability.AbstractLoadabilityTester;
import com.reprezen.genflow.api.loadability.LoadabilityTester;
import com.reprezen.genflow.api.openapi.OpenApiSource.OpenApiType;

public class OpenApiLoadabilityTester extends AbstractLoadabilityTester {
	private static final List<String> openApiExtensions = Arrays.asList("json", "yaml");

	private static ObjectMapper jsonMapper = new ObjectMapper();
	private static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

	private final OpenApiType type;

	public OpenApiLoadabilityTester(OpenApiType type) {
		this.type = type;
	}

	private static OpenApiLoadabilityTester instance = new OpenApiLoadabilityTester(null);

	public static OpenApiLoadabilityTester getInstance() {
		return instance;
	}

	@Override
	public Loadability _getLoadability(File file, int diligence) {
		return getLoadability(file, diligence, type);
	}

	public Loadability getLoadability(File file, int diligence, OpenApiType type) {
		if (diligence <= LoadabilityTester.FILENAME_DILIGENCE) {
			String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
			if (openApiExtensions.contains(extension)) {
				return Loadability.loadable();
			} else {
				return Loadability.notLoadable("File is not named with a valid Swagger extension: " + file.toString());
			}
		} else {
			try {
				String content = new String(Files.readAllBytes(file.toPath()));
				OpenApiLoadabilityTester tester = type != null ? type.getLoadabilityTester() : this;
				return tester.getContentLoadability(file, diligence, content);
			} catch (Exception e) {
				return Loadability.notLoadable("File content could not be parsed");
			}
		}
	}

	public OpenApiType getLoadableType(File file, int diligence, String content) {
		for (OpenApiType type : OpenApiType.values()) {
			try {
				Loadability loadability = type.getLoadabilityTester().getContentLoadability(file, diligence, content);
				if (loadability.isLoadable()) {
					return type;
				}
			} catch (Exception e) {
			}
		}
		return null;
	}

	protected Loadability getContentLoadability(File file, int diligence, String content) throws Exception {
		// loadability tester when no type is provided
		if (type != null) {
			throw new IllegalStateException(
					"Type-specific OpenApiLoadabilityTester instance must override getContentLoadability method");
		}
		Loadability loadability = null;
		for (OpenApiType type : OpenApiType.values()) {
			loadability = type.getLoadabilityTester().getContentLoadability(file, diligence, content);
			if (loadability.isLoadable()) {
				break;
			}
		}
		return loadability;
	}

	private String lastContent = null;
	private JsonNode lastTree = null;

	// helper method optimized for repeated calls for the same JSON tree from
	// multiple subtypes
	// to support "switch-hitting" gentemplates
	protected JsonNode getJsonTree(String content) throws JsonProcessingException, IOException {
		if (!content.equals(lastContent)) {
			ObjectMapper mapper = content.trim().startsWith("{") ? jsonMapper : yamlMapper;
			JsonNode tree = mapper.readTree(content);
			lastContent = content;
			lastTree = tree;
		}
		return lastTree;
	}

	@Override
	public int getDefaultDiligence() {
		return LoadabilityTester.PARTIAL_LOAD_DILIGENCE;
	}
}