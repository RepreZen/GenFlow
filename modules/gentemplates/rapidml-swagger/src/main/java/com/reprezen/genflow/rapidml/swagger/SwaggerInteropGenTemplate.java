package com.reprezen.genflow.rapidml.swagger;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;

public class SwaggerInteropGenTemplate extends XSwaggerGenTemplate {

	public SwaggerInteropGenTemplate() {
		super();
	}

	@Override
	public boolean isSuppressed() {
		return false;
	}

	@Override
	public String getName() {
		return "Swagger (RAPID-XChange Interop)";
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swagger.SwaggerInteropGenTemplate");
		defineZenModelSource();
		configureParameters();
		defineParameterizedOutputItem(outputItem().using(XGenerateSwaggerInterop.class), "JSON",
				"${zenModel.name}.json");
		defineParameterizedOutputItem(outputItem().using(XGenerateSwaggerInteropYaml.class), "YAML",
				"${zenModel.name}.yaml");
		define(GenTemplateProperty.reprezenProvider());
	}

}
