package com.reprezen.genflow.swagger.doc;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.swagger.SwaggerGenTemplate;
import com.reprezen.genflow.api.swagger.SwaggerSource.SwaggerSource_DocNormalizerOptions;
import com.reprezen.genflow.api.template.GenTemplateProperty;

public class XSwaggerDocGenTemplate extends SwaggerGenTemplate {

	public static final String SHOW_ALL_OF_COMPONENTS_OPTION = "showAllOfComponentModels";
	public static final String INCLUDE_TABLE_OF_CONTENTS_OPTION = "includeTableOfContents";

	@Override
	protected void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swaggerdoc.XSwaggerDocGenTemplate");
		define(primarySource().ofType(SwaggerSource_DocNormalizerOptions.class));
		define(outputItem().named("HTML").using(XGenerateSwaggerDoc.class).writing("${swagger.info.title}_doc.html"));
		define(staticResource().copying("js").to("."));
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
