/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.swaggerui.v2;

import static com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.APIS_SORTER_OPTION;
import static com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.BASE_PATH_OPTION;
import static com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.DEFAULT_MODEL_RENDERING_OPTION;
import static com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.HOST_OPTION;
import static com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.JSON_EDITOR_OPTION;
import static com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.JSON_EDITOR_SETTABLE_OPTION;
import static com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.OAUTH_REDIRECT_URL;
import static com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.OPERATIONS_SORTER_OPTION;
import static com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.SHOW_REQUEST_HEADERS;
import static com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.SUPPORTED_SUBMIT_METHOD_OPTION;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.base.Strings;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.swagger.SwaggerGenTemplate;
import com.reprezen.genflow.api.swagger.SwaggerOutputItem;
import com.reprezen.genflow.api.template.GenTemplateProperty;

import io.swagger.models.Swagger;

public class XSwaggerUiGenTemplate extends SwaggerGenTemplate {

	@Override
	protected void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swaggerui.XSwaggerUiGenTemplate");
		defineSwaggerSource();

		SwaggerUiOptions defaultOptions = SwaggerUiOptions.DEFAULT;
		define(parameter().named(HOST_OPTION).optional().withDescription("Host to be used for TryItOut testing"));
		define(parameter().named(BASE_PATH_OPTION).optional()
				.withDescription("Base path to be used for TryItOut testing"));
		define(parameter().named(SwaggerUiOptions.THEME_OPTION).optional().withDefault(defaultOptions.getTheme())
				.withDescription("CSS theme to style Swagger-UI. The following values are allowed:", //
						"- FEELING_BLUE", //
						"- FLATTOP", //
						"- MATERIAL", //
						"- MONOKAI", //
						"- MUTED", //
						"- NEWSPAPER", //
						"- OUTLINE", //
						"The DEFAULT theme (null) is used otherwise."));

		// Swagger-UI JavaScript options
		define(parameter().named(SwaggerUiOptions.DOC_EXPANSION_OPTION).optional()
				.withDefault(defaultOptions.getDocExpansion())
				.withDescription("Controls how the API listing is displayed. It can be set to:", //
						"- 'NONE', ", //
						"- 'LIST' (default, shows operations for each resource),", //
						"- 'FULL' (fully expanded: shows operations and their details)"));

		define(parameter().named(APIS_SORTER_OPTION).optional().withDefault(defaultOptions.getApisSorter())
				.withDescription("Apply a sort to the API/tags list.", //
						"It can be 'alpha' (sort by name) or a function (see Array.prototype.sort() to know how sort function works)"));

		define(parameter().named(OPERATIONS_SORTER_OPTION).optional().withDefault(defaultOptions.getOperationsSorter())
				.withDescription("Apply a sort to the operation list of each API.", //
						"It can be 'alpha' (sort by paths alphanumerically), 'method' (sort by HTTP method) or a function (see Array.prototype.sort() to know how sort function works).", //
						"Default is the order returned by the server unchanged."));

		define(parameter().named(DEFAULT_MODEL_RENDERING_OPTION).optional()
				.withDefault(defaultOptions.getDefaultModelRendering())
				.withDescription("Controls how models are shown when the API is first rendered.", //
						"(The user can always switch the rendering for a given model by clicking the 'Model' and 'Model Schema' links.)", //
						"It can be set to 'MODEL' or 'SCHEMA', and the default is 'schema'"));

		define(parameter().named(SUPPORTED_SUBMIT_METHOD_OPTION).optional()
				.withDefault(defaultOptions.getSupportedSubmitMethods())
				.withDescription("An array of of the HTTP operations that will have the 'Try it out!' option.", //
						"An empty array disables all operations. ", //
						"This does not filter the operations from the display."));

		define(parameter().named(JSON_EDITOR_OPTION).optional().withDefault(defaultOptions.isJsonEditor())
				.withDescription("Enables a graphical view for editing complex bodies. Defaults to false."));

		define(parameter().named(JSON_EDITOR_SETTABLE_OPTION).optional()
				.withDefault(defaultOptions.isShowJsonEditorInSettings())
				.withDescription("'true' value displays the Json Editor option in the settings."));

		define(parameter().named(SHOW_REQUEST_HEADERS).optional().withDefault(defaultOptions.isShowRequestHeaders())
				.withDescription(
						"Whether or not to show the headers that were sent when making a request via the 'Try it out!' option. Defaults to false."));

		define(parameter().named(OAUTH_REDIRECT_URL).optional().withDefault(defaultOptions.getOauth2RedirectUrl())
				.withDescription("OAuth redirect URL."));

		// define(parameter().named(INCLUDE_DEPENDENCIES_OPTION).optional().withDefault("false").withDescription(
		// "Set to true generates all needed artifacts (*.js and *.css files) in the
		// generated folder, set to false
		// references them via full internet URLs"));

		define(outputItem().named("HTML").using(Generator.class).writing("${swagger.info.title}_swagger-ui.html"));
		define(staticResource().copying("assets").to("assets"));
		define(staticResource().copying("themes").to("assets/css"));
		define(GenTemplateProperty.reprezenProvider());
	}

	@Override
	public String getName() {
		return "Swagger UI v2";
	}

	public static class Generator extends SwaggerOutputItem {

		@Override
		public String generate(Swagger swagger) throws GenerationException {
			SwaggerUiOptions options = SwaggerUiOptions.fromParams(context.getGenTargetParameters());
			if (!Strings.isNullOrEmpty(options.getBasePath())) {
				swagger.setBasePath(options.getBasePath());
			}
			if (!Strings.isNullOrEmpty(options.getHost())) {
				swagger.setHost(options.getHost());
			}

			URL resolutionBase = getResolutionBase();
			String swaggerUiString = new XGenerateSwaggerUI().generateForSwaggerSpec(swagger, resolutionBase,
					isLiveView(), options, context);

			return swaggerUiString;
		}

		private URL getResolutionBase() {
			try {
				return context.getPrimarySource().getInputFile().toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		}

		private boolean isLiveView() {
			Object previewParam = context.getGenTargetParameters().get("preview");
			return previewParam != null && previewParam instanceof Boolean && ((Boolean) previewParam);
		}
	}
}
