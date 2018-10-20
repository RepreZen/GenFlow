package com.reprezen.genflow.openapi.diagram.openapi3;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.openapi3.OpenApi3GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateProperty;

public class XOpenApi3DiagGenTemplate extends OpenApi3GenTemplate {

	@Override
	protected void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swaggerdiag.openapi3.XOpenApi3DiagGenTemplate");
		defineOpenApi3Source();
		define(outputItem().named("HTML").using(XGenerateOpenApi3Diagram.class).writing("diagram.html"));
		define(staticResource().copying("../assets").to("."));
		define(GenTemplateProperty.reprezenProvider());
	}

	@Override
	public String getName() {
		return "RepreZen Diagram";
	}
}
