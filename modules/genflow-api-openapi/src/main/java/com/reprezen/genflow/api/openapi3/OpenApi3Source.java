/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi3;

import static com.reprezen.genflow.api.normal.openapi.ObjectType.OPENAPI3_MODEL_VERSION;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.loadability.LoadabilityTester;
import com.reprezen.genflow.api.normal.openapi.OpenApiNormalizer;
import com.reprezen.genflow.api.normal.openapi.Option;
import com.reprezen.genflow.api.openapi.OpenApiLoadabilityTester;
import com.reprezen.genflow.api.openapi.OpenApiSource.OpenApiType;
import com.reprezen.genflow.api.source.AbstractSource;
import com.reprezen.genflow.api.source.ILocator;
import com.reprezen.jsonoverlay.IModelPart;
import com.reprezen.kaizen.oasparser.OpenApi;
import com.reprezen.kaizen.oasparser.OpenApiParser;
import com.reprezen.kaizen.oasparser.model3.Callback;
import com.reprezen.kaizen.oasparser.model3.Example;
import com.reprezen.kaizen.oasparser.model3.Header;
import com.reprezen.kaizen.oasparser.model3.Link;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.model3.Parameter;
import com.reprezen.kaizen.oasparser.model3.Path;
import com.reprezen.kaizen.oasparser.model3.RequestBody;
import com.reprezen.kaizen.oasparser.model3.Response;
import com.reprezen.kaizen.oasparser.model3.Schema;
import com.reprezen.kaizen.oasparser.model3.SecurityScheme;

public class OpenApi3Source extends AbstractSource<OpenApi3> {
	private static OpenApi3LoadabilityTester loadabilityTester = OpenApi3LoadabilityTester.getInstance();

	@Override
	public OpenApi3 load(File inFile) throws GenerationException {
		OpenApi3 model;
		try {
			model = (OpenApi3) new OpenApiNormalizer(OPENAPI3_MODEL_VERSION, getNormalizerOptions()).of(inFile)
					.normalizeToKaizen(inFile);
		} catch (Exception e) {
			throw new GenerationException("Failed to parse Swagger spec: " + inFile, e);
		}
		if (model != null && model.isValid()) {
			return model;
		} else {
			throw new GenerationException("File does not contain a valid Swagger spec: " + inFile);
		}

	}

	protected Option[] getNormalizerOptions() {
		return Option.DO_NOT_NORMALIZE_OPTIONS;
	}

	@Override
	public ILocator<OpenApi3> getLocator(OpenApi3 model) {
		return new OpenApi3Locator(model);
	}

	enum Extractable {
		PATH(Path.class, model -> model.getPaths()), //
		SCHEMA(Schema.class, model -> model.getSchemas()), //
		RESPONSE(Response.class, model -> model.getResponses()), //
		PARAMETER(Parameter.class, model -> model.getParameters()), //
		EXAMPLE(Example.class, model -> model.getExamples()), //
		REQUEST_BODY(RequestBody.class, model -> model.getRequestBodies()), //
		HEADER(Header.class, model -> model.getHeaders()), //
		SECURITY_SCHEME(SecurityScheme.class, model -> model.getSecuritySchemes()), //
		LINK(Link.class, model -> model.getLinks()), //
		CALLBACK(Callback.class, model -> model.getCallbacks());

		private final Class<? extends IModelPart<OpenApi3, ?>> partClass;
		private final Function<OpenApi3, Map<String, ?>> mapFn;

		Extractable(Class<? extends IModelPart<OpenApi3, ?>> partClass, Function<OpenApi3, Map<String, ?>> mapFn) {
			this.partClass = partClass;
			this.mapFn = mapFn;
		}
	}

	@Override
	public Iterable<Object> extractByNonSourceType(OpenApi3 model, Class<?> itemClass) throws GenerationException {
		for (Extractable part : Extractable.values()) {
			if (itemClass == part.partClass) {
				return Lists.newArrayList(part.mapFn.apply(model).values());
			}
		}
		return super.extractByNonSourceType(model, itemClass);
	}

	@Override
	public String getLabel() {
		return "OpenAPI v3";
	}

	@Override
	public LoadabilityTester getLoadabilityTester() {
		return loadabilityTester;
	}

	public static LoadabilityTester LoadabilityTester() {
		return loadabilityTester;
	}

	public static boolean canLoad(File file) {
		return loadabilityTester.canLoad(file);
	}

	public static boolean canLoad(File file, int diligence) {
		return loadabilityTester.canLoad(file, diligence);
	}

	public static class OpenApi3LoadabilityTester extends OpenApiLoadabilityTester {
		public OpenApi3LoadabilityTester() {
			super(OpenApiType.OPENAPIv3);
		}

		private static OpenApi3LoadabilityTester instance;

		public static OpenApi3LoadabilityTester getInstance() {
			if (instance == null) {
				instance = new OpenApi3LoadabilityTester();
			}
			return instance;
		}

		@Override
		protected Loadability getContentLoadability(File file, int diligence, String content)
				throws JsonProcessingException, IOException {
			if (diligence <= LoadabilityTester.PARTIAL_LOAD_DILIGENCE) {
				// openapi: "3.0.0"
				JsonNode tree = getJsonTree(content);
				if (tree.path("openapi").asText().startsWith("3.0.")) {
					return Loadability.loadable();
				} else {
					return Loadability.notLoadable("File does not contain a valid OpenAPI v3 definition");
				}
			} else {
				OpenApi<?> definition = new OpenApiParser().parse(content, file.toURI().toURL());
				if (definition instanceof OpenApi3 && ((OpenApi3) definition).isValid()) {
					return Loadability.loadable();
				} else {
					return Loadability.notLoadable("File does not contain a valid OpenAPI v3 definition");
				}
			}
		}
	}

	public static class OpenApi3Source_DocNormalizerOptions extends OpenApi3Source {

		@Override
		protected Option[] getNormalizerOptions() {
			return Option.DOC_DEFAULT_OPTIONS;
		}
	}
}
