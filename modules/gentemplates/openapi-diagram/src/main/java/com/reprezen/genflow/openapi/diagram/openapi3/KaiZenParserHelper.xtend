package com.reprezen.genflow.openapi.diagram.openapi3

import com.reprezen.genflow.api.normal.openapi.RepreZenVendorExtension
import com.reprezen.jsonoverlay.IJsonOverlay
import com.reprezen.jsonoverlay.JsonOverlay
import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.model3.Schema
import java.util.Map

class KaiZenParserHelper {

	def <T extends JsonOverlay<?>> T asNullIfMissing(T el) {
		if (!Overlay.of(el as IJsonOverlay<?>).isPresent) {
			return null
		}
		return el
	}

	def String getKaiZenSchemaName(Schema schema) {
		Overlay.of(schema).pathInParent
	}
	
	def getSchemaTitle(Schema schema) {
		#[schema.getKaiZenSchemaName, schema.title, schema.rzveTypeName, "UNKNOWN"].filter[it !== null].head
	}

	def getRZVE(Schema schema) {
		schema.getExtension(RepreZenVendorExtension.EXTENSION_NAME) as Map<String, Object>
	}

	def getRzveTypeName(Schema schema) {
		schema.getRZVE?.get("typeName") as String
	}
}
