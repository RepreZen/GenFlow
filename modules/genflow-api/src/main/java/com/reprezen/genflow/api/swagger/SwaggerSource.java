/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.swagger;

import static com.reprezen.genflow.api.normal.openapi.ObjectType.SWAGGER_MODEL_VERSION;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.loadability.LoadabilityTester;
import com.reprezen.genflow.api.normal.openapi.OpenApiNormalizer;
import com.reprezen.genflow.api.normal.openapi.Option;
import com.reprezen.genflow.api.normal.openapi.Options;
import com.reprezen.genflow.api.openapi.OpenApiLoadabilityTester;
import com.reprezen.genflow.api.openapi.OpenApiSource.OpenApiType;
import com.reprezen.genflow.api.source.AbstractSource;
import com.reprezen.genflow.api.source.ILocator;

import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

public class SwaggerSource extends AbstractSource<Swagger> {

	public SwaggerSource() {
		super();
	}

	public SwaggerSource(File inputFile) {
		super(inputFile);
	}

	@Override
	public String getLabel() {
		return "Swagger";
	}

	@Override
	public Swagger load(File inFile) throws GenerationException {
		Swagger swagger;
		try {
			swagger = new OpenApiNormalizer(getNormalizerOptions()).of(inFile).normalizeToSwagger(inFile);
		} catch (Exception e) {
			throw new GenerationException("Failed to parse Swagger spec: " + inFile, e);
		}
		if (swagger != null) {
			return swagger;
		} else {
			throw new GenerationException("File does not contain a valid Swagger spec: " + inFile);
		}
	}

	protected Option[] getNormalizerOptionArray() throws GenerationException {
		return Option.CODEGEN_DEFAULT_OPTIONS;
	}

	private Options getNormalizerOptions() throws GenerationException {
		return new Options(SWAGGER_MODEL_VERSION, getNormalizerOptionArray());
	}

	@Override
	public Class<?> getValueType() throws GenerationException {
		return Swagger.class;
	}

	@Override
	public ILocator<Swagger> getLocator(Swagger swagger) {
		return new SwaggerLocator(swagger);
	}

	enum Extractable {
		PATH(Path.class, model -> model.getPaths().values()), //
		NAMED_PATH(NamedPath.class, model -> addNames(model.getPaths(), NamedPath.class)), //
		SCHEMA(Model.class, model -> model.getDefinitions().values()), //
		NAMED_SCHEMA(NamedModel.class, model -> addNames(model.getDefinitions(), NamedModel.class)), //
		RESPONSE(Response.class, model -> model.getResponses().values()), //
		NAMED_RESPONSE(NamedResponse.class, model -> addNames(model.getResponses(), NamedResponse.class)), //
		PARAMETER(Parameter.class, model -> model.getParameters().values()), //
		NAMED_PARAMETER(NamedParameter.class, model -> addNames(model.getParameters(), NamedParameter.class));

		private final Class<?> partClass;
		private final Function<Swagger, Collection<?>> fn;

		Extractable(Class<?> partClass, Function<Swagger, Collection<?>> fn) {
			this.partClass = partClass;
			this.fn = fn;
		}

		@SuppressWarnings("unchecked")
		private static <T, NE extends NamedExtractable<T>> Collection<NE> addNames(Map<String, T> values,
				Class<NE> partClass) {
			return (Collection<NE>) values.entrySet().stream()
					.map((Entry<String, T> entry) -> instantiate(entry, partClass)) //
					.collect(Collectors.toList());
		}

		private static <T, NE extends NamedExtractable<T>> T instantiate(Entry<String, T> entry, Class<NE> partClass) {
			try {
				Constructor<?> cons = partClass.getDeclaredConstructor(Entry.class);
				@SuppressWarnings("unchecked")
				T result = (T) cons.newInstance(entry);
				return result;
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				return null;
			}
		}
	}

	@Override
	public Iterable<Object> extractByNonSourceType(Swagger model, Class<?> itemClass) throws GenerationException {
		for (Extractable part : Extractable.values()) {
			if (itemClass == part.partClass) {
				return Lists.newArrayList(part.fn.apply(model));
			}
		}
		return super.extractByNonSourceType(model, itemClass);
	}

	@Override
	public LoadabilityTester getLoadabilityTester() {
		return SwaggerLoadabilityTester.getInstance();
	}

	public static LoadabilityTester loadabilityTester() {
		return SwaggerLoadabilityTester.getInstance();
	}

	public static boolean canLoad(File file) {
		return SwaggerLoadabilityTester.getInstance().canLoad(file);
	}

	public static boolean canLoad(File file, int diligence) {
		return SwaggerLoadabilityTester.getInstance().canLoad(file, diligence);
	}

	public static class SwaggerLoadabilityTester extends OpenApiLoadabilityTester {

		public SwaggerLoadabilityTester() {
			super(OpenApiType.SWAGGERv2);
		}

		private static SwaggerLoadabilityTester instance;

		public static SwaggerLoadabilityTester getInstance() {
			if (instance == null) {
				instance = new SwaggerLoadabilityTester();
			}
			return instance;
		}

		@Override
		protected Loadability getContentLoadability(File file, int diligence, String content)
				throws JsonProcessingException, IOException {
			if (diligence <= LoadabilityTester.PARTIAL_LOAD_DILIGENCE) {
				JsonNode tree = getJsonTree(content);
				if (tree.path("swagger").asText().equals("2.0")) {
					return Loadability.loadable();
				} else {
					return Loadability.notLoadable("File does not contain a valid Swagger spec");
				}
			} else {
				// Swagger parser writes messages to stdout and exception stack traces to
				// stderr. We definitely
				// don't
				// want them here, so we need to silence them.
				PrintStream out = System.out;
				PrintStream err = System.err;
				try (OutputStream output = ByteStreams.nullOutputStream()) {
					System.setOut(new PrintStream(output));
					System.setErr(new PrintStream(output));
					new SwaggerSource().load(file);
					return Loadability.loadable();
				} catch (GenerationException | IOException e) {
					return Loadability.notLoadable(
							"File [" + file.toString() + "] does not contain a valid Swagger spec: " + e.getMessage());
				} finally {
					System.setOut(out);
					System.setErr(err);
				}
			}
		}

	}

	public static class SwaggerSource_MinimalNormalizerOptions extends SwaggerSource {

		@Override
		protected Option[] getNormalizerOptionArray() {
			return Option.MINIMAL_OPTIONS;
		}
	}

	public static class SwaggerSource_DocNormalizerOptions extends SwaggerSource {

		@Override
		protected Option[] getNormalizerOptionArray() {
			return Option.DOC_DEFAULT_OPTIONS;
		}
	}

	public static class NamedExtractable<T> {
		private String name;
		private T value;

		public NamedExtractable(String name, T value) {
			this.name = name;
			this.value = value;
		}

		public NamedExtractable(Entry<String, T> entry) {
			this(entry.getKey(), entry.getValue());
		}

		public String getName() {
			return name;
		}

		public T getValue() {
			return value;
		}
	}

	public static class NamedPath extends NamedExtractable<Path> {

		public NamedPath(Entry<String, Path> entry) {
			super(entry);
		}

		public NamedPath(String name, Path value) {
			super(name, value);
		}
	}

	public static class NamedModel extends NamedExtractable<Model> {

		public NamedModel(Entry<String, Model> entry) {
			super(entry);
		}

		public NamedModel(String name, Model value) {
			super(name, value);
		}
	}

	public static class NamedResponse extends NamedExtractable<Response> {

		public NamedResponse(Entry<String, Response> entry) {
			super(entry);
		}

		public NamedResponse(String name, Response value) {
			super(name, value);
		}
	}

	public static class NamedParameter extends NamedExtractable<Parameter> {

		public NamedParameter(Entry<String, Parameter> entry) {
			super(entry);
		}

		public NamedParameter(String name, Parameter value) {
			super(name, value);
		}
	}
}
