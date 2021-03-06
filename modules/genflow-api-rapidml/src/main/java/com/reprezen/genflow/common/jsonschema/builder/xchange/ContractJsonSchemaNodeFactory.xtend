package com.reprezen.genflow.common.jsonschema.builder.xchange

import com.reprezen.rapidml.RealizationContainer
import com.reprezen.rapidml.ReferenceLink

class ContractJsonSchemaNodeFactory extends XChangeJsonSchemaNodeFactory {
	new() {
		super()
	}

	override createDefaultRealizationNode(RealizationContainer element) {
		return new DefaultRealizationContainerNode(this, element);
	}

	override createDefaultLinkNode(ReferenceLink element) {
		return new DefaultLinkNode(this, element)
	}

}
