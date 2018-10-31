package com.reprezen.genflow.rapidml.csharp.helpers

import com.reprezen.rapidml.Documentable

class DocHelper {
	def static simpleDoc(Documentable item) {
		item.documentation?.text?.simpleDoc
	}

	def static simpleDoc(String doc) {
		val lines = doc.split('\n')
		if (lines.size == 1) {
			'''/// <summary>«lines.get(0)»</summary>
			'''
		} else {
			'''
				/// <summary>
				«FOR line : lines»
					/// «line»
				«ENDFOR»
				/// </summary>
			'''
		}
	}
}
