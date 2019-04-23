package com.reprezen.genflow.openapi.normalizer

import com.reprezen.genflow.api.normal.openapi.Option
import java.util.Map
import org.junit.Test

import static org.junit.Assert.*

class NormalizerParametersTest {
	
	@Test
	def void testRetain_All() {
		val Map<String, Object> parameters = #{"RETAIN" -> "ALL"}

		val params = new NormalizerParameters(parameters)

		// Contains options RETAIN and DEFER_EXTENSION_DATA_REMOVAL
		assertEquals(2, params.options.size)

		val option = params.options.get(0)
		assertEquals(Option.RETAIN_ALL.data, option.data)
	}

	@Test
	def void testRetain_Components() {
		val Map<String, Object> parameters = #{"RETAIN" -> "COMPONENTS"}

		val params = new NormalizerParameters(parameters)

		// Contains options RETAIN and DEFER_EXTENSION_DATA_REMOVAL
		assertEquals(2, params.options.size)

		val option = params.options.get(0)
		assertEquals(Option.RETAIN_COMPONENTS.data, option.data)
	}

	@Test
	def void testRetain_Paths() {
		val Map<String, Object> parameters = #{"RETAIN" -> "PATH"}

		val params = new NormalizerParameters(parameters)

		// Contains options RETAIN and DEFER_EXTENSION_DATA_REMOVAL
		assertEquals(2, params.options.size)

		val option = params.options.get(0)
		// Expected value should be put in a collection here to pass test
		// as NormalizerParameters always puts option data in collections
		assertEquals(#{(Option.RETAIN_PATHS.data)}, option.data)
	}

	@Test
	def void testRetain_PathOrComponent() {
		val Map<String, Object> parameters = #{"RETAIN" -> "PATH_OR_COMPONENTS"}

		val params = new NormalizerParameters(parameters)

		// Contains options RETAIN and DEFER_EXTENSION_DATA_REMOVAL
		assertEquals(2, params.options.size)

		val option = params.options.get(0)
		assertEquals(Option.RETAIN_PATHS_OR_COMPONENTS.data, option.data)
	}
}
