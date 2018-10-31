/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.api.zenmodel.ZenModelExtractOutputItem
import com.reprezen.genflow.api.zenmodel.ZenModelLocator
import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.ObjectRealization
import com.reprezen.rapidml.ObjectResource
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.ReferenceElement
import com.reprezen.rapidml.ReferenceEmbed
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.ZenModel
import com.reprezen.rapidml.util.ResourceFinder
import java.util.Collection
import java.util.List
import org.eclipse.emf.ecore.EObject

class ResourceApiSchemaGenerator extends ZenModelExtractOutputItem<ResourceAPI> {
	extension FeatureHelper featureHelper
	extension ResourceHelper resourceHelper
	extension ReferenceLinkHelper referenceLinkHelper
	extension XMLSchemaHelper xmlSchemaHelper
	extension ParamsHelper paramsHelper
	extension TraceHelper traceHelper
	var ZenModelLocator locator

	private ResourceFinder resourceFinder

	override void init(IGenTemplateContext context) {
		super.init(context)
	}

	override String generate(ZenModel zenModel, ResourceAPI api) {
		val helpers = new Helpers(context, zenModel)
		this.featureHelper = helpers.featureHelper
		this.resourceHelper = helpers.resourceHelper
		this.referenceLinkHelper = helpers.referecneLinkHelper
		this.xmlSchemaHelper = helpers.xmlSchemaHelper
		this.paramsHelper = helpers.paramsHelper
		this.traceHelper = helpers.traceHelper

		this.locator = new ZenModelLocator(zenModel)
		this.resourceFinder = new ResourceFinder(api)
		api.traceForApi //
		.withProperty('namespace', api.namespace) //
		.withProperty('namespacePrefix', api.nsPrefix) //
		.withPrimarySourceItem(locator.locate(api)) //
		'''
			<xs:schema
				targetNamespace="«api.namespace»"
				elementFormDefault="qualified"
				xmlns="«api.namespace»"
				xmlns:tns="«api.namespace»"
				«FOR dm : api.usedDataModels SEPARATOR ""»
					xmlns:«dm.nsPrefix(api)»="«dm.namespace»"
				«ENDFOR»
				xmlns:xs="http://www.w3.org/2001/XMLSchema"
				xmlns:xml="http://www.w3.org/XML/1998/namespace"
				«api.generateNamespaceAdditions»
			>
			«api.generateSchemaImports»
			«FOR dm : api.usedDataModels SEPARATOR ""»
				<xs:import namespace="«dm.namespace»" schemaLocation="«dm.xsdFileName(api.zenModel)»"/>
			«ENDFOR»
			<!-- ELEMENT AND COMPLEX TYPE DECLARATIONS FOR RESOURCE DEFINITIONS -->
			«FOR resource : api.ownedResourceDefinitions SEPARATOR ""»
				«generateForResource(resource as ServiceDataResource, api)»
			«ENDFOR»
			<!-- ELEMENT AND COMPLEX TYPE DECLARATIONS FOR MESSAGE BODY DEFINITIONS -->
			«FOR resource : api.ownedResourceDefinitions SEPARATOR ""»
				«FOR method : resource.methods SEPARATOR ""»
					««« Make sure each method has an id value in trace
					«{method.setIdInTrace; null}»
					«method.request?.generateForMessage»
					«FOR response : method.responses SEPARATOR ""»
						«response.generateForMessage»
					«ENDFOR»
				«ENDFOR»
			«ENDFOR»
			«FOR dataType : api.zenModel.dataModels.map[it.ownedDataTypes.filter(Structure)].flatten SEPARATOR ""»
				«dataType.generateForStructure(api)»
			«ENDFOR»
			</xs:schema>
		'''
	}

	def private generateNamespaceAdditions(ResourceAPI api) {
		'xmlns:atom="http://www.w3.org/2005/Atom"'
	}

	def private String generateSchemaImports(ResourceAPI api) {
		api.generateAtomImports
	}

//
// Per-resource generation
//
	def private generateForResource(ServiceDataResource resource, ResourceAPI api) {
		resource.traceForComplexType //
		.withProperty('complexType', resource.typeName) //
		.withProperty('elementName', resource.rootElementName) //
		.withProperty('namespace', api.namespace) //
		.withProperty('namespacePrefix', api.nsPrefix) //
		.withPrimarySourceItem(locator.locate(api)) //
		.withPrimarySourceItem(locator.locate(resource))
		'''
			«resource.dataType.generateResourceComplexType(resource, api)»
			«resource.generateResourceElement»
			«resource.generateTransitionalContainersForReferenceLinks(api)»
			«generateContainersForReferenceEmbeds(resource.name, resource, resource.referenceTreatments, api)»
		'''
	}

	def private dispatch generateResourceComplexType(Structure complexType, CollectionResource resource,
		ResourceAPI api) {
		val elementProperties = Iterables.<EObject>concat(
			resource.referenceLinks,
			resource.referenceEmbeds
		)
		'''
			<xs:complexType name="«resource.typeName»">
			«complexType.generateXSDDoc»
			«elementProperties.generateAllBlock(resource, api)»
			</xs:complexType>
		'''
	}

	def private dispatch generateResourceComplexType(Structure complexType, ObjectResource resource, ResourceAPI api) {
		val elementProperties = Iterables.concat(
			resource.includedProperties.filterForElements,
			resource.referenceLinks,
			resource.referenceEmbeds
		)
		val attributeProperties = resource.includedProperties.filterForAttributes
		'''
			<xs:complexType name="«resource.typeName»">
				«complexType.generateXSDDoc»
				«elementProperties.generateAllBlock(resource, api)»
				«attributeProperties.generateAttributeDecls(api)»
			</xs:complexType>
		'''
	}

	def private generateAllBlock(Iterable<EObject> properties, EObject context, ResourceAPI api) {
		generateAllBlock(properties, context, api, null)
	}

	def private generateAllBlock(Iterable<EObject> properties, EObject context, ResourceAPI api,
		(EObject)=>String special) {
		if (!properties.empty) {
			'''
				<xs:all>
				«FOR prop : properties SEPARATOR ""»
					«special?.apply(prop) ?: generatePropertyDecl(prop, context, api, true)»
				«ENDFOR»
				</xs:all>
			'''
		}
	}

	def private generateResourceElement(ServiceDataResource resource) {
		'''
			<xs:element name="«resource.rootElementName»" type="«resource.typeName»" />
		'''
	}

	def private generateTransitionalContainersForReferenceLinks(ServiceDataResource resource, ResourceAPI api) {
		val links = resource.referenceLinks.map[it as ReferenceTreatment]
		'''
			«FOR referenceProperty : getContainmentReferencesAtPosition(links, 1)»
				«generateContainmentSegment(referenceProperty, resource, api)»
			«ENDFOR»
		'''
	}

	def private generateContainmentSegment(ReferenceProperty referenceProperty, ServiceDataResource resource,
		ResourceAPI api) {
		generateContainmentSegment(#[referenceProperty], resource, api)
	}

	def private String generateContainmentSegment(List<ReferenceProperty> path, ServiceDataResource resource,
		ResourceAPI api) {
		val currentType = path.findLast[].type
		val levelOfContainment = path.length
		val elementProperties = Iterables.concat(
			currentType.ownedFeatures.filterForElements,
			resource.referenceLinks.filter[startsWithPath(path)].filter[containmentDepth < levelOfContainment + 1]
		)
		val attributeProperties = currentType.ownedFeatures.filterForAttributes
		val nextLevelReferences = resource.referenceLinks.map[it as ReferenceTreatment]
		'''
			<xs:complexType name="«resource.typeName.extend(path)»">
				«elementProperties.generateAllBlock(resource, api)[
					switch it {
						ReferenceProperty:
							it.generateReferenceProperty(resource, api, path)
						default:
							null
					}
				]»
				«attributeProperties.generateAttributeDecls(api)»
			</xs:complexType>
			«FOR referenceProperty : getContainmentReferencesAtPosition(nextLevelReferences, levelOfContainment + 1)»
				«generateContainmentSegment(path.concat(referenceProperty), resource, api)»
			«ENDFOR»
		'''
	}

//
// Per-Message Generation
//
	def private generateForMessage(TypedMessage message) {
		if (message.actualType != null) {
			val api = message.resourceAPI
			'''
				«message.actualType.generateMessageComplexType(message, api)»
				«message.generateMessageTypeElement»
				«message.generateTransitionalContainersForReferenceLinks(api)»
				«generateContainersForReferenceEmbeds(message.typeName, message, message.referenceTreatments, api)»
			'''
		}
	}

	def private generateMessageComplexType(Structure complexType, TypedMessage message, ResourceAPI api) {
		val elementProperties = Iterables.concat(
			message.includedProperties.filterForElements,
			message.referenceLinks,
			message.referenceEmbeds
		)
		val attributeProperties = message.includedProperties.filterForAttributes
		'''
			<xs:complexType name="«message.typeName»">
			«complexType.generateXSDDoc»
			«elementProperties.generateAllBlock(message, api)[
				switch it {
					ReferenceEmbed: {
						val nextReference = it.referenceElement as ReferenceProperty
						nextReference.generateNestedReference(extend(message.typeName, #[nextReference]))
					}
					default:
						null
				}
			]»
			«attributeProperties.generateAttributeDecls(api)»
			</xs:complexType>
		'''
	}

	def private generateMessageTypeElement(TypedMessage message) {
		'''
			<xs:element name="«message.rootElementName»" type="«message.typeName»" />
		'''
	}

	def private generateTransitionalContainersForReferenceLinks(TypedMessage message,
		ResourceAPI api) {
		'''
			«FOR referenceProperty : getContainmentReferencesAtPosition(message.referenceLinks.map[it as ReferenceTreatment], 1)»
				«generateContainmentSegment(referenceProperty, message, api)»
			«ENDFOR»
		'''
	}

	def private generateContainmentSegment(ReferenceProperty referenceProperty, TypedMessage message, ResourceAPI api) {
		generateContainmentSegment(#[referenceProperty], message, api)
	}

	def private String generateContainmentSegment(List<ReferenceProperty> path, TypedMessage message, ResourceAPI api) {
		val currentType = path.findLast[].type;
		val levelOfContainment = path.length
		val elementProperties = Iterables.concat(
			currentType.ownedFeatures.filterForElements,
			message.referenceLinks.filter[startsWithPath(path)].filter[containmentDepth < levelOfContainment + 1]
		)
		val attributeProperties = currentType.ownedFeatures.
			filterForAttributes
		'''
			<xs:complexType name="message.typeName.extend(path)»">
			«elementProperties.generateAllBlock(message, api)»
			«attributeProperties.generateAttributeDecls(api)»
			</xs:complexType>
			«FOR referenceProperty : getContainmentReferencesAtPosition(message.referenceLinks.map[it as ReferenceTreatment], levelOfContainment + 1)»
				«generateContainmentSegment(path.concat(referenceProperty), message, api)»
			«ENDFOR»
		'''
	}

//
// ReferenceEmbed generation - for both Resources and Messages
//
	def private generateContainersForReferenceEmbeds(String name, EObject obj,
		Iterable<ReferenceTreatment> referenceTreatments, ResourceAPI api) {
		'''
			«FOR referenceEmbed : referenceTreatments.filter(ReferenceEmbed)»
				«referenceEmbed.generateContainersForReferenceEmbed(name, obj, api)»
			«ENDFOR»
		'''
	}

	def private String generateContainersForReferenceEmbed(ReferenceEmbed referenceEmbed, String name, EObject obj,
		ResourceAPI api) {
		val path = getPathTo(referenceEmbed)
		'''
			«path.generateEmbedSegment(name, obj, referenceEmbed)»
			«FOR childReferenceEmbed : referenceEmbed.nestedReferenceTreatments.filter(ReferenceEmbed)»
				«childReferenceEmbed.generateContainersForReferenceEmbed(name, obj, api)»
			«ENDFOR»
		'''
	}

	def private List<ReferenceProperty> getPathTo(ReferenceEmbed referenceEmbed) {
		val Collection<ReferenceProperty> containers = Lists.newArrayList
		val Collection<ReferenceProperty> references = Lists.newArrayList
		references.addAll(referenceEmbed.containmentReferences)
		if (referenceEmbed.eContainer.eContainer instanceof ReferenceEmbed) {
			containers.addAll(getPathTo(referenceEmbed.eContainer.eContainer as ReferenceEmbed))
		}
		references.add(referenceEmbed.referenceElement as ReferenceProperty)
		Iterables.concat(containers, references).toList
	}

	def private generateEmbedSegment(List<ReferenceProperty> path, String baseName, EObject obj,
		ReferenceEmbed referenceEmbed) {
		val elementProperties = Iterables.concat(
			referenceEmbed.linkDescriptor.allIncludedProperties.map[baseProperty].filterForElements,
			referenceEmbed.nestedReferenceTreatments.filter(ReferenceLink),
			referenceEmbed.nestedReferenceTreatments.filter(ReferenceEmbed)
		)
		val attributeProperties = referenceEmbed.linkDescriptor.allIncludedProperties.map[baseProperty].
			filterForAttributes
		'''
			<xs:complexType name="«baseName.extend(path)»">
			«IF referenceEmbed.linkDescriptor != null»
				«elementProperties.generateAllBlock(obj, obj.resourceAPI)[
					switch it {
						ReferenceEmbed: {
							val nextSegment = it.referenceElement as ReferenceProperty
							nextSegment.generateNestedReference(extend(baseName, path.concat(nextSegment)))
						}
						default:
							null							
					}
				]»
				«attributeProperties.generateAttributeDecls(obj.resourceAPI)»
			«ENDIF»
			</xs:complexType>
		'''
	}

//
// Generators for properties in within complex types
//
	def private dispatch String generatePropertyDecl(EObject prop, EObject context, ResourceAPI api,
		boolean asElement) {
		throw new IllegalArgumentException('''Cannot gernerate property decl for «prop?.class» in context «context?.class»''')
	}

	def private dispatch String generatePropertyDecl(PrimitiveProperty prop, EObject context, ResourceAPI api,
		boolean asElement) {
		if (asElement) {
			if (prop.
				isMultiValued) {
				'''
					<xs:element name="«prop.listName»" minOccurs="«prop.listMinOccurs»" maxOccurs="1">
						<xs:complexType>
							<xs:sequence>
								<xs:element name="«prop.itemName»" type="«prop.getTypeName(api)»" minOccurs="«prop.listItemMinOccurs»" maxOccurs="«prop.listItemMaxOccurs»" />
							</xs:sequence>
						</xs:complexType>
					</xs:element>
				'''
			} else {
				'''<xs:element name="«prop.elementName»" type="«prop.getTypeName(api)»" minOccurs="«prop.minOccurs»" maxOccurs="1"/>'''
			}
		} else {
			if (prop.isMultiValued) {
				throw new IllegalArgumentException("Cannot declare multi-valued property as a schema attribute");
			}
			'''
				<xs:attribute name="«prop.attributeName»" type="«prop.getTypeName(api)»" use="optional" />
			'''
		}
	}

	def private dispatch String generatePropertyDecl(PropertyRealization property, EObject context, ResourceAPI api,
		boolean asElement) {
		switch (property.baseProperty) {
			PrimitiveProperty:
				if (asElement) {
					if (property.
						isMultiValued) {
						'''
							<xs:element name="«property.listName»" minOccurs="«property.listMinOccurs»" maxOccurs="1">
								<xs:complexType>
									<xs:sequence>
										<xs:element name="«property.itemName»" type="«property.getTypeName(api)»" minOccurs="«property.listItemMinOccurs»" maxOccurs="«property.listItemMaxOccurs»" />
									</xs:sequence>
								</xs:complexType>
							</xs:element>
						'''
					} else {
						'''<xs:element name="«property.elementName»" type="«property.getTypeName(api)»" minOccurs="«property.minOccurs»" maxOccurs="1"/>'''
					}
				} else {
					if (property.isMultiValued) {
						throw new IllegalArgumentException("Cannot declare multi-valued property as a schema attribute")
					}
					'''
						<xs:attribute name = "«property.attributeName»"
						«IF property.allConstraints.nullOrEmpty»
							type="«property.getTypeName(api)»"
						«ENDIF»
						use="«property.propertyUse»">
						«IF !property.allConstraints.nullOrEmpty»
							<xs:simpleType>
								«property.basePrimitiveProperty.getTypeName(api).generateRestriction(property.allConstraints)»
							</xs:simpleType>
						«ENDIF»
						</xs:attribute>
					'''
				}
			ReferenceProperty:
				property.generateReferenceProperty(context, api, #[])
		}
	}

	def private dispatch String generatePropertyDecl(ReferenceLink link, EObject context, ResourceAPI api,
		boolean asElement) {
		val ReferenceElement referenceProperty = link.referenceProperty
		val ObjectRealization linkDescriptor = link.linkDescriptor
		val elementProperties = Iterables.<EObject>concat(
			#[link],
			linkDescriptor?.allIncludedProperties?.map[baseProperty]?.primitiveProperties?.filterForElements ?: #[]
		)
		val attributeProperties = linkDescriptor?.allIncludedProperties?.map[baseProperty]?.primitiveProperties?.
			filterForAttributes ?: #[]
		if (referenceProperty.
			isMultiValued) {
			'''
				<xs:element name="«referenceProperty.listName»" minOccurs="«referenceProperty.listMinOccurs»" maxOccurs="1">
					<xs:complexType>
						<xs:sequence>
							<xs:element name="«referenceProperty.itemName»" minOccurs="«referenceProperty.listItemMinOccurs»" maxOccurs="«referenceProperty.listItemMaxOccurs»">
								<xs:complexType>
										«elementProperties.generateAllBlock(context, api) [
										switch it {
											ReferenceLink: it.generateAtomLink
											default: null
										}
									]»
									«attributeProperties.generateAttributeDecls(api)»
								</xs:complexType>
							</xs:element>
						</xs:sequence>
					</xs:complexType>
				</xs:element>
			'''

		} else {
			'''
				<xs:element name="«referenceProperty.elementName»" minOccurs="«referenceProperty.minOccurs»" maxOccurs="«referenceProperty.maxOccurs»">
					<xs:complexType>
						«elementProperties.generateAllBlock(context, api)[
							switch it {
								ReferenceLink: it.generateAtomLink
								default: null
							}
						]»
						«attributeProperties.generateAttributeDecls(api)»
					</xs:complexType>
				</xs:element>
			'''
		}
	}

	def private dispatch String generatePropertyDecl(ReferenceEmbed embed, EObject context, ResourceAPI api,
		boolean asElement) {
		val baseName = switch context {
			TypedMessage: context.typeName
			ServiceDataResource: context.name
		}
		val complexTypeName = baseName.extend(#[embed.referenceElement])
		generateNestedReference(embed.referenceElement, complexTypeName)
	}

	def private dispatch String generatePropertyDecl(ReferenceProperty prop, EObject context, ResourceAPI api,
		boolean asElement) {
		prop.generateReferenceProperty(context, api, #[])
	}

	def private dispatch String generateReferenceProperty(PropertyRealization referenceProperty,
		ServiceDataResource resource, ResourceAPI api, List<ReferenceProperty> path) {
		if (referenceProperty.baseReferenceProperty.isPropertyOverridenByReferenceLink(resource, path)) {
			referenceProperty.generateContainmentProperty(
				resource.typeName.extend(path.concat(referenceProperty.baseReferenceProperty)))
		}
	}

	def private dispatch String generateReferenceProperty(ReferenceProperty referenceProperty,
		ServiceDataResource resource, ResourceAPI api, List<ReferenceProperty> path) {
		if (isPropertyOverridenByReferenceLink(referenceProperty, resource, path)) {
			referenceProperty.generateNestedReference(resource.typeName.extend(path.concat(referenceProperty)))
		}
	}

	def private dispatch generateReferenceProperty(ReferenceProperty referenceProperty, TypedMessage message,
		ResourceAPI api, List<ReferenceProperty> path) {
		if (isPropertyOverridenByReferenceLink(referenceProperty, message, path)) {
			referenceProperty.generateNestedReference(message.typeName.extend(path.concat(referenceProperty)))
		}
	}

	//
	// Nested references
	//
	def private String generateNestedReference(ReferenceElement referenceElement, String typeName) {
		if (!referenceElement.isMultiValued)
			referenceElement.generateSimpleNestedReference(typeName)
		else
			referenceElement.generateMultiNestedReference(typeName)
	}

	def private String generateSimpleNestedReference(ReferenceElement referenceElement,
		String typeName) {
		'''
			<xs:element name="«referenceElement.elementName»" type="«typeName»" minOccurs="«referenceElement.minOccurs»" maxOccurs="«referenceElement.maxOccurs»" />
		'''
	}

	def private String generateMultiNestedReference(ReferenceElement referenceElement,
		String typeName) {
		'''
			<xs:element name="«referenceElement.listName»" minOccurs="«referenceElement.listMinOccurs»" maxOccurs="1">
				<xs:complexType>
					<xs:sequence>
						<xs:element name="«referenceElement.itemName»" type="«typeName»" minOccurs="«referenceElement.listItemMinOccurs»" maxOccurs="«referenceElement.listItemMaxOccurs»" />
					</xs:sequence>
				</xs:complexType>
			</xs:element>
		'''
	}

	//
	// Containment
	//
	def private String generateContainmentProperty(PropertyRealization property, String typeName) {
		if (!property.isMultiValued) {
			property.generateSimpleContainmentProperty(typeName)
		} else {
			property.generateMultiContainmentProperty(typeName)
		}
	}

	def private String generateSimpleContainmentProperty(PropertyRealization property,
		String typeName) {
		'''
			<xs:element name="«property.elementName»" type="«typeName»" minOccurs="«property.minOccurs»" maxOccurs="«property.maxOccurs»" />
		'''
	}

	def private String generateMultiContainmentProperty(PropertyRealization property,
		String typeName) {
		'''
			<xs:element name="«property.listName»" minOccurs="«property.listMinOccurs»" maxOccurs="1">
				<xs:complexType>
					<xs:sequence>
						<xs:element name="«property.itemName»" type="«typeName»" minOccurs="«property.listItemMinOccurs»" maxOccurs="«property.listItemMaxOccurs»" />
					</xs:sequence>
				</xs:complexType>
			</xs:element>
		'''
	}

	//
	// Miscellaneous
	//
	def private String generateAtomImports(ResourceAPI api) {
		'''
			<xs:import namespace="http://www.w3.org/2005/Atom" schemaLocation="atom.xsd" />
		'''
	}

	def private String generateAtomLink(ReferenceLink referenceLink) {
		if (referenceLink.targetResource != null) '''
			<!-- A reference link to the «referenceLink.targetResource.name»-->
			<!--Recommended value of the 'rel' attribute is '«referenceLink.relValue»'-->
			<xs:element ref="atom:link" minOccurs="1" maxOccurs="1" />
		'''
	}

	def private dispatch String generateReferenceProperty(PropertyRealization referenceProperty, TypedMessage message,
		ResourceAPI api, List<ReferenceProperty> path) {
		if (referenceProperty.baseReferenceProperty.isPropertyOverridenByReferenceLink(message, path)) {
			generateContainmentProperty(referenceProperty,
				message.typeName.extend(path.concat(referenceProperty.baseReferenceProperty)))
		}
	}

	def private getPropertyUse(PropertyRealization property) {
		if(property.isRequired) 'required' else 'optional'
	}

	def private generateForStructure(Structure structure, ResourceAPI api) {
		if (resourceFinder.findResource(structure, true) == null) {
			val elementProperties = structure.ownedFeatures.filter[isPrimitiveProperty].filterForElements
			val attrProperties = structure.ownedFeatures.filterForAttributes
			'''
				<xs:complexType name="«structure.typeName»">
					«structure.generateXSDDoc»
					«elementProperties.generateAllBlock(structure, api)»
					«attrProperties.generateAttributeDecls(api)»
				</xs:complexType>
			'''
		}
	}

	def private dispatch String generateAttributeDecls(ReferenceEmbed embed, ResourceAPI api) {
		embed.linkDescriptor.generateAttributeDecls(api);
	}

	def private dispatch String generateAttributeDecls(ObjectRealization objectRealization, ResourceAPI api) {
		objectRealization.allIncludedProperties.primitiveSingleProperties.generateAttributeDecls(api)
	}

	def private dispatch String generateAttributeDecls(Iterable<EObject> properties, ResourceAPI api) {
		'''
			«FOR property : properties SEPARATOR ""»
				«property.generatePropertyDecl(api, api, false)»
			«ENDFOR»
		'''
	}

	def private ReferenceProperty getBaseReferenceProperty(PropertyRealization property) {
		property.baseProperty as ReferenceProperty
	}

	def private PrimitiveProperty getBasePrimitiveProperty(PropertyRealization property) {
		property.baseProperty as PrimitiveProperty
	}

	def private dispatch ResourceAPI getResourceAPI(TypedMessage message) {
		message.getEContainer(Method).containingResourceDefinition.resourceAPI
	}

	def private dispatch ResourceAPI getResourceAPI(ResourceDefinition resource) {
		resource.eContainer as ResourceAPI
	}

	def private <T> List<T> concat(List<T> list, T value) {
		val copy = Lists.newArrayList(list)
		copy.add(value)
		return copy
	}

	def private <T extends EObject> Iterable<EObject> filterForElements(Iterable<T> properties) {
		properties.filter[eltStyle || !it.isAttributeEligible].map[it as EObject]
	}

	def private <T extends EObject> Iterable<EObject> filterForAttributes(Iterable<T> properties) {
		properties.filter[attrStyle && it.isAttributeEligible].map[it as EObject]
	}

	def private boolean isAttributeEligible(EObject property) {
		switch property {
			Feature: property.isPrimitiveProperty && property.isSingleValued
			PropertyRealization: property.baseProperty.isPrimitiveProperty && property.isSingleValued
			default: throw new IllegalArgumentException
		}
	}
}
