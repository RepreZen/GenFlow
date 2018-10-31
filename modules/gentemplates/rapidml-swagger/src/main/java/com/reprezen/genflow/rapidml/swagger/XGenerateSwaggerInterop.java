package com.reprezen.genflow.rapidml.swagger;

import com.reprezen.genflow.common.jsonschema.builder.xchange.InteropJsonSchemaNodeFactory;

public class XGenerateSwaggerInterop extends XGenerateSwagger {
	public XGenerateSwaggerInterop() {
		this(SwaggerOutputFormat.JSON);
	}

	public XGenerateSwaggerInterop(SwaggerOutputFormat format) {
		super(format, new JsonSchemaForSwaggerGenerator(new InteropJsonSchemaNodeFactory()));
	}

}
