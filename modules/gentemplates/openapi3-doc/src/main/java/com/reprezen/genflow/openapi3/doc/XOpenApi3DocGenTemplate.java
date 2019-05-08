package com.reprezen.genflow.openapi3.doc;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.openapi3.OpenApi3GenTemplate;
import com.reprezen.genflow.api.openapi3.OpenApi3Source.OpenApi3Source_DocNormalizerOptions;
import com.reprezen.genflow.api.template.GenTemplateProperty;

public class XOpenApi3DocGenTemplate extends OpenApi3GenTemplate {

	public static final String SHOW_ALL_OF_COMPONENTS_OPTION = "showAllOfComponentModels";
	public static final String INCLUDE_TABLE_OF_CONTENTS_OPTION = "includeTableOfContents";
	public static final String OUTPUT_FILE_OVERRIDE_OPTION = "outputFileOverride";
	public static final String HTML_INJECTIONS_OPTION = "htmlInjections";

	@Override
	protected void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.openapi3doc.XOpenApi3DocGenTemplate");
		define(primarySource().ofType(OpenApi3Source_DocNormalizerOptions.class));
		// Primary var is defined in GenTemplate.resolve() and is
		// OpenApi3.toFirstLower()
		define(outputItem().named("HTML").using(XGenerateOpenApi3Doc.class).writing("${openApi3.info.title}_doc.html"));
		define(staticResource().copying("bootstrap").to("bootstrap"));
		define(staticResource().copying("jquery").to("jquery"));
		define(staticResource().copying("images").to("images"));
		define(parameter().named(SHOW_ALL_OF_COMPONENTS_OPTION).optional().withDefault("true")
				.withDescription("Set to false to suppress component model names in allOf models"));
		define(parameter().named(INCLUDE_TABLE_OF_CONTENTS_OPTION).optional().withDefault("true")
				.withDescription("Set to false to suppress table of contents"));
		define(GenTemplateProperty.reprezenProvider());
	}

	@Override
	public String getName() {
		return "RepreZen HTML Documentation";
	}

}
