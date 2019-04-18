package com.reprezen.genflow.openapi.generator

import com.google.common.io.Resources
import com.reprezen.genflow.api.target.GenTargetUtils
import java.io.File
import java.nio.file.Paths
import java.util.logging.Logger
import org.junit.Test
import java.nio.file.Files

import static org.junit.Assert.*

class JavaClientCodeGenTest {

	@Test
	def void testDefault() {
		val url = Resources.getResource("fixtures/default/JavaClient.gen")

		val genTarget = GenTargetUtils.load(Paths.get(url.toURI()).toFile())

		val result = genTarget.execute(Logger.getLogger("test"))
		val generatedFolder = new File(result.baseDirectory, "generated")
		val modelFolder = new File(generatedFolder, "src/main/java/org/openapitools/client/model")

		assertTrue(Files.list(modelFolder.toPath).anyMatch["Pet.java".equals(it.toFile.name)])
	}

	@Test
	def void testImportMappings() {
		val url = Resources.getResource("fixtures/importMappings/JavaClient.gen")

		val genTarget = GenTargetUtils.load(Paths.get(url.toURI()).toFile())

		val result = genTarget.execute(Logger.getLogger("test"))
		val generatedFolder = new File(result.baseDirectory, "generated")
		val modelFolder = new File(generatedFolder, "src/main/java/org/openapitools/client/model")

		assertTrue(Files.list(modelFolder.toPath).noneMatch["Pet.java".equals(it.toFile.name)])
	}
}
