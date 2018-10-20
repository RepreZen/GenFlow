package com.reprezen.genflow.openapi.swaggerui.v2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class SwaggerUiOptions {

	public static final SwaggerUiOptions DEFAULT = new SwaggerUiOptions();

	public static final String HOST_OPTION = "host";
	public static final String BASE_PATH_OPTION = "basePath";
	public static final String INCLUDE_DEPENDENCIES_OPTION = "includeDependencies";
	public static final String THEME_OPTION = "theme";

	public static final String DOC_EXPANSION_OPTION = "docExpansion";
	public static final String APIS_SORTER_OPTION = "apisSorter";
	public static final String OPERATIONS_SORTER_OPTION = "operationsSorter";
	public static final String DEFAULT_MODEL_RENDERING_OPTION = "defaultModelRendering";
	public static final String SUPPORTED_SUBMIT_METHOD_OPTION = "supportedSubmitMethods";
	public static final String JSON_EDITOR_OPTION = "jsonEditor";
	public static final String SHOW_REQUEST_HEADERS = "showRequestHeaders";
	public static final String OAUTH_REDIRECT_URL = "oauth2RedirectUrl";

	public static final String JSON_EDITOR_SETTABLE_OPTION = "showJsonEditorInSettings";

	public static SwaggerUiOptions fromParams(Map<String, Object> params) throws IllegalArgumentException {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false);
		mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {
			return mapper.convertValue(params, SwaggerUiOptions.class);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
					"Some of the Swagger UI Generator parameters have invalid type: " + e.getMessage(), e);
		}
	}

	private String basePath;
	private String host;
	private Theme theme;
	private boolean includeDependencies = true;
	private DocExpansion docExpansion = DocExpansion.LIST;
	private String apisSorter = "alpha";
	private String operationsSorter = null;
	private DefaultModelRendering defaultModelRendering = DefaultModelRendering.SCHEMA;
	private SupportedSubmitMethod[] supportedSubmitMethods = { SupportedSubmitMethod.GET, SupportedSubmitMethod.POST,
			SupportedSubmitMethod.PUT, SupportedSubmitMethod.DELETE, SupportedSubmitMethod.PATCH };
	private boolean jsonEditor;
	private boolean showRequestHeaders;
	private String oauth2RedirectUrl;

	private boolean showJsonEditorInSettings = true;

	public boolean isIncludeDependencies() {
		return includeDependencies;
	}

	public String getBasePath() {
		return basePath;
	}

	public String getHost() {
		return host;
	}

	public Theme getTheme() {
		return theme;
	}

	public String getThemeAsString() {
		return safeToLowerCase(theme);
	}

	public DocExpansion getDocExpansion() {
		return docExpansion;
	}

	public String getDocExpansionAsString() {
		return safeToLowerCase(docExpansion);
	}

	public String getApisSorter() {
		return apisSorter;
	}

	public String getApisSorterAsString() {
		return apisSorter;
	}

	public String getOperationsSorter() {
		return operationsSorter;
	}

	public String getOperationsSorterAsString() {
		return operationsSorter;
	}

	public DefaultModelRendering getDefaultModelRendering() {
		return defaultModelRendering;
	}

	public String getDefaultModelRenderingAsString() {
		return safeToLowerCase(defaultModelRendering);
	}

	private String safeToLowerCase(Enum<?> value) {
		return value != null ? value.name().toLowerCase() : null;
	}

	public SupportedSubmitMethod[] getSupportedSubmitMethods() {
		return supportedSubmitMethods;
	}

	public String getSupportedSubmitMethodsAsString() {
		if (supportedSubmitMethods == null) {
			supportedSubmitMethods = new SupportedSubmitMethod[0];
		}
		List<SupportedSubmitMethod> asList = Arrays.asList(supportedSubmitMethods);
		String result = Joiner.on(", ").join(Lists.transform(asList, new Function<SupportedSubmitMethod, String>() {

			@Override
			public String apply(SupportedSubmitMethod input) {
				return "'" + input.toString().toLowerCase() + "'";
			}
		}));
		return "[" + result + "]";
	}

	public boolean isJsonEditor() {
		return jsonEditor;
	}

	public boolean isShowJsonEditorInSettings() {
		return showJsonEditorInSettings;
	}

	public boolean isShowRequestHeaders() {
		return showRequestHeaders;
	}

	public String getOauth2RedirectUrl() {
		return oauth2RedirectUrl;
	}

	public static enum Theme {
		FEELING_BLUE, //
		FLATTOP, //
		MATERIAL, //
		MONOKAI, //
		MUTED, //
		NEWSPAPER, //
		OUTLINE
	}

	public static enum DocExpansion {
		NONE, //
		LIST, //
		FULL
	}

	public static enum DefaultModelRendering {
		MODEL, //
		SCHEMA
	}

	public static enum SupportedSubmitMethod {
		GET, //
		POST, //
		PUT, //
		DELETE, //
		PATCH
	}

}
