/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.swaggerui.v3;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class SwaggerUi3Options {

	public static final SwaggerUi3Options DEFAULT = new SwaggerUi3Options();

	public static final String HOST_OPTION = "host";
	public static final String BASE_PATH_OPTION = "basePath";

	// Core
	public static final String CONFIG_URL_OPTION = "configUrl";
	public static final String DOM_ID_OPTION = "dom_id";
	public static final String DOM_NODE_OPTION = "domNode";
	public static final String SPEC_OPTION = "spec";
	public static final String URL_OPTION = "url";
	public static final String URLS_OPTION = "urls";
	public static final String URLS_PRIMARY_NAME_OPTION = "urls.primaryName";

	// Plugin System
	public static final String LAYOUT_OPTION = "layout";
	public static final String PLUGINS_OPTION = "plugins";
	public static final String PRESETS_OPTION = "presets";

	// Display
	public static final String DEEP_LINKING_OPTION = "deepLinking";
	public static final String DISPLAY_OPERATION_ID_OPTION = "displayOperationId";
	public static final String DEFAULT_MODELS_EXPANDED_DEPTH_OPTION = "defaultModelsExpandDepth";
	public static final String DEFAULT_MODEL_EXPANDED_DEPTH_OPTION = "defaultModelExpandDepth";
	public static final String DEFAULT_MODEL_RENDERING_OPTION = "defaultModelRendering";
	public static final String DISPLAY_REQUEST_DURATION_OPTION = "displayRequestDuration";
	public static final String DOC_EXPANSION_OPTION = "docExpansion";
	public static final String FILTER_OPTION = "filter";
	public static final String MAX_DISPLAYED_TAGS_OPTION = "maxDisplayedTags";
	public static final String OPERATIONS_SORTER_OPTION = "operationsSorter";
	public static final String SHOW_EXTENSIONS_OPTION = "showExtensions";
	public static final String SHOW_COMMON_EXTENSIONS_OPTION = "showCommonExtensions";
	public static final String TAGS_SORTER_OPTION = "tagsSorter";
	public static final String ON_COMPLETE_OPTION = "onComplete";

	// Network
	public static final String OAUTH_REDIRECT_URL_OPTION = "oauth2RedirectUrl";
	public static final String REQUEST_INTERCEPTOR_OPTION = "requestInterceptor";
	public static final String RESPONSE_INTERCEPTOR_OPTION = "responseInterceptor";
	public static final String SHOW_MUTATED_REQUEST_OPTION = "showMutatedRequest";
	public static final String SUPPORTED_SUBMIT_METHODS_OPTION = "supportedSubmitMethods";
	public static final String VALIDATOR_URL_OPTION = "validatorUrl";

	// Macros
	public static final String MODEL_PROPERTY_MACRO_OPTION = "modelPropertyMacro";
	public static final String PARAMETER_MACRO_OPTION = "parameterMacro";

	// Instance Methods
	public static final String INIT_OAUTH_OPTION = "initOAuth";
	public static final String PRE_AUTHORIZE_BASIC_OPTION = "preauthorizeBasic";
	public static final String PRE_AUTHORIZE_API_KEY_OPTION = "preauthorizeApiKey";

	public static SwaggerUi3Options fromParams(Map<String, Object> params) throws IllegalArgumentException {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false);
		mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {
			return mapper.convertValue(params, SwaggerUi3Options.class);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"Some of the Swagger UI Generator parameters have invalid type: " + e.getMessage(), e);
		}
	}

	private String host;
	private String basePath;

	// Core
	private String configUrl;
	private String domId;
	private String domNode;
	private String spec;
	private String url;
	private String urls;
	private String urlsPrimaryName;

	// Plugin System
	private String layout = "BaseLayout";
	private String[] plugins;
	private String[] presets;

	// Display
	private boolean deepLinking = false;
	private boolean displayOperationId = false;
	private int defaultModelsExpandDepth = 1;
	private int defaultModelExpandDepth = 1;
	private DefaultModelRendering defaultModelRendering = DefaultModelRendering.EXAMPLE;
	private boolean displayRequestDuration = false;
	private DocExpansion docExpansion = DocExpansion.LIST;
	private boolean filter = false;
	private int maxDisplayedTags = -1;
	private String operationsSorter = null;
	private boolean showExtensions = false;
	private boolean showCommonExtensions = false;
	private String tagsSorter = null;
	private String onComplete = null;

	// Network
	private String oauth2RedirectUrl;
	private String requestInterceptor;
	private String responseInterceptor;
	private boolean showMutatedRequest = true;
	private SupportedSubmitMethod[] supportedSubmitMethods = SupportedSubmitMethod.values();

	private String validatorUrl = "https://online.swagger.io/validator";

	// Macros
	private String modelPropertyMacro;
	private String parameterMacro;

	// Instance Methods
	private String initOAuth;
	private String preauthorizeBasic;
	private String preauthorizeApiKey;

	private String safeToLowerCase(Enum<?> value) {
		return value != null ? value.name().toLowerCase() : null;
	}

	public String getBasePath() {
		return basePath;
	}

	public String getHost() {
		return host;
	}

	// Core
	public String getConfigUrl() {
		return configUrl;
	}

	public String getDomId() {
		return domId;
	}

	public String getDomNode() {
		return domNode;
	}

	public String getSpec() {
		return spec;
	}

	public String getUrl() {
		return url;
	}

	public String getUrls() {
		return urls;
	}

	public String getUrlsPrimaryName() {
		return urlsPrimaryName;
	}

	// Plugin System

	public String getLayout() {
		return layout;
	}

	public String[] getPlugins() {
		return plugins;
	}

	public String[] getPresets() {
		return presets;
	}

	// Display

	public boolean isDeepLinking() {
		return deepLinking;
	}

	public boolean isDisplayOperationId() {
		return displayOperationId;
	}

	public int getDefaultModelsExpandDepth() {
		return defaultModelsExpandDepth;
	}

	public int getDefaultModelExpandDepth() {
		return defaultModelExpandDepth;
	}

	public DefaultModelRendering getDefaultModelRendering() {
		return defaultModelRendering;
	}

	public String getDefaultModelRenderingAsString() {
		return safeToLowerCase(defaultModelRendering);
	}

	public boolean isDisplayRequestDuration() {
		return displayRequestDuration;
	}

	public DocExpansion getDocExpansion() {
		return docExpansion;
	}

	public String getDocExpansionAsString() {
		return safeToLowerCase(docExpansion);
	}

	public boolean isFilter() {
		return filter;
	}

	public int getMaxDisplayedTags() {
		return maxDisplayedTags;
	}

	public String getOperationsSorter() {
		return operationsSorter;
	}

	public boolean isShowExtensions() {
		return showExtensions;
	}

	public boolean isShowCommonExtensions() {
		return showCommonExtensions;
	}

	public String getTagsSorter() {
		return tagsSorter;
	}

	public String getOnComplete() {
		return onComplete;
	}

	// Network

	public String getOauth2RedirectUrl() {
		return oauth2RedirectUrl;
	}

	public String getRequestInterceptor() {
		return requestInterceptor;
	}

	public String getResponseInterceptor() {
		return responseInterceptor;
	}

	public boolean isShowMutatedRequest() {
		return showMutatedRequest;
	}

	public SupportedSubmitMethod[] getSupportedSubmitMethods() {
		return supportedSubmitMethods;
	}

	public String getSupportedSubmitMethodsAsString() {
		if (supportedSubmitMethods == null) {
			supportedSubmitMethods = new SupportedSubmitMethod[0];
		}

		if (supportedSubmitMethods.length == 0) {
			return "";
		}

		return Stream.of(supportedSubmitMethods) //
				.map(e -> "'" + e.name().toLowerCase() + "'") //
				.collect(Collectors.joining(",", "[", "]"));
	}

	public String getValidatorUrl() {
		return validatorUrl;
	}

	// Macros

	public String getModelPropertyMacro() {
		return modelPropertyMacro;
	}

	public String getParameterMacro() {
		return parameterMacro;
	}

	// Instance Methods

	public String getInitOAuth() {
		return initOAuth;
	}

	public String getPreauthorizeBasic() {
		return preauthorizeBasic;
	}

	public String getPreauthorizeApiKey() {
		return preauthorizeApiKey;
	}

	public static enum DocExpansion {
		NONE, //
		LIST, //
		FULL
	}

	public static enum DefaultModelRendering {
		MODEL, //
		EXAMPLE
	}

	public static enum SupportedSubmitMethod {
		GET, //
		PUT, //
		POST, //
		DELETE, //
		OPTIONS, //
		HEAD, //
		PATCH, //
		TRACE
	}
}
