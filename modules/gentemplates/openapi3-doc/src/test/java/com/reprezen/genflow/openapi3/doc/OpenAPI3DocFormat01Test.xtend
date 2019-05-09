package com.reprezen.genflow.openapi3.doc;

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.kaizen.oasparser.OpenApiParser
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.junit.Test
import static org.junit.Assert.*
import com.google.common.io.Resources
import com.reprezen.genflow.api.target.GenTargetUtils
import java.util.logging.Logger
import java.io.File
import java.nio.file.Paths

public class OpenAPI3DocFormat01Test {

	@Test
	public def void testFormatting() {
		val url = Resources.getResource("gentargets/formattingTest01/formattingTest01.gen")

		val genTarget = GenTargetUtils.load(Paths.get(url.toURI()).toFile())

		val result = genTarget.execute(Logger.getLogger("test"))
		val generatedFolder = new File(result.baseDirectory, "generated")
	}
}