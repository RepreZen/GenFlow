package com.reprezen.genflow.openapi.swaggerui.v3

import java.util.HashMap
import org.junit.Test

import static org.hamcrest.MatcherAssert.*
import static org.hamcrest.core.StringContains.*
import java.util.Map

class XGenerateSwaggerUIv3Test {

	@Test
	def void testGenerateDefaultOptions() {
		val options = new HashMap<String, Object>()

		val result = new XGenerateSwaggerUIv3().generateOptions("{}", SwaggerUi3Options.fromParams(options))

		assertThat(result, containsString("url: \"\","));
		assertThat(result, containsString("spec: {},"));
		assertThat(result, containsString("dom_id: '#swagger-ui',"));

		assertThat(result, containsString("layout: \"BaseLayout\","));
		assertThat(result, containsString("plugins: "));
		assertThat(result, containsString("presets: "));

		assertThat(result, containsString("deepLinking: false,"));
		assertThat(result, containsString("displayOperationId: false,"));
		assertThat(result, containsString("defaultModelsExpandDepth: 1,"));
		assertThat(result, containsString("defaultModelExpandDepth: 1,"));
		assertThat(result, containsString("defaultModelRendering: \"example\","));
		assertThat(result, containsString("displayRequestDuration: false,"));
		assertThat(result, containsString("docExpansion: \"list\","));
		assertThat(result, containsString("filter: false,"));
		assertThat(result, containsString("maxDisplayedTags: -1,"));
		assertThat(result, containsString("operationsSorter: null,"));
		assertThat(result, containsString("showExtensions: false,"));
		assertThat(result, containsString("showCommonExtensions: false,"));
		assertThat(result, containsString("tagsSorter: null,"));
		assertThat(result, containsString("onComplete: null,"));

		assertThat(result, containsString("requestInterceptor: null,"));
		assertThat(result, containsString("responseInterceptor: null,"));
		assertThat(result, containsString("showMutatedRequest: true,"));
		assertThat(result, containsString("showMutatedRequest: true,"));
		assertThat(result, containsString("supportedSubmitMethods: ['get','put','post','delete','options','head','patch','trace'],"))
		assertThat(result, containsString("validatorUrl: \"https://online.swagger.io/validator\","));

		assertThat(result, containsString("modelPropertyMacro: null,"));
		assertThat(result, containsString("parameterMacro: null,"));

		assertThat(result, containsString("initOAuth: null,"));
		assertThat(result, containsString("preauthorizeBasic: null,"));
		assertThat(result, containsString("preauthorizeApiKey: null"));
	}

	@Test
	def void testGenerateNullStringParams() {
		val options = #{
			"layout" -> null
		}

		val result = new XGenerateSwaggerUIv3().generateOptions("{}", SwaggerUi3Options.fromParams(options))

		assertThat(result, containsString("layout: null,"));
	}

	@Test
	def void testGenerateFunctionParams() {
		val Map<String, Object> options = #{
			"operationsSorter" -> "function() {}",
			"tagsSorter" -> "function() {}",
			"onComplete" -> "function() {}",
			"requestInterceptor" -> "function() {}",
			"responseInterceptor" -> "function() {}",
			"modelPropertyMacro" -> "function() {}",
			"parameterMacro" -> "function() {}",
			"initOAuth" -> "function() {}",
			"preauthorizeBasic" -> "function() {}",
			"preauthorizeApiKey" -> "function() {}"
		}

		val result = new XGenerateSwaggerUIv3().generateOptions("{}", SwaggerUi3Options.fromParams(options))

		assertThat(result, containsString("operationsSorter: function() {},"));
		assertThat(result, containsString("tagsSorter: function() {},"));
		assertThat(result, containsString("onComplete: function() {},"));
		assertThat(result, containsString("requestInterceptor: function() {},"));
		assertThat(result, containsString("responseInterceptor: function() {},"));
		assertThat(result, containsString("modelPropertyMacro: function() {},"));
		assertThat(result, containsString("parameterMacro: function() {},"));
		assertThat(result, containsString("initOAuth: function() {},"));
		assertThat(result, containsString("preauthorizeBasic: function() {},"));
		assertThat(result, containsString("preauthorizeApiKey: function() {}"));
	}
}
