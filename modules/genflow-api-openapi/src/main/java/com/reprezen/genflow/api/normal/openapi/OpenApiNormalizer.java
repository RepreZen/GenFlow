/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import static com.reprezen.genflow.api.normal.openapi.NormalizerDebug.debug;
import static com.reprezen.genflow.api.normal.openapi.NormalizerDebug.Option.FINAL_SPEC;
import static com.reprezen.genflow.api.normal.openapi.ObjectType.SWAGGER_MODEL_VERSION;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.kaizen.oasparser.OpenApi;
import com.reprezen.kaizen.oasparser.OpenApiParser;

import io.swagger.models.Swagger;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

/**
 * Apply a variety of "normalizing" transformations on a Swagger or OpenAPI3
 * spec, depending on specified options.
 * <p>
 * The most extensive transformation is the "inliner," which minimally
 * transforms a multi-file spec into a single JSON document.
 */
public class OpenApiNormalizer {

	private static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	private final Options options;
	private String spec;
	private JsonNode tree;

	public OpenApiNormalizer(Integer modelVersion, Option... options) {
		this(new Options(modelVersion, options));
	}

	public OpenApiNormalizer(Options options) {
		this.options = options;
		debug("Normalizer Options", options);
	}

	public OpenApiNormalizer of(String spec) {
		this.spec = spec;
		return this;
	}

	public OpenApiNormalizer of(File specFile) throws IOException {
		this.spec = new String(Files.readAllBytes(specFile.toPath()));
		return this;
	}

	public OpenApiNormalizer of(Swagger spec) throws JsonProcessingException {
		this.spec = Yaml.mapper().writeValueAsString(spec);
		return this;
	}

	public OpenApiNormalizer of(JsonNode tree) {
		this.tree = tree;
		return this;
	}

	public JsonNode normalizeToJson(URL resolutionBase) throws IOException, GenerationException {
		long start = System.nanoTime();
		try {
			resolutionBase = checkArgs(resolutionBase);
			debug("Normalizer Start", resolutionBase, Thread.currentThread().getStackTrace(),
					spec != null ? spec : tree);
			JsonNode normalized = normalizeToJsonInternal(resolutionBase);
			if (!options.isDeferExtensionDataRemoval()) {
				OpenApiMarkers.removeMarkers(normalized, options);
			}
			debug(FINAL_SPEC, "Normalized model spec", normalized);
			return normalized;
		} catch (IOException | GenerationException e) {
			debug(e);
			throw e;
		} finally {
			long elapsed = (System.nanoTime() - start) / (1000 * 1000); // milliseconds
			debug(String.format("Normalizer End [%d.%03dsec]", elapsed / 1000, (elapsed % 1000)));
		}
	}

	private JsonNode normalizeToJsonInternal(URL resolutionBase) throws GenerationException {
		if (spec != null) {
			return new OpenApiReferenceProcessor(options).of(spec).inline(resolutionBase);
		} else if (tree != null) {
			return new OpenApiReferenceProcessor(options).of(tree).inline(resolutionBase);
		} else {
			throw new IllegalStateException("Cannot call normalize() before supplying a model spec");
		}
	}

	public JsonNode normalizeToJson() throws IOException, GenerationException {
		return normalizeToJson((URL) null);
	}

	public JsonNode normalizeToJson(String resolutionBase) throws IOException, GenerationException {
		return normalizeToJson(new URL(resolutionBase));
	}

	public JsonNode normalizeToJson(File resolutionBase) throws IOException, GenerationException {
		return normalizeToJson(resolutionBase.toURI().toURL());
	}

	public Swagger normalizeToSwagger(URL resolutionBase) throws IOException, GenerationException {
		String baseUrlString = new Reference(resolutionBase, SWAGGER_MODEL_VERSION).getCanonicalFileRefString();
		options.resolveScope(baseUrlString);
		debug("Normalizer Start", resolutionBase, Thread.currentThread().getStackTrace(), spec != null ? spec : tree);
		long start = System.nanoTime();
		tree = normalizeToJsonInternal(resolutionBase);
		try {
			if (options.isFixXExamples()) {
				new SwaggerXExamplesFixer(tree).fixXExamples();
			}

			debug(NormalizerDebug.Option.PRE_PARSE_SPEC, tree);
			// parse the built tree to a Swagger object

			Swagger model = new SwaggerParser().read(tree, true);

			debug(NormalizerDebug.Option.POST_PARSE_SPEC, tree);

			if (options.isInstantiateNullTypes()) {
				SwaggerFiller.fill(model);
			}
			if (options.isFixMissingTypes()) {
				SwaggerTypeFixer.fixTypes(model);
			}
			if (options.isHoistParameters()) {
				SwaggerParameterHoister.hoist(model);
			}
			if (options.isHoistMediaTypes()) {
				SwaggerMediaTypesHoister.hoist(model);
			}
			if (options.isHoistSecurityRequirements()) {
				SwaggerSecurityRequirementHoister.hoist(model);
			}
			if (!options.isDeferExtensionDataRemoval()) {
				OpenApiMarkers.removeMarkers(model, options);
			}

			debug(FINAL_SPEC, "Normalized Swagger Spec", model);
			return model;
		} catch (Exception e) {
			debug(e);
			throw e;
		} finally {
			long elapsed = (System.nanoTime() - start) / (1000 * 1000); // milliseconds
			debug(String.format("Normalizer End [%d.%03dsec]", elapsed / 1000, (elapsed % 1000)));
		}
	}

	public Swagger normalizeToSwagger() throws IOException, GenerationException {
		return normalizeToSwagger((URL) null);
	}

	public Swagger normalizeToSwagger(String resolutionBase) throws IOException, GenerationException {
		return normalizeToSwagger(new URL(resolutionBase));
	}

	public Swagger normalizeToSwagger(File resolutionBase) throws IOException, GenerationException {
		return normalizeToSwagger(resolutionBase.toURI().toURL());
	}

	public OpenAPI normalizeToOpenAPI(URL resolutionBase) throws IOException, GenerationException {
		String baseUrlString = new Reference(resolutionBase, options.getModelVersion()).getCanonicalFileRefString();
		options.resolveScope(baseUrlString);
		debug("Normalizer Start", resolutionBase, Thread.currentThread().getStackTrace(), spec != null ? spec : tree);
		long start = System.nanoTime();
		tree = normalizeToJsonInternal(resolutionBase);

		// still need this even though we'll be invoking hte new parser, because htat
		// parser uses the legacy parser
		// when parsing v2 models, and it will still choke on non-string examples
		if (options.isFixXExamples()) {
			new SwaggerXExamplesFixer(tree).fixXExamples();
		}

		try {
			debug(NormalizerDebug.Option.PRE_PARSE_SPEC, tree);

			String spec = new ObjectMapper().writeValueAsString(tree);
			SwaggerParseResult result = new OpenAPIParser().readContents(spec, null, null);
			OpenAPI model = result.getOpenAPI();
			if (model != null) {
				debug(FINAL_SPEC, "Normalized OpenAPIv2 Spec", model);
				return model;
			} else {
				throw new IllegalArgumentException(
						"Failed to parse OpenAPI2 model spec: " + result.getMessages().toString());
			}
		} catch (Exception e) {
			debug(e);
			throw e;
		} finally {
			long elapsed = (System.nanoTime() - start) / (1000 * 1000); // milliseconds
			debug(String.format("Normalizer End [%d.%03dsec]", elapsed / 1000, (elapsed % 1000)));
		}
	}

	public OpenAPI normalizeToOpenAPI() throws IOException, GenerationException {
		return normalizeToOpenAPI((URL) null);
	}

	public OpenAPI normalizeToOpenAPI(String resolutionBase) throws IOException, GenerationException {
		return normalizeToOpenAPI(new URL(resolutionBase));
	}

	public OpenAPI normalizeToOpenAPI(File resolutionBase) throws IOException, GenerationException {
		return normalizeToOpenAPI(resolutionBase.toURI().toURL());
	}

	public OpenApi<?> normalizeToKaizen(URL resolutionBase) throws IOException, GenerationException {
		String baseUrlString = new Reference(resolutionBase, options.getModelVersion()).getCanonicalFileRefString();
		options.resolveScope(baseUrlString);
		debug("Normalizer Start", resolutionBase, Thread.currentThread().getStackTrace(), spec != null ? spec : tree);
		long start = System.nanoTime();
		tree = normalizeToJsonInternal(resolutionBase);

		try {
			debug(NormalizerDebug.Option.PRE_PARSE_SPEC, tree);

			String spec = mapper.writeValueAsString(tree);
			return new OpenApiParser().parse(spec, resolutionBase, true);
		} catch (Exception e) {
			debug(e);
			throw e;
		} finally {
			long elapsed = (System.nanoTime() - start) / (1000 * 1000); // milliseconds
			debug(String.format("Normalizer End [%d.%03dsec]", elapsed / 1000, (elapsed % 1000)));
		}
	}

	public OpenApi<?> normalizeToKaizen() throws IOException, GenerationException {
		return normalizeToKaizen((URL) null);
	}

	public OpenApi<?> normalizeToKaizen(String resolutionBase) throws IOException, GenerationException {
		return normalizeToKaizen(new URL(resolutionBase));
	}

	public OpenApi<?> normalizeToKaizen(File resolutionBase) throws IOException, GenerationException {
		return normalizeToKaizen(resolutionBase.toURI().toURL());
	}

	private URL checkArgs(URL resolutionBase) throws MalformedURLException {
		if (resolutionBase == null) {
			// null resolution base should never be null unless the provided
			// spec is free of relative references.
			// But just in case, we use a dummy URL that is extremely
			// unlikely to lead to successful resolution of
			// relative references URLs
			resolutionBase = new URL("file:/../n/o/r/e/s/o/l/u/t/i/o/n/b/a/s/e/p/r/o/v/i/d/e/d/");
		}
		String baseUrlString = new Reference(resolutionBase, SWAGGER_MODEL_VERSION).getCanonicalFileRefString();
		options.resolveScope(baseUrlString);
		if (spec == null && tree == null) {
			throw new IllegalStateException("Cannot call normalize() before supplying a model spec");
		}
		return resolutionBase;
	}

}
