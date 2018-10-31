/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger;

import static com.reprezen.genflow.common.jsonschema.Options.LINKS_PROPERTY_NAME;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;
import com.reprezen.genflow.common.jsonschema.Options;

/**
 * @author Konstantin Zaitsev
 * @date Jun 1, 2015
 */
public class XSwaggerGenTemplate extends ZenModelGenTemplate {

	public static final String FOLD_MULTILINE = "foldMultiline";

	public XSwaggerGenTemplate() {
	}

	@Override
	public String getName() {
		return "Swagger"; //$NON-NLS-1$
	}

	@Override
	public boolean isSuppressed() {
		return true;
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swagger.XSwaggerGenTemplate");
		defineZenModelSource();
		defineParameterizedOutputItem(outputItem().using(XGenerateSwagger.class), "JSON", "${zenModel.name}.json");
		defineParameterizedOutputItem(outputItem().using(XGenerateSwaggerYaml.class), "YAML", "${zenModel.name}.yaml");
		configureParameters(true);
		define(GenTemplateProperty.reprezenProvider());
	}

	protected void configureParameters() throws GenerationException {
		configureParameters(false);
	}

	protected void configureParameters(boolean allowEmptyElements) throws GenerationException {
		define(parameter().named("retainEmptyParameters")
				.withDescription(
						"Include an empty Parameters collection on methods that don't have explicit parameters")
				.withDefault(false));
		define(parameter().named(Options.ALLOW_EMPTY_OBJECT)
				.withDescription("Allows empty object values if true. Otherwise sets minProperties=1.")
				.withDefault(allowEmptyElements));
		define(parameter().named(Options.ALLOW_EMPTY_ARRAY)
				.withDescription("Allows empty array values if true. Otherwise sets minItems=1.")
				.withDefault(allowEmptyElements));
		define(parameter().named(Options.ALLOW_EMPTY_STRING)
				.withDescription("Allows empty string values if true. Otherwise sets minLength=1.")
				.withDefault(allowEmptyElements));
		define(parameter().named(Options.INLINE_ARRAY_ITEMS).withDescription(
				"Inlines (embeds) array items inside the parent definition. Otherwise generates a top-level definition for array items.")
				.withDefault(false));
		define(parameter().named(Options.INLINE_REFERENCE_EMBEDS).withDescription(
				"Inlines (embeds) reference embed elements inside the parent definition. Otherwise generates a top-level definition for array items.")
				.withDefault(false));
		define(parameter().named(FOLD_MULTILINE).withDescription("Fold multi-line descriptions.").withDefault(true));
		define(parameter().named(LINKS_PROPERTY_NAME)
				.withDescription("Overrides links property name, the default name for this property is `_links`.")
				.optional().withDefault("_links"));
	}
}
