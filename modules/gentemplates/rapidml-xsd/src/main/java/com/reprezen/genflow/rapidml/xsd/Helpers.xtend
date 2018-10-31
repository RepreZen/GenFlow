package com.reprezen.genflow.rapidml.xsd

import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.api.zenmodel.ZenModelLocator
import com.reprezen.rapidml.ZenModel
import com.reprezen.rapidml.xtext.util.ZenModelHelper

class Helpers {
	val IGenTemplateContext context
	var ZenModel model

	var TraceHelper traceHelper
	var ParamsHelper paramsHelper
	var FeatureHelper featureHelper
	var ReferenceLinkHelper referenceLinkHelper
	var ResourceHelper resourceHelper
	var XMLSchemaHelper xmlSchemaHelper
	var ZenModelHelper zenModelHelper
	var ZenModelLocator zenModelLocator

	new(IGenTemplateContext context, ZenModel zenModel) {
		this.context = context
		this.model = model
	}

	def getContext() {
		context
	}

	def getParamsHelper() {
		paramsHelper ?: (paramsHelper = new ParamsHelper(this))
	}

	def getTraceHelper() {
		traceHelper ?: (traceHelper = new TraceHelper(this))
	}

	def getFeatureHelper() {
		featureHelper ?: (featureHelper = new FeatureHelper(this))
	}

	def getReferecneLinkHelper() {
		referenceLinkHelper ?: (referenceLinkHelper = new ReferenceLinkHelper(this))
	}

	def getResourceHelper() {
		resourceHelper ?: (resourceHelper = new ResourceHelper(this))
	}

	def getXmlSchemaHelper() {
		xmlSchemaHelper ?: (xmlSchemaHelper = new XMLSchemaHelper(this))
	}

	def getZenModelHelper() {
		zenModelHelper ?: (zenModelHelper = new ZenModelHelper)
	}
	
	def getZenModelLocator() {
		zenModelLocator ?: (zenModelLocator = new ZenModelLocator(model))
	}
}
