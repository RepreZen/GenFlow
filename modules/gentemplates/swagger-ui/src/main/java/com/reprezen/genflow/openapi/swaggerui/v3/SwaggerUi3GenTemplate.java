/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.swaggerui.v3;

import static com.reprezen.genflow.openapi.swaggerui.v3.SwaggerUi3Options.BASE_PATH_OPTION;
import static com.reprezen.genflow.openapi.swaggerui.v3.SwaggerUi3Options.HOST_OPTION;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.openapi.OpenApiDocument;
import com.reprezen.genflow.api.openapi.OpenApiGenTemplate;
import com.reprezen.genflow.api.openapi.OpenApiOutputItem;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.swagger.models.Swagger;

public class SwaggerUi3GenTemplate extends OpenApiGenTemplate {

	public static final String SWAGGER_UI_VERSION = "3.18.2";

	@Override
	protected void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swaggerui3.SwaggerUi3GenTemplate");
		defineOpenApiSource();

		SwaggerUi3Options defaultOptions = SwaggerUi3Options.DEFAULT;
		define(parameter().named(HOST_OPTION).optional().withDescription("Host to be used for TryItOut testing"));
		define(parameter().named(BASE_PATH_OPTION).optional()
				.withDescription("Base path to be used for TryItOut testing"));

		// Display options
		define(parameter().named(SwaggerUi3Options.DEEP_LINKING_OPTION).optional()
				.withDefault(defaultOptions.isDeepLinking())
				.withDescription("If set to true, enables dynamic deep linking for tags and operations."));

		define(parameter().named(SwaggerUi3Options.DISPLAY_OPERATION_ID_OPTION).optional()
				.withDefault(defaultOptions.isDisplayOperationId())
				.withDescription("Controls the display of operationId in operations list. The default is false."));

		define(parameter().named(SwaggerUi3Options.DEFAULT_MODELS_EXPANDED_DEPTH_OPTION).optional()
				.withDefault(defaultOptions.getDefaultModelsExpandDepth()).withDescription(
						"The default expansion depth for models (set to -1 completely hide the models). The default value is 1."));

		define(parameter().named(SwaggerUi3Options.DEFAULT_MODEL_EXPANDED_DEPTH_OPTION).optional()
				.withDefault(defaultOptions.getDefaultModelExpandDepth()).withDescription(
						"The default expansion depth for the model on the model-example section. The default value is 1."));

		define(parameter().named(SwaggerUi3Options.DEFAULT_MODEL_RENDERING_OPTION).optional()
				.withDefault(defaultOptions.getDefaultModelRendering()).withDescription( //
						"Controls how the model is shown when the API is first rendered.", //
						"(The user can always switch the rendering for a given model by clicking the 'Model' and 'Example Value' links.)"));

		define(parameter().named(SwaggerUi3Options.DISPLAY_REQUEST_DURATION_OPTION).optional()
				.withDefault(defaultOptions.isDisplayRequestDuration()).withDescription(
						"Controls the display of the request duration (in milliseconds) for Try it out requests. The default is false."));

		define(parameter().named(SwaggerUi3Options.DOC_EXPANSION_OPTION).optional()
				.withDefault(defaultOptions.getDocExpansion()).withDescription( //
						"Controls the default expansion setting for the operations and tags. ", //
						"It can be 'LIST' (expands only the tags), 'FULL' (expands the tags and operations) or 'NONE' (expands nothing). ", //
						"The default is 'LIST'."));

		define(parameter().named(SwaggerUi3Options.FILTER_OPTION).optional().withDefault(defaultOptions.isFilter())
				.withDescription(
						"If set, enables filtering. The top bar will show an edit box that you can use to filter the tagged operations that are shown. ", //
						"Can be true/false to enable or disable, or an explicit filter string in which case filtering will be enabled using that string as the filter expression.", //
						"Filtering is case sensitive matching the filter expression anywhere inside the tag."));

		define(parameter().named(SwaggerUi3Options.MAX_DISPLAYED_TAGS_OPTION).optional()
				.withDefault(defaultOptions.getMaxDisplayedTags()).withDescription(
						"If set, limits the number of tagged operations displayed to at most this many. The default is to show all operations."));

		define(parameter().named(SwaggerUi3Options.OPERATIONS_SORTER_OPTION).optional().withDescription(
				"Apply a sort to the operation list of each API. It can be 'alpha' (sort by paths alphanumerically), ", //
				"'method' (sort by HTTP method) or a function (see Array.prototype.sort() to know how sort function works). ", //
				"Default is the order returned by the server unchanged."));

		define(parameter().named(SwaggerUi3Options.SHOW_EXTENSIONS_OPTION).optional().withDescription(
				"Controls the display of vendor extension (x-) fields and values for Operations, Parameters, and Schema."));

		define(parameter().named(SwaggerUi3Options.SHOW_COMMON_EXTENSIONS_OPTION).optional().withDescription(
				"Controls the display of extensions (pattern, maxLength, minLength, maximum, minimum) fields and values for Parameters."));

		define(parameter().named(SwaggerUi3Options.TAGS_SORTER_OPTION).optional().withDescription(
				"Apply a sort to the tag list of each API. It can be 'alpha' (sort by paths alphanumerically) or a function. ", //
				"Two tag name strings are passed to the sorter for each pass. Default is the order determined by Swagger-UI. "));

		define(parameter().named(SwaggerUi3Options.ON_COMPLETE_OPTION).optional().withDescription(
				"Provides a mechanism to be notified when Swagger-UI has finished rendering a newly provided definition."));

		// Network options
		define(parameter().named(SwaggerUi3Options.OAUTH_REDIRECT_URL_OPTION).optional()
				.withDefault(defaultOptions.getOauth2RedirectUrl()).withDescription("OAuth redirect URL"));

		define(parameter().named(SwaggerUi3Options.REQUEST_INTERCEPTOR_OPTION).optional()
				.withDefault(defaultOptions.getOauth2RedirectUrl()).withDescription(
						"MUST be a function. Function to intercept remote definition, Try-It-Out, and OAuth2 requests.", //
						"Accepts one argument requestInterceptor(request) and must return the modified request, or a Promise that resolves to the modified request."));

		define(parameter().named(SwaggerUi3Options.RESPONSE_INTERCEPTOR_OPTION).optional()
				.withDefault(defaultOptions.getOauth2RedirectUrl()).withDescription(
						"MUST be a function. Function to intercept remote definition, Try-It-Out, and OAuth2 responses.", //
						"Accepts one argument responseInterceptor(response) and must return the modified response, or a Promise that resolves to the modified response."));

		define(parameter().named(SwaggerUi3Options.SHOW_MUTATED_REQUEST_OPTION).optional().withDescription(
				"If set to true (the default), uses the mutated request returned from a rquestInterceptor to produce the curl command in the UI, ", //
				"otherwise the request before the requestInterceptor was applied is used."));

		define(parameter().named(SwaggerUi3Options.SUPPORTED_SUBMIT_METHODS_OPTION).optional().withDescription(
				"List of HTTP methods that have the Try it out feature enabled. An empty array disables Try it out for all operations.", //
				"This does not filter the operations from the display."));

		define(parameter().named(SwaggerUi3Options.VALIDATOR_URL_OPTION).optional().withDescription(
				"By default, Swagger-UI attempts to validate specs against swagger.io's online validator.", //
				"You can use this parameter to set a different validator URL, for example for locally deployed validators (Validator Badge).", //
				"Setting it to null will disable validation."));

		// Macros
		define(parameter().named(SwaggerUi3Options.MODEL_PROPERTY_MACRO_OPTION).optional().withDescription(
				"Function to set default values to each property in model. Accepts one argument modelPropertyMacro(property), property is immutable."));

		define(parameter().named(SwaggerUi3Options.PARAMETER_MACRO_OPTION).optional().withDescription(
				"Function to set default value to parameters. Accepts two arguments parameterMacro(operation, parameter). Operation and parameter are objects passed for context, both remain immutable."));

		// Instance Methods
		define(parameter().named(SwaggerUi3Options.INIT_OAUTH_OPTION).optional().withDescription(
				"Provide Swagger-UI with information about your OAuth server - see the OAuth2 documentation for more information."));

		define(parameter().named(SwaggerUi3Options.PRE_AUTHORIZE_BASIC_OPTION).optional()
				.withDescription("Programmatically set values for a Basic authorization scheme."));

		define(parameter().named(SwaggerUi3Options.PRE_AUTHORIZE_API_KEY_OPTION).optional()
				.withDescription("Programmatically set values for an API key authorization scheme."));

		define(outputItem().named("HTML").using(Generator.class).writing("${_model.title}_swagger-ui.html"));
		String assetsPath = String.format("%s/assets", SWAGGER_UI_VERSION);
		define(staticResource().copying(assetsPath).to("assets"));
		define(staticResource().copying("reprezen").to("reprezen"));
		define(GenTemplateProperty.reprezenProvider());
	}

	@Override
	public String getName() {
		return "Swagger UI v3";
	}

	public static class Generator extends OpenApiOutputItem {

		@Override
		public String generate(OpenApiDocument oaDoc) throws GenerationException {
			SwaggerUi3Options options = SwaggerUi3Options.fromParams(context.getGenTargetParameters());
			if (oaDoc.isOpenApi3()) {
				handleOptions(oaDoc.asKaizenOpenApi3(), options);
			} else if (oaDoc.isSwagger()) {
				handleOptions(oaDoc.asSwagger(), options);
			}
			String modelSpec;
			try {
				modelSpec = oaDoc.asSpec();
			} catch (IOException e) {
				throw new GenerationException("Failed to extract JSON from OpenApi document", e);
			}
			String urlPrefix = context.getOutputDirectory().toURI().toString();
			String swaggerUiString = new XGenerateSwaggerUIv3().generate(modelSpec, urlPrefix, isLiveView(), options,
					context);
			return swaggerUiString;
		}

		private void handleOptions(Swagger swagger, SwaggerUi3Options options) {
			if (!Strings.isNullOrEmpty(options.getBasePath())) {
				swagger.setBasePath(options.getBasePath());
			}
			if (!Strings.isNullOrEmpty(options.getHost())) {
				swagger.setHost(options.getHost());
			}
		}

		private void handleOptions(OpenApi3 doc, SwaggerUi3Options options) {
			// NYI
		}

		private boolean isLiveView() {
			Object previewParam = context.getGenTargetParameters().get("preview");
			return previewParam != null && previewParam instanceof Boolean && ((Boolean) previewParam);
		}

		private static ObjectMapper jsonMapper = new ObjectMapper();
		static {
			jsonMapper.setSerializationInclusion(Include.NON_NULL);
		}
	}
}
