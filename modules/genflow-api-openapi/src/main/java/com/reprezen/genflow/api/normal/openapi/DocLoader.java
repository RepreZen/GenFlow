package com.reprezen.genflow.api.normal.openapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.util.OpenApiIO;

/**
 * Cached loader of JSON documents that are needed in order to assemble either a
 * top-level model spec being normalized, or a component spec referenced by that
 * spec in a multi-file configuration.
 */
public class DocLoader {

	private final Integer modelVersion;
	private final Map<Reference, Content> docCache = Maps.newConcurrentMap();

	public DocLoader(Integer modelVersion) {
		this.modelVersion = modelVersion;
	}

	public Content load(URL url) {
		Reference ref = new Reference(url, modelVersion);
		Content doc = docCache.get(ref);
		if (doc == null) {
			try {
				String text = readFromUrl(url);
				JsonNode root = toJson(text, url, modelVersion);
				doc = Content.getContentItem(ref, root);
			} catch (LoadException e) {
				doc = Content.getUnloadableContentItem(ref, e.getMessage());
			}
		}
		docCache.put(ref, doc);
		return doc;

	}

	private String readFromUrl(URL url) {
		try {
			String content = "";
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
				content = reader.lines().collect(Collectors.joining("\n"));
			}
			return content;
		} catch (IOException e) {
			throw new LoadException(String.format("Could not read from URL '%s': %s", url, e.getMessage()));
		}
	}

	public static JsonNode toJson(String text, Integer modelVersion) {
		return toJson(text, null, modelVersion);
	}

	private static JsonNode toJson(String text, URL url, Integer modelVersion) {
		try {
			if (modelVersion == ObjectType.SWAGGER_MODEL_VERSION) {
				return OpenApiIO.loadSwaggerTree(text, false);
			} else if (modelVersion == ObjectType.OPENAPI3_MODEL_VERSION) {
				return OpenApiIO.loadOpenApi3Tree(text, false);
			} else {
				throw new LoadException("Invalid model version number: " + modelVersion);
			}
		} catch (GenerationException e) {
			String urlMsg = url != null ? " from URL '" + url.toString() + "'" : "";
			throw new LoadException(String.format("Invalid YAML or JSON%s: %s", urlMsg, e.getMessage()));
		}
	}

	private static class LoadException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public LoadException(String message) {
			super(message);
		}
	}
}
