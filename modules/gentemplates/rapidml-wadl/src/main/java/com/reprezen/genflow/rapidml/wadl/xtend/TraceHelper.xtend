package com.reprezen.genflow.rapidml.wadl.xtend

import com.google.common.collect.Maps
import com.reprezen.genflow.api.source.ILocator
import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.api.trace.GenTemplateTrace
import com.reprezen.genflow.api.trace.GenTemplateTraceItem
import com.reprezen.genflow.api.trace.GenTemplateTraceUtils
import com.reprezen.genflow.api.zenmodel.ZenModelLocator
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ZenModel
import java.nio.file.Paths
import java.util.Map
import org.eclipse.emf.ecore.EObject

class TraceHelper {
	val IGenTemplateContext context
	val ILocator<ZenModel> locator
	val GenTemplateTrace xsdTrace
	val Map<String, GenTemplateTraceItem> namesItems = Maps.newHashMap
	val Map<ResourceAPI, GenTemplateTraceItem> apiItems = Maps.newHashMap

	new(IGenTemplateContext context, ZenModel model) {
		this.context = context
		this.locator = new ZenModelLocator(model)
		this.xsdTrace = context.getPrerequisiteTrace('xsdGenerator')
		for (item : GenTemplateTraceUtils::getTraceItemsOfType(xsdTrace, 'namesInSchema')) {
			namesItems.put(item.locator, item)
		}
		for (api : model.resourceAPIs) {
			apiItems.put(api, GenTemplateTraceUtils::getTraceItem(xsdTrace, 'resourceAPI', 'sourceData', locator.locate(api)))
		}
	}

	def getTypeName(EObject obj) {
		obj.namesProperty('typeName')
	}

	def getRootElementName(EObject obj) {
		obj.namesProperty('rootElementName')
	}

	def getElementName(EObject obj) {
		obj.namesProperty('elementName')
	}

	def getListElementName(EObject obj) {
		obj.namesProperty('listElementName')
	}

	def getListItemElementName(EObject obj) {
		obj.namesProperty('listItemElementName')
	}

	def getAttributeName(EObject obj) {
		obj.namesProperty('attributeName')
	}

	def getIdInTrace(EObject obj) {
		obj.namesProperty('id')
	}

	def namesProperty(EObject obj, String property) {
		namesItems.get(locator.locate(obj))?.properties?.get(property)
	}

	def String getXsdFilePath(ResourceAPI resourceAPI) {
		val item = GenTemplateTraceUtils::getTraceItem(xsdTrace, 'file', 'sourceData', locator.locate(resourceAPI))
		val outDir = Paths::get(context.outputDirectory.absolutePath)
		val file = Paths::get(item.outputFile.absolutePath)
		return outDir.relativize(file).join('/')
	}

	def String getNsPrefix(ResourceAPI resourceAPI) {
		apiItems.get(resourceAPI).properties.get('namespacePrefix')
	}

	def String getNamespace(ResourceAPI resourceAPI) {
		apiItems.get(resourceAPI).properties.get('namespace')
	}

	def String getComplexTypeQName(ResourceDefinition resource) {
		val api = resource.eContainer as ResourceAPI
		api.nsPrefix + ':' + resource.typeName
	}
}
