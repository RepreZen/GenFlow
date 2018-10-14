package com.reprezen.genflow.openapi3.doc

import com.reprezen.jsonoverlay.JsonOverlay
import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.model3.Schema

class KaiZenParserHelper {
	def public <T> T asNullIfMissing(T el) {
		// FIXME nested elements are always initialized (non null), provide a better solution
		if ((el instanceof JsonOverlay<?>) && !Overlay.of(el as JsonOverlay<?>).isPresent) {
			return null
		}
		return el
	}

	def public String getKaiZenSchemaName(Schema schema) {
		return Overlay.of(schema).pathInParent
	}
}
