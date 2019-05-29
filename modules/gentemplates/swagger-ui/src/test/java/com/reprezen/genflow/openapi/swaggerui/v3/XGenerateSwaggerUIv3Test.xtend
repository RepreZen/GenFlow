package com.reprezen.genflow.openapi.swaggerui.v3

import com.google.common.io.Resources
import com.reprezen.genflow.api.target.GenTargetUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Map
import java.util.logging.Logger
import org.junit.Test

import static org.hamcrest.core.IsNot.*
import static org.hamcrest.core.StringContains.*
import static org.junit.Assert.*

class XGenerateSwaggerUIv3Test {

	@Test
	def void testRequestInterceptor() {
		val genTarget = GenTargetUtils.load(Paths.get(Resources.getResource("Swagger UI v3.gen").toURI).toFile)
		val result = genTarget.execute(Logger.getLogger("test"))
		val html = new File(new File(result.baseDirectory, "generated"), "Swagger Petstore_swagger-ui.html")

		val requestInterceptor = Files.lines(html.toPath)
			.filter[it.contains("requestInterceptor")]
			.findAny

		assertFalse(requestInterceptor.isPresent)
	}

	@Test
	def void testGenerateDefaultOptions() {
		val result = new XGenerateSwaggerUIv3().generateOptions("{}", SwaggerUi3Options.DEFAULT)

		assertThat(result, containsString("url: \"\","))
		assertThat(result, containsString("spec: {},"))
		assertThat(result, containsString("dom_id: '#swagger-ui',"))

		assertThat(result, containsString("layout: \"BaseLayout\","))
		assertThat(result, containsString("plugins: "))
		assertThat(result, containsString("presets: "))

		assertThat(result, containsString("deepLinking: false,"))
		assertThat(result, containsString("displayOperationId: false,"))
		assertThat(result, containsString("defaultModelsExpandDepth: 1,"))
		assertThat(result, containsString("defaultModelExpandDepth: 1,"))
		assertThat(result, containsString("defaultModelRendering: \"example\","))
		assertThat(result, containsString("displayRequestDuration: false,"))
		assertThat(result, containsString("docExpansion: \"list\","))
		assertThat(result, containsString("filter: false,"))
		assertThat(result, containsString("maxDisplayedTags: -1,"))
		assertThat(result, not(containsString("operationsSorter")))
		assertThat(result, containsString("showExtensions: false,"))
		assertThat(result, containsString("showCommonExtensions: false,"))
		assertThat(result, not(containsString("tagsSorter")))
		assertThat(result, not(containsString("onComplete")))

		assertThat(result, not(containsString("requestInterceptor")))
		assertThat(result, not(containsString("responseInterceptor")))
		assertThat(result, containsString("showMutatedRequest: true,"))
		assertThat(result, containsString("showMutatedRequest: true,"))
		assertThat(result, containsString("supportedSubmitMethods: ['get','put','post','delete','options','head','patch','trace'],"))
		assertThat(result, containsString("validatorUrl: \"https://online.swagger.io/validator\","))

		assertThat(result, not(containsString("modelPropertyMacro")))
		assertThat(result, not(containsString("parameterMacro")))

		assertThat(result, not(containsString("initOAuth: null,")))
		assertThat(result, not(containsString("preauthorizeBasic")))
		assertThat(result, not(containsString("preauthorizeApiKey")))
	}

	@Test
	def void testGenerateNullStringParams() {
		val options = #{
			"layout" -> null
		}

		val result = new XGenerateSwaggerUIv3().generateOptions("{}", SwaggerUi3Options.fromParams(options))

		assertThat(result, not(containsString("layout")))
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
