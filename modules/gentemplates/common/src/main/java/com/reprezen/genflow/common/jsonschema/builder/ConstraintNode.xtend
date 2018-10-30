package com.reprezen.genflow.common.jsonschema.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.reprezen.genflow.common.jsonschema.JacksonUtils
import com.reprezen.rapidml.Constraint
import com.reprezen.rapidml.LengthConstraint
import com.reprezen.rapidml.RegExConstraint
import com.reprezen.rapidml.ValueRangeConstraint
import java.text.NumberFormat

class ConstraintNode extends JsonSchemaNode<Constraint> {
	extension JacksonUtils = new JacksonUtils

	new(JsonSchemaNodeFactory factory, Constraint element) {
		super(factory, element)
	}

	override write(ObjectNode node) {

		switch element {
			LengthConstraint: {
				if (element.setMinLength) {
					node.put('minLength', element.minLength)
				}
				if (element.setMaxLength) {
					node.put('maxLength', element.maxLength)
				}
			}
			RegExConstraint: {
				if (element.pattern !== null) {
					node.put('pattern', '^' + element.pattern + '$')
				}
			}
			ValueRangeConstraint: {
				if (element.minValue !== null) {
					var String value = element.minValue
					try {
						val number = NumberFormat.getInstance().parse(value)
						node.putNumber('minimum', number)
					} catch (Exception e) {
						node.put('minimum', value)
					}
				}
				if (element.minValueExclusive) {
					node.put('exclusiveMinimum', element.minValueExclusive)
				}
				if (element.maxValue !== null) {
					node.put('maximum', element.maxValue)
					var String value = element.maxValue
					try {
						val number = NumberFormat.getInstance().parse(value)
						node.putNumber('maximum', number)
					} catch (Exception e) {
						node.put('maximum', value)
					}
				}
				if (element.maxValueExclusive) {
					node.put('exclusiveMaximum', element.maxValueExclusive)
				}
			}
		}

	}

}
