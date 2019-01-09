package com.reprezen.genflow.rapidml.jsonschema.xchange.realizations

import com.google.common.io.Resources
import com.reprezen.genflow.rapidml.jsonschema.xchange.XChangeSchemaTestBase
import com.reprezen.genflow.test.common.GeneratorTestFixture
import org.eclipse.emf.common.util.URI

abstract class RealizationTestBase extends XChangeSchemaTestBase {
	
	def abstract String rapid_model_relative_path();

	override rapid_model_uri() {
		val path = "/models/autoRealization/Tests" + rapid_model_relative_path
		val url = Resources.getResource(GeneratorTestFixture, path)
		URI.createURI(url.toString)
	}

	override rapid_model() {
		throw new UnsupportedOperationException("Use Model Relative Path")
	}

}
