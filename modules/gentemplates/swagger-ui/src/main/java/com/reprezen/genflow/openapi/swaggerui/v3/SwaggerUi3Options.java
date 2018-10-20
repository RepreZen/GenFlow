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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.reprezen.genflow.openapi.swaggerui.v2.SwaggerUiOptions.DocExpansion;

public class SwaggerUi3Options {

	public static final SwaggerUi3Options DEFAULT = new SwaggerUi3Options();

	public static final String HOST_OPTION = "host";
	public static final String BASE_PATH_OPTION = "basePath";

	public static final String OAUTH_REDIRECT_URL = "oauth2RedirectUrl";
	public static final String TAGS_SORTER_OPTION = "tagsSorter";
	public static final String OPERATIONS_SORTER_OPTION = "operationsSorter";
	public static final String DEFAULT_MODEL_RENDERING_OPTION = "defaultModelRendering";
	public static final String DEFAULT_MODEL_EXPANDED_DEPTH_OPTION = "defaultModelExpandDepth";
	public static final String DOC_EXPANSION_OPTION = "docExpansion";
	public static final String DISPLAY_OPERATION_ID_OPTION = "displayOperationId";
	public static final String DISPLAY_REQUEST_DURATION_OPTION = "displayRequestDuration";
	public static final String MAX_DISPLAYED_TAGS_OPTION = "maxDisplayedTags";
	public static final String FILTER_OPTION = "filter";
	public static final String DEEP_LINKING_OPTION = "deepLinking";
	public static final String SHOW_MUTATED_REQUEST_OPTION = "showMutatedRequest";

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

	private String basePath;
	private String host;
	private String oauth2RedirectUrl;
	private String tagsSorter = null;
	private String operationsSorter = null;
	private DefaultModelRendering defaultModelRendering = DefaultModelRendering.EXAMPLE;
	private int defaultModelExpandDepth = 1;
	private DocExpansion docExpansion = DocExpansion.LIST;
	private boolean displayOperationId = false;
	private boolean displayRequestDuration = false;
	private int maxDisplayedTags = -1;
	private boolean filter = false;
	private boolean deepLinking = false;
	private boolean showMutatedRequest = false;

	private String safeToLowerCase(Enum<?> value) {
		return value != null ? value.name().toLowerCase() : null;
	}

	public String getBasePath() {
		return basePath;
	}

	public String getHost() {
		return host;
	}

	public String getOauth2RedirectUrl() {
		return oauth2RedirectUrl;
	}

	public String getTagsSorter() {
		return tagsSorter;
	}

	public String getOperationsSorter() {
		return operationsSorter;
	}

	public DefaultModelRendering getDefaultModelRendering() {
		return defaultModelRendering;
	}

	public String getDefaultModelRenderingAsString() {
		return safeToLowerCase(defaultModelRendering);
	}

	public int getDefaultModelExpandDepth() {
		return defaultModelExpandDepth;
	}

	public DocExpansion getDocExpansion() {
		return docExpansion;
	}

	public String getDocExpansionAsString() {
		return safeToLowerCase(docExpansion);
	}

	public boolean isDisplayOperationId() {
		return displayOperationId;
	}

	public boolean isDisplayRequestDuration() {
		return displayRequestDuration;
	}

	public int getMaxDisplayedTags() {
		return maxDisplayedTags;
	}

	public boolean isFilter() {
		return filter;
	}

	public boolean isDeepLinking() {
		return deepLinking;
	}

	public boolean isShowMutatedRequest() {
		return showMutatedRequest;
	}

	public static enum DefaultModelRendering {
		MODEL, //
		EXAMPLE
	}
}
