package com.reprezen.genflow.swagger.nswag;

import java.io.File;
import java.io.IOException;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.swagger.SwaggerSource;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.trace.GenTemplateTrace;
import com.reprezen.genflow.api.trace.GenTemplateTraceItem;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;
import com.reprezen.genflow.rapidml.swagger.SwaggerContractGenTemplate;
import com.reprezen.genflow.swagger.nswag.Config.Output;
import com.reprezen.rapidml.ZenModel;

import io.swagger.models.Swagger;

public class NSwagRapidTypeScriptClientGenTemplate extends ZenModelGenTemplate {

	@Override
	public boolean isSuppressed() {
		return !NSwagGenerator.isPlatformSupported();
	}

	private static final String SWAGGER_PREREQ_NAME = "swagger";

	@Override
	public String getName() {
		return "TypeScript Client by NSwag";
	}

	@Override
	protected void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.nswag.NSwagRapidTypeScriptClientGenTemplate");
		defineZenModelSource();
		define(prerequisite() //
				.named(SWAGGER_PREREQ_NAME) //
				.withDescription("GenTarget to produce Swagger from RAPID model") //
				.on(SwaggerContractGenTemplate.class) //
				.required());
		Config.defineGenTemplateParameters(this, Config.Output.TS_CLIENT);
		define(GenTemplateProperty.nswagProvider());
	}

	@Override
	protected StaticGenerator<ZenModel> getStaticGenerator() {
		return new Generator(this, context);
	}

	private static class Generator extends GenTemplate.StaticGenerator<ZenModel> {

		public Generator(GenTemplate<ZenModel> genTemplate, GenTemplateContext context) {
			super(genTemplate, context);
		}

		@Override
		public void generate(ZenModel model) throws GenerationException {
			Config config = Config.fromContext(context);
			File outputFile = new File(context.getOutputDirectory(), model.getName() + " Client.js");
			Swagger swagger = getSwaggerFromPrereq();
			try {
				new NSwagGenerator(config).generate(config.getNSwagRunFile(swagger, Output.TS_CLIENT, outputFile));
			} catch (IOException e) {
				throw new GenerationException("Failed to run NSwag Generation", e);
			}
		}

		private Swagger getSwaggerFromPrereq() throws GenerationException {
			GenTemplateTrace trace = context.getPrerequisiteTrace(SWAGGER_PREREQ_NAME);
			for (GenTemplateTraceItem item : trace.getTraceItems()) {
				if (item.getType() == GenTemplateTrace.FILE_ITEM_TYPE) {
					return new SwaggerSource(item.getOutputFile()).load();
				}
			}
			throw new GenerationException("Failed to load Swagger model from prerequisite GenTarget");
		}
	}
}
