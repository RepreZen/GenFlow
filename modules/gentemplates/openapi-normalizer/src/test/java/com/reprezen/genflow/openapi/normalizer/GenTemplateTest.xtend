package com.reprezen.genflow.openapi.normalizer

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import com.reprezen.genflow.api.target.GenTargetUtils
import java.io.File
import org.junit.Test

import static org.junit.Assert.*

class GenTemplateTest {

	@Test
	def void testGenTemplate() {
		val gt = GenTargetUtils.load(
			new File(Resources.getResource(GenTemplateTest, "/normalizer_path_or_components.gen").toURI))

		val traces = gt.execute
		val output = new File(new File(traces.baseDirectory, "generated"), "Swagger Petstore.json")
		
		val tree = new ObjectMapper().readTree(output)

		assertFalse(tree.get("paths").isEmpty)
		assertFalse(tree.get("components").isEmpty)
	}

}
