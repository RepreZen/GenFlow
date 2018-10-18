package com.reprezen.genflow.swagger.doc

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.List
import java.util.Map

class ExamplesHelper implements Helper {

	extension HtmlHelper htmlHelper

	override init() {
		htmlHelper = HelperHelper.htmlHelper
	}

	def <T> renderExamples(Map<String, T> examples) {
		if (examples !== null && !examples.empty) {
			'''
				<h4>Examples</h4>
				<dl>
				    «FOR key : examples.keySet»
				    	<dt>«key»</dt>
				    	<dd>
				    	    <pre class="remove-xtend-indent">
				    	    «examples.get(key)?.exampleText?.htmlEscape»
				    	    </pre>
				    	</dd>
				    «ENDFOR»
				</dl>
			'''
		}
	}

	def String getExampleText(Object value) {
		switch value {
			Map<?,?>:
				new ObjectMapper().writerWithDefaultPrettyPrinter.writeValueAsString(value)
			List<?>:
				new ObjectMapper().writerWithDefaultPrettyPrinter.writeValueAsString(value)
			default:
				value.toString
		}
	}

}
