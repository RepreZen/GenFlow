package com.reprezen.genflow.api.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.kaizen.oasparser.OpenApi;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.swagger.models.Swagger;
import io.swagger.v3.oas.models.OpenAPI;

public abstract class OpenApiDocument {

	// isXxx methods answer the question: is this a model that conforms to the Xxx
	// specification?

	/**
	 * @deprecated Use {@link #isSwagger()} or its alias, {@link #isOpenApi2()}
	 */
	@Deprecated
	public final boolean isSwagger2() {
		return isOpenApi2();
	}

	public boolean isOpenApi2() {
		return false;
	}

	public final boolean isSwagger() {
		return isOpenApi2();
	}

	public boolean isOpenApi3() {
		return false;
	}

	// asXxx methods return the model in the form produced by the Xxx parser, or in
	// the format indicated by Xxx when
	// parsing is avoided.

	/**
	 * @deprecated Use {@link #asSwagger()}
	 */
	@Deprecated
	public final Swagger asSwagger2() throws GenerationException {
		return asSwagger();
	}

	public Swagger asSwagger() throws GenerationException {
		return null;
	}

	/**
	 * 
	 * @deprecated Use {@link #asOpenAPI()}
	 */
	@Deprecated
	public final OpenAPI asOpenApi2() throws GenerationException {
		return asOpenAPI();
	}

	public OpenAPI asOpenAPI() throws GenerationException {
		return null;
	}

	/**
	 * 
	 * @deprecated Use {@link #asKaizenOpenApi3()}
	 */
	@Deprecated
	public final OpenApi3 asOpenApi3() throws GenerationException {
		return asKaizenOpenApi3();
	}

	public final OpenApi3 asKaizenOpenApi3() throws GenerationException {
		OpenApi<?> model = asKaizenOpenApi();
		return (OpenApi3) model;
	}

	public OpenApi<?> asKaizenOpenApi() throws GenerationException {
		return null;
	}

	public abstract JsonNode asJson() throws GenerationException;

	public abstract String asSpec() throws GenerationException, JsonProcessingException;

	public abstract String getTitle();
}
