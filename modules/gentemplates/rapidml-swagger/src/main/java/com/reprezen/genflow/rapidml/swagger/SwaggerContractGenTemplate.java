package com.reprezen.genflow.rapidml.swagger;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.common.jsonschema.builder.xchange.ContractJsonSchemaNodeFactory;

public class SwaggerContractGenTemplate extends XSwaggerGenTemplate {

	public SwaggerContractGenTemplate() {
		super();
	}

	@Override
	public String getName() {
		return "Swagger (RAPID-XChange Contract)";
	}

	@Override
	public boolean isSuppressed() {
		return false;
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swagger.SwaggerContractGenTemplate");
		defineZenModelSource();
		configureParameters();
		defineParameterizedOutputItem(outputItem().using(XGenerateSwaggerContract.class), "JSON",
				"${zenModel.name}.json");
		defineParameterizedOutputItem(outputItem().using(XGenerateSwaggerContractYaml.class), "YAML",
				"${zenModel.name}.yaml");
		define(GenTemplateProperty.reprezenProvider());
	}

	public static class XGenerateSwaggerContract extends XGenerateSwagger {
		public XGenerateSwaggerContract() {
			this(SwaggerOutputFormat.JSON);
		}

		public XGenerateSwaggerContract(SwaggerOutputFormat format) {
			super(format, new JsonSchemaForSwaggerGenerator(new ContractJsonSchemaNodeFactory()));
		}

	}

	public static class XGenerateSwaggerContractYaml extends XGenerateSwaggerContract {

		public XGenerateSwaggerContractYaml() {
			super(SwaggerOutputFormat.YAML);
		}

	}

}
