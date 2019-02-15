/*******************************************************************************
 * Copyright © 2013, 2019 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen3;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.openapi.OpenApiDocument;
import com.reprezen.genflow.api.openapi.OpenApiGenTemplate;
import com.reprezen.genflow.api.swagger.SwaggerSource.SwaggerSource_MinimalNormalizerOptions;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.template.GenTemplateProperty.StandardProperties;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.common.codegen.GenModuleWrapper;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Info;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.ClientOpts;
import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.v3.oas.models.OpenAPI;

public abstract class Scg3CodegenGenTemplateBase extends OpenApiGenTemplate {

	public static final String SWAGGER_CODEGEN_SYSTEM_PROPERTIES = "swaggerCodegenSystemProperties";
	public static final String SWAGGER_CODEGEN_CONFIG = "swaggerCodegenConfig";
	public static final List<String> SPECIAL_PARAMS = Arrays.asList(SWAGGER_CODEGEN_CONFIG,
			SWAGGER_CODEGEN_SYSTEM_PROPERTIES);

	protected final GenModuleWrapper<CodegenConfig> wrapper;
	private Info info;

	public Scg3CodegenGenTemplateBase(GenModuleWrapper<CodegenConfig> wrapper, Info info) {
		this.wrapper = wrapper;
		this.info = info;
	}

	@Override
	public IGenTemplate newInstance() throws GenerationException {
		return new Scg3CodegenGenTemplate(wrapper, info);
	}

	@Override
	protected StaticGenerator<OpenApiDocument> getStaticGenerator() {
		return new Generator(this, context, wrapper);
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swagger.codegen." + wrapper.getSimpleName());
		define(primarySource().ofType(SwaggerSource_MinimalNormalizerOptions.class));
		define(parameter().named(SWAGGER_CODEGEN_CONFIG).optional().withDescription(
				"Contents of swagger codegen configuration file.",
				"This is the file that would be passed with --config option on swagger codegen command line.",
				"The JSON contents of that file should be the value of this parameter.",
				"This parameter need not be used. If it is absent, all string-valued parameters are collected into",
				"a map that is then passed to the swagger codegen module. If a map is provided here, then string-valued",
				"parameters are still copied in, overriding like-named values appearing in the map."));
		define(parameter().named(SWAGGER_CODEGEN_SYSTEM_PROPERTIES).optional().withDescription(
				"System properties to set, as in the -D option of swagger codegen command line.",
				"Each property should be a json object with a name/value pair for each property.",
				"Example: for '-Dmodels -Dapis=User,Pets' use the following:", "value:", "  models: ''",
				"  apis: Users,Pets"));
		define(GenTemplateProperty.swaggerCodegenProvider());
		if (info != null) {
			define(property().named(StandardProperties.DESCRIPTION) //
					.withValue(String.format("Provider: %s\nGenerator Name: %s\nType: %s\nPackage: %s\nClassname: %s",
							"Swagger Codegen", info.getReportedName(), info.getType(), wrapper.getPackageName(),
							wrapper.getSimpleName())));
			define(property().named(StandardProperties.GENERATOR_TYPE).withValue(info.getType().name()));
		}
	}

	public static class Generator extends GenTemplate.StaticGenerator<OpenApiDocument> {

		private GenModuleWrapper<CodegenConfig> wrapper;

		public Generator(GenTemplate<OpenApiDocument> genTemplate, GenTemplateContext context,
				GenModuleWrapper<CodegenConfig> wrapper) {
			super(genTemplate, context);
			this.wrapper = wrapper;
		}

		@Override
		public void generate(OpenApiDocument model) throws GenerationException {
			OpenAPI openApi = model.asOpenAPI();
			generate(openApi);
		}

		private void generate(OpenAPI model) throws GenerationException {
			CodegenConfig openApiCodegen;
			try {
				openApiCodegen = wrapper.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new GenerationException("Failed to instantiate Swagger Codegen instance", e);
			}
			openApiCodegen.setOutputDir(context.getOutputDirectory().getAbsolutePath());
			@SuppressWarnings("unchecked")
			Map<String, String> config = (Map<String, String>) context.getGenTargetParameters()
					.get(SWAGGER_CODEGEN_CONFIG);
			if (config == null) {
				config = Maps.newHashMap();
			}
			addParameters(config, context.getGenTargetParameters());
			ClientOptInput clientOptInput = new ClientOptInput();
			clientOptInput.setConfig(openApiCodegen);
			ClientOpts clientOpts = new ClientOpts();
			clientOpts.setOutputDirectory(context.getOutputDirectory().getAbsolutePath());
			clientOpts.setProperties(config != null ? config : Maps.<String, String>newHashMap());
			clientOptInput.setOpts(clientOpts);
			clientOptInput.setOpenAPI(model);
			DefaultGenerator generator = new DefaultGenerator();
			@SuppressWarnings("unchecked")
			Map<String, String> systemProperties = (Map<String, String>) context.getGenTargetParameters()
					.get(SWAGGER_CODEGEN_SYSTEM_PROPERTIES);
			setSystemProperties(systemProperties);
			generator.opts(clientOptInput);
			reportScgVersion();
			generator.generate();
		}

		private void addParameters(Map<String, String> config, Map<String, Object> params) {
			for (String key : params.keySet()) {
				if (!SPECIAL_PARAMS.contains(key)) {
					Object value = params.get(key);
					if (value != null && value instanceof String) {
						config.put(key, (String) value);
					}
				}
			}
		}

		private void setSystemProperties(Map<String, String> properties) {
			if (properties != null) {
				for (String key : properties.keySet()) {
					System.setProperty(key, properties.get(key));
				}
			}
		}

		private void reportScgVersion() {
			context.getLogger().info(String.format("Using swagger-codegen v%s\n",
					CodegenConfig.class.getPackage().getImplementationVersion()));
		}
	}
}