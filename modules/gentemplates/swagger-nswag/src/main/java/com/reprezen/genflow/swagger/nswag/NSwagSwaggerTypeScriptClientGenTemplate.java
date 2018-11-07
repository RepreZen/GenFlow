package com.reprezen.genflow.swagger.nswag;

import java.io.File;
import java.io.IOException;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.swagger.SwaggerGenTemplate;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.swagger.nswag.Config.Output;

import io.swagger.models.Swagger;

public class NSwagSwaggerTypeScriptClientGenTemplate extends SwaggerGenTemplate {

	@Override
	public boolean isSuppressed() {
		return !NSwagGenerator.isPlatformSupported();
	}

	@Override
	public String getName() {
		return "TypeScript Client by NSwag";
	}

	@Override
	protected void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.nswag.NSwagSwaggerTypeScriptClientGenTemplate");
		defineSwaggerSource();
		Config.defineGenTemplateParameters(this, Config.Output.TS_CLIENT);
		define(GenTemplateProperty.nswagProvider());
	}

	@Override
	protected StaticGenerator<Swagger> getStaticGenerator() {
		return new Generator(this, context);
	}

	private static class Generator extends GenTemplate.StaticGenerator<Swagger> {

		public Generator(GenTemplate<Swagger> genTemplate, GenTemplateContext context) {
			super(genTemplate, context);
		}

		@Override
		public void generate(Swagger swagger) throws GenerationException {
			Config config = Config.fromContext(context);
			File outputFile = new File(context.getOutputDirectory(), swagger.getInfo().getTitle() + " Client.js");

			try {
				new NSwagGenerator(config).generate(config.getNSwagRunFile(swagger, Output.TS_CLIENT, outputFile));
			} catch (IOException e) {
				throw new GenerationException("Failed to run NSwag Generation", e);
			}
		}

	}
}
