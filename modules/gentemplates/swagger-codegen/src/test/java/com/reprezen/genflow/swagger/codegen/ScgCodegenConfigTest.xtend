package com.reprezen.genflow.swagger.codegen

import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.genflow.common.codegen.GenModuleWrapper.ScgModuleWrapper
import io.swagger.codegen.languages.JavaClientCodegen
import org.junit.Test

import static org.mockito.Mockito.*
import static org.junit.Assert.*

class ScgCodegenConfigTest {

	@Test
	def void testCodegenConfig() {
		val genTemplate = mock(ScgCodegenGenTemplate)
		when(genTemplate.generator).thenCallRealMethod

		val generator = new ScgCodegenGenTemplateBase.Generator(
			genTemplate,
			new FakeGenTemplateContext(#{
				"instantiationTypes" -> #{
					"array" -> "java.util.List"
				},
				"importMappings" -> #{
					"Foo" -> "com.my.Foo",
					"Bar" -> "com.my.Bar"
				},
				"typeMappings" -> #{
					"Foo" -> "com.my.Foo",
					"Bar" -> "com.my.Bar"
				}
			}),
			new ScgModuleWrapper(new JavaClientCodegen)
		)

		val config = generator.createCodeGenConfig

		assertTrue(config.config.importMapping.containsKey("Foo"))
		assertEquals("com.my.Foo", config.config.importMapping.get("Foo"))
		
		assertTrue(config.config.instantiationTypes.containsKey("array"))
		assertEquals("java.util.List", config.config.instantiationTypes.get("array"))
	}

}
