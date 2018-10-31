package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.common.xtend.XImportHelper
import com.reprezen.rapidml.UserDefinedType
import com.reprezen.rapidml.datatypes.util.ConstraintsUtils

class UserDefinedTypeNode extends TypedNode<UserDefinedType> {
	final XImportHelper importHelper

	new(JsonSchemaNodeFactory director, UserDefinedType element, XImportHelper importHelper) {
		super(director, element)
		this.importHelper = importHelper
	}
	
	override write(ObjectNode parentNode) {
		writeBody(parentNode)
		return parentNode
	}

	override writeBody(ObjectNode body) {
		if (!ConstraintsUtils.isCircularBaseTypeReference(element)) {
			body.putDescription(getDocumentation(element))
			writePropertyType(body, element.baseType)
			for (constraint : element.allConstraints) {
				factory.createConstraintNode(constraint).write(body)
			}
		}
	}

	override getName() {
		importHelper.getQualifiedName(element)
	}

}
