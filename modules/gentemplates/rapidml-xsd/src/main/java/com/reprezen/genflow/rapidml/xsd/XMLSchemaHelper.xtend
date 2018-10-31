/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd

import com.google.common.base.Splitter
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.reprezen.genflow.api.zenmodel.util.CommonServices
import com.reprezen.rapidml.Constraint
import com.reprezen.rapidml.DataModel
import com.reprezen.rapidml.Documentable
import com.reprezen.rapidml.Element
import com.reprezen.rapidml.Enumeration
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.LengthConstraint
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.PrimitiveType
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.ReferenceElement
import com.reprezen.rapidml.RegExConstraint
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.SingleValueType
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.UserDefinedType
import com.reprezen.rapidml.ValueRangeConstraint
import com.reprezen.rapidml.ZenModel
import com.reprezen.rapidml.xtext.util.ZenModelHelper
import java.util.Collection
import java.util.LinkedList
import java.util.List
import java.util.Set
import org.eclipse.emf.ecore.EObject

class XMLSchemaHelper {
	extension ZenModelHelper zenModelHelper
	extension ParamsHelper paramsHelper
	extension TraceHelper traceHelper

	new(Helpers helpers) {
		this.zenModelHelper = helpers.zenModelHelper
		this.paramsHelper = helpers.paramsHelper
		this.traceHelper = helpers.traceHelper
	}

	// TODO: Use Guava's 15 Escaper after migration 
	def String escapeXml(String source) {
		source.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").
			replaceAll("\'", "&apos;");
	}

	def <T extends EObject> T getEContainer(EObject ele, Class<T> type) {
		CommonServices::getContainerOfType(ele, type)
	}

	def <T> Collection<T> putAll(Iterable<T> iterable, Class<T> _class) {
		val Collection<T> result = new LinkedList<T>
		iterable.forEach[result.add(it)]
		return result
	}

	def ZenModel zenModel(EObject obj) {
		obj.getEContainer(ZenModel)
	}

	def dispatch String namespace(DataModel dataModel) {
		schemaRoot(dataModel.zenModel) + dataModel.name.toLowerCase()
	}

	def dispatch String namespace(ResourceAPI resourceAPI) {
		schemaRoot(resourceAPI.zenModel) + resourceAPI.name.toLowerCase()
	}

	def String schemaRoot(ZenModel zenModel) {
		val uriFragment = if (zenModel.namespace.nullOrEmpty) {
				'http://modelsolv.com/reprezen/schemas'
			} else {
				'http://' + reverseNamespace(zenModel.namespace)
			}
		uriFragment + "/" + zenModel.name.toLowerCase + "/"
	}

	def String reverseNamespace(String namespace) {
		val int idx = namespace.indexOf('.')
		if(idx == -1) namespace else Lists.reverse(Splitter.on('.').split(namespace).toList).join('.')
	}

	def nsPrefix(DataModel dataModel, ResourceAPI resourceAPI) {
		if (dataModel != null) {
			if (resourceAPI.zenModel == dataModel.zenModel)
				dataModel.name.toFirstLower
			else {
				val String alias = getAliasForDataModel(resourceAPI.zenModel, dataModel)
				if(Strings.isNullOrEmpty(alias)) dataModel.getShortestUniqueName(resourceAPI) else alias
			}
		}
	}

	def dispatch String nsPrefix(ResourceAPI resourceAPI) {
		if (resourceAPI != null)
			resourceAPI.name.toLowerCase + ""
	}

	def dispatch String nsPrefix(ServiceDataResource dataResource) {
		nsPrefix(dataResource.getInterface)
	}

	def xsdFileName(DataModel dataModel, ZenModel zenModel) {
		if (zenModel == dataModel.zenModel)
			dataModel.name.toFirstLower + ".xsd"
		else {
			val ZenModel model = dataModel.zenModel
			val String woNsName = model.name.toFirstLower + "-" + dataModel.name.toFirstLower + ".xsd"
			if (model.namespace.nullOrEmpty)
				woNsName
			else
				model.namespace.toFirstLower.replaceAll("\\.", "-") + "-" + woNsName
		}
	}

	def xsdFileName(ResourceAPI resourceAPI) {
		resourceAPI.name.toFirstLower + ".xsd"
	}

	def dispatch String typeName(TypedMessage message) {
		val name = switch typeNamingMethod {
			case SIMPLE_NAME: CommonServices.getMessageTypeName(message)
			case FULLY_QUALIFIED_NAME: message.fqName.toFirstUpper
		}
		message.typeNameInTrace = name
		return name
	}

	def dispatch String typeName(Structure structure) {
		val name = switch typeNamingMethod {
			case SIMPLE_NAME: structure.name.toFirstUpper
			case FULLY_QUALIFIED_NAME: structure.fqName.toFirstUpper
		}
		structure.typeNameInTrace = name
		return name
	}

	def dispatch String typeName(Enumeration enumeration) {
		val name = switch typeNamingMethod {
			case SIMPLE_NAME: enumeration.name // backward compatibility - no firstUpper
			case FULLY_QUALIFIED_NAME: enumeration.fqName.toFirstUpper
		}
		enumeration.typeNameInTrace = name
		return name
	}

	def dispatch String typeName(UserDefinedType type) {
		val name = switch typeNamingMethod {
			case SIMPLE_NAME: type.name // backward compatibility - no firstUpper
			case FULLY_QUALIFIED_NAME: type.fqName.toFirstUpper
		}
		type.typeNameInTrace = name
		return name
	}

	def dispatch String typeName(PrimitiveType type) {
		type.name
	}

	def dispatch String typeName(ServiceDataResource dataResource) {
		val name = switch typeNamingMethod {
			case SIMPLE_NAME: dataResource.name.toFirstUpper
			case FULLY_QUALIFIED_NAME: dataResource.fqName.toFirstUpper
		}
		dataResource.typeNameInTrace = name
		return name
	}

	def String extend(String baseName, List<? extends ReferenceElement> path) {
		'''«baseName»_«path.map[name].join("_")»'''
	}

	def dispatch getRootElementName(Structure structure) {
		val name = structure.typeName.toFirstLower
		structure.rootElementInTrace = name
		return name
	}

	def dispatch getRootElementName(ServiceDataResource resource) {
		val name = resource.typeName.toFirstLower
		resource.rootElementInTrace = name
		return name
	}

	def dispatch getRootElementName(TypedMessage message) {
		val name = message.typeName.toFirstLower
		message.rootElementInTrace = name
		return name
	}

	def getElementName(Element element) {
		val name = switch element {
			Feature: element.name
			ReferenceElement: element.name
		}.toFirstLower
		element.elementNameInTrace = name
		return name
	}

	def getElementName(PropertyRealization property) {
		property.baseProperty.elementName
	}

	def getAttributeName(Element element) {
		val name = switch element {
			Feature: element.name
			ReferenceElement: element.name
		}.toFirstLower
		element.attributeNameInTrace = name
		return name
	}

	def getAttributeName(PropertyRealization property) {
		property.baseProperty.attributeName
	}

	def dispatch String typeQName(TypedMessage message) {
		val resourceType = message.resourceType
		typeQName(toServiceDataResource(resourceType))
	}

	def dispatch String typeQName(ServiceDataResource dataResource) {
		if(dataResource == null) null else nsPrefix(dataResource) + ":" + typeName(dataResource)
	}

	def private toServiceDataResource(ResourceDefinition resource) {
		if(resource instanceof ServiceDataResource) resource else null
	}

	// FIXME support nested data types
	def protected DataModel getInterfaceDataModel(Structure complexType) {
		complexType.eContainer as DataModel
	}

	def getInterface(ServiceDataResource dataResource) {
		if(dataResource == null) null else dataResource.getEContainer(ResourceAPI)
	}

	def Iterable<SingleValueType> getUsedTypes(ZenModel zenModel) {
		CommonServices.getUsedSimpleTypes(zenModel)
	}

	def Iterable<SingleValueType> getUsedTypes(ResourceAPI resourceAPI) {
		CommonServices.getUsedSimpleTypes(resourceAPI)
	}

	def getAliasForDataModel(ZenModel zenModel, DataModel dataModel) {
		zenModel.getImportDeclaration(dataModel).alias
	}

	def getImportDeclaration(ZenModel zenModel, DataModel dataModel) {
		if (zenModel == dataModel.zenModel)
			null
		else
			zenModel.imports.findFirst [
				it.importedNamespace == dataModel.getFQN
			]
	}

	def getFQN(DataModel dataModel) {
		val ZenModel zenModel = dataModel.zenModel
		val String woNsName = zenModel.name + "." + dataModel.name
		if(Strings.isNullOrEmpty(zenModel.namespace)) woNsName else zenModel.namespace + "." + woNsName
	}

	def getShortestUniqueName(DataModel dataModel, ResourceAPI resourceAPI) {
		var String result
		var Set<DataModel> dataModels = resourceAPI.getUsedDataModels.filter [
			(
                    (resourceAPI.zenModel == it.zenModel) || resourceAPI.zenModel.getImportDeclaration(it).alias == null
                ) && (it.name == dataModel.name)
		].filter[it != dataModel].toSet

		// dataModels.remove(dataModel)
		if (dataModels.empty)
			result = dataModel.name
		else {
			if (!dataModels.exists[it.zenModel.name == dataModel.zenModel.name])
				result = dataModel.zenModel.name + "." + dataModel.name
			else
				result = dataModel.getFQN
		}

		return result
	}

	def getBaseTypeName(UserDefinedType userDefinedType) {
		val prefix = if(userDefinedType.baseType instanceof PrimitiveType) "xs:" else ""
		return prefix + userDefinedType.baseType.name
	}

	def dispatch Iterable<DataModel> getUsedDataModels(ResourceAPI resourceAPI) {
		resourceAPI.getUsedTypes.map[it.getEContainer(DataModel)].filter[it != null].toSet
	}

	def dispatch Iterable<DataModel> getUsedDataModels(ZenModel zenModel) {
		zenModel.resourceAPIs.map[it.usedDataModels].flatten.toSet
	}

	def getParentResourceDefinition(TypedMessage message) {
		message.getEContainer(Method).getEContainer(ResourceDefinition)
	}

	def String generateXSDDoc(Documentable doc) {
		val String docText = getDocumentation(doc)
		val String escapedDoc = docText.replaceAll("[\r\n]+", " ").replaceAll("[\n]+", " ").replaceAll("([\"\\\\])",
			"\\\\$1").trim()
		if (!escapedDoc.nullOrEmpty) {
			'''
				<xs:annotation>
					<xs:documentation>
						<!-- «escapedDoc» -->
					</xs:documentation>
				</xs:annotation>
			'''
		}
	}

	def generateRestriction(String baseTypeName, Iterable<Constraint> constraints) {
		'''
			<xs:restriction base="«baseTypeName»">
			«IF !constraints.nullOrEmpty»
				«FOR constraint : constraints SEPARATOR ""»
					«IF constraint instanceof LengthConstraint»
						«IF constraint.maxLength != 0 && constraint.maxLength == constraint.minLength»
							<xs:length value="«constraint.minLength»"/>
						«ELSE»
							«IF (constraint.minLength != 0)»
								<xs:minLength value="«constraint.minLength»"/>
							«ENDIF»
							«IF (constraint.maxLength != 0)»
								<xs:maxLength value="«constraint.maxLength»"/>
							«ENDIF»
						«ENDIF»
					«ENDIF»
					«IF constraint instanceof RegExConstraint»
						<xs:pattern value="«escapeXml(constraint.pattern)»"/>
					«ENDIF»
					«IF constraint instanceof ValueRangeConstraint»
						«IF constraint.minValueExclusive»
							<xs:minExclusive value="«constraint.minValue»"/>
						«ELSEIF constraint.minValue != null» 
							<xs:minInclusive value="«constraint.minValue»"/>
						«ENDIF»
						«IF constraint.maxValueExclusive»
							<xs:maxExclusive value="«constraint.maxValue»"/>
						«ELSEIF constraint.maxValue != null» 
							<xs:maxInclusive value="«constraint.maxValue»"/>
						«ENDIF»
					«ENDIF»
				«ENDFOR»
			«ENDIF»
			</xs:restriction>
		'''
	}
}
