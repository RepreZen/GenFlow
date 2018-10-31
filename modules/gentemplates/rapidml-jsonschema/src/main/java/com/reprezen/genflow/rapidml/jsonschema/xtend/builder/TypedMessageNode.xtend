package com.reprezen.genflow.rapidml.jsonschema.xtend.builder

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.Iterators
import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.TypedRequest
import com.reprezen.rapidml.TypedResponse

class TypedMessageNode extends NamedSchemaNode<TypedMessage> {
	final NamedSchemaNode<?> delegate
	
	new(JsonSchemaNodeFactory director, TypedMessage element) {
		super(director, element)
		if (element.resourceType != null) {
			delegate = factory.createResourceNode(element.resourceType as ServiceDataResource)
		} else if (element.withDefaultRealization) {
			delegate = factory.createDefaultRealizationNode(element)
		} else {
			delegate = null
		}
	}
	
	override getName() {
		if (delegate != null) {
			return delegate.name
		}
		getMessageDefinitionName(element)
	}
	
	def static dispatch String getMessageDefinitionName(TypedRequest message) {
		return getMethodName(message) + "_request";
	}

	def static dispatch String getMessageDefinitionName(TypedResponse message) {
		return getMethodName(message) + "_response" + message.getStatusCode();
	}

	def static private String getMethodName(TypedMessage message) {
		val Method method = message.eContainer() as Method;
		val String methodName = method.getId();
		val String resourceName = method.getContainingResourceDefinition().getName();
		if (methodName != null) {
			return resourceName + "_" + methodName;
		}

		val allMethodNames = Iterators.filter(method.getContainingResourceDefinition().eAllContents(), typeof(Method)).
			filter[it.name == methodName].toList;
		val int sequence = allMethodNames.indexOf(method) + 1; // it's a 1-based integer
		val String httpVerb = method.getName();
		return resourceName + "_" + httpVerb + sequence;
	}

	override writeBody(ObjectNode bodyNode) {
		if (delegate != null) {
			delegate.writeBody(bodyNode)
			return
		}
		val body = if (element.resourceType instanceof CollectionResource) {
				bodyNode.put("type", "array")
				val items = bodyNode.putObject("items")
				items
			} else {
				bodyNode
			}
		setObjectAsType(body).putDescription(getDocumentation(element))
		bodyNode.addVendorExtensions(getRapidExtensions(element))
		val propertiesNode = body.putObject("properties")
		for (PropertyRealization includedProperty : element.properties.allIncludedProperties.filter [ e |
			!hasReferenceTreatment(element, e.baseProperty)
		]) {
			factory.createPropertyRealizationNode(includedProperty).write(propertiesNode)
		}
		for (ReferenceTreatment referenceTreatment : element.referenceTreatments) {
			factory.createReferenceTreatmentNode(referenceTreatment).inlineOrAddTopLevelNode(propertiesNode)
		}
		body.writeRequiredProperties(getRequiredPropertyNames(element))
	}

}
