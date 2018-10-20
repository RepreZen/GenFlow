package com.reprezen.genflow.openapi.diagram.swagger;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.swagger.SwaggerGenTemplate;
import com.reprezen.genflow.api.template.GenTemplateProperty;

public class XSwaggerDiagGenTemplate extends SwaggerGenTemplate {

	@Override
	protected void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swaggerdiag.XSwaggerDiagGenTemplate");
		defineSwaggerSource();
		define(outputItem().named("HTML").using(XGenerateSwaggerDiagram.class)
				.writing("${swagger.info.title}_diagram.html"));
		define(staticResource().copying("../assets").to("."));
		define(GenTemplateProperty.reprezenProvider());
	}

	@Override
	public String getName() {
		return "RepreZen Diagram";
	}
}
