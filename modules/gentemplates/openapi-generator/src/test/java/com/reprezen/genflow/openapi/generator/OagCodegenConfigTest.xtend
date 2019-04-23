package com.reprezen.genflow.openapi.generator

import com.reprezen.genflow.api.template.FakeGenTemplateContext
import org.junit.Test
import org.openapitools.codegen.languages.JavaClientCodegen

import static org.junit.Assert.*
import static org.mockito.Mockito.*

class OagCodegenConfigTest {

	@Test
	def void testCodegenConfig() {
		val genTemplate = mock(OagCodegenGenTemplate)
		when(genTemplate.generator).thenCallRealMethod

		val generator = new OagCodegenGenTemplateBase.Generator(
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
			new OagModuleWrapper(new JavaClientCodegen)
		)

		val config = generator.createCodeGenConfig

		assertTrue(config.config.importMapping.containsKey("Foo"))
		assertEquals("com.my.Foo", config.config.importMapping.get("Foo"))
		
		assertTrue(config.config.instantiationTypes.containsKey("array"))
		assertEquals("java.util.List", config.config.instantiationTypes.get("array"))
	}

}
