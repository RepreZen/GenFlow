/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.swagger.SwaggerGenTemplate;
import com.reprezen.genflow.api.swagger.SwaggerSource.SwaggerSource_MinimalNormalizerOptions;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.template.GenTemplateProperty.StandardProperties;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.swagger.codegen.SwaggerCodegenModulesInfo.Info;

import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.ClientOpts;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.models.Swagger;

public abstract class SwaggerCodegenGenTemplate extends SwaggerGenTemplate {

	public static final String SWAGGER_CODEGEN_SYSTEM_PROPERTIES = "swaggerCodegenSystemProperties";
	public static final String SWAGGER_CODEGEN_CONFIG = "swaggerCodegenConfig";
	public static final List<String> SPECIAL_PARAMS = Arrays.asList(SWAGGER_CODEGEN_CONFIG,
			SWAGGER_CODEGEN_SYSTEM_PROPERTIES);
	protected final Class<? extends CodegenConfig> codegenClass;
	private Info info;

	public SwaggerCodegenGenTemplate(Class<? extends CodegenConfig> codegenClass, Info info) {
		this.codegenClass = codegenClass;
		this.info = info;
	}

	@Override
	public IGenTemplate newInstance() throws GenerationException {
		return new BuiltinSwaggerCodegenGenTemplate(codegenClass, info);
	}

	@Override
	protected StaticGenerator<Swagger> getStaticGenerator() {
		return new Generator(this, context, codegenClass);
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swagger.codegen." + codegenClass.getSimpleName());
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
							"Swagger Codegen", info.getReportedName(), info.getType(),
							codegenClass.getPackage().getName(), codegenClass.getSimpleName())));
			define(property().named(StandardProperties.GENERATOR_TYPE).withValue(info.getType().name()));
		}
	}

	public static class Generator extends GenTemplate.StaticGenerator<Swagger> {

		private Class<? extends CodegenConfig> codegenClass;

		public Generator(GenTemplate<Swagger> genTemplate, GenTemplateContext context,
				Class<? extends CodegenConfig> codegenClass) {
			super(genTemplate, context);
			this.codegenClass = codegenClass;
		}

		@Override
		public void generate(Swagger model) throws GenerationException {
			CodegenConfig swaggerCodegen;
			try {
				swaggerCodegen = codegenClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new GenerationException("Failed to instantiate Swagger Codegen instance", e);
			}
			swaggerCodegen.setOutputDir(context.getOutputDirectory().getAbsolutePath());
			@SuppressWarnings("unchecked")
			Map<String, String> config = (Map<String, String>) context.getGenTargetParameters()
					.get(SWAGGER_CODEGEN_CONFIG);
			if (config == null) {
				config = Maps.newHashMap();
			}
			addParameters(config, context.getGenTargetParameters());
			ClientOptInput clientOptInput = new ClientOptInput();
			clientOptInput.setConfig(swaggerCodegen);
			ClientOpts clientOpts = new ClientOpts();
			clientOpts.setOutputDirectory(context.getOutputDirectory().getAbsolutePath());
			clientOpts.setProperties(config != null ? config : Maps.<String, String>newHashMap());
			clientOptInput.setOpts(clientOpts);
			clientOptInput.setSwagger(model);
			io.swagger.codegen.Generator generator = new DefaultGenerator();
			@SuppressWarnings("unchecked")
			Map<String, String> systemProperties = (Map<String, String>) context.getGenTargetParameters()
					.get(SWAGGER_CODEGEN_SYSTEM_PROPERTIES);
			setSystemProperties(systemProperties);
			generator.opts(clientOptInput);
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
	}
}