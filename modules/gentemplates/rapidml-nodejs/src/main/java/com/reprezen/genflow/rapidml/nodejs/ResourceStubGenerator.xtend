package com.reprezen.genflow.rapidml.nodejs

import com.reprezen.genflow.rapidml.nodejs.NodejsGenerator.Generator
import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.DataModel
import com.reprezen.rapidml.DataType
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.ObjectResource
import com.reprezen.rapidml.PropertyReference
import com.reprezen.rapidml.ReferenceEmbed
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedResponse
import com.reprezen.rapidml.URI
import com.reprezen.rapidml.ZenModel
import com.reprezen.rapidml.realization.processor.CycleDetector
import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.common.util.EList

class ResourceStubGenerator implements Generator {

	val ServiceDataResource resource;
	val String fqName;

	extension CycleDetector<ReferenceTreatment> = new CycleDetector<ReferenceTreatment>

	new(ServiceDataResource resource) {
		this.resource = resource
		this.fqName = resource.fqName
	}

	override generate() {
		'''
			const Resource = require('../../../lib/Resource');
			
			class «resource.name» extends Resource {
				constructor(app, dao) {
					super(app);
					this.dao = dao;
					this.resourceFQN = '«fqName»';
					«FOR method : resource.methods»
						app.«method.httpMethod.literal.toLowerCase»(`${app.locals.basePath}«resource.URI.expressify»`, this.«method.id»Handler.bind(this));
					«ENDFOR»
				}
				
				«FOR method : resource.methods SEPARATOR "\n"»
					«method.handler»
				«ENDFOR»
			}
			
			module.exports = «resource.name»;
		'''
	}

	def private getHandler(Method method) {
		'''
			«method.id»Handler(req, res, next) {
			
				const enrichments = «method.enrichments.trim»; 
			
				super.callMethodImpl(this.resourceFQN, '«method.id»', enrichments, req, res, next);
			
			}
		'''
	}

	def String getEnrichments(Method method) {
		'''
			{
				«FOR response : method.responses SEPARATOR ","»
					'«String.format("%03d", response.statusCode)»': «response.enrichmentSpec ?: "null"»
				«ENDFOR»
			}
		'''
	}

	def private getEnrichmentSpec(TypedResponse response) {
		val type = response.structType
		if (type !== null) {
			'''
				{
					type: '«response.structType.fqName»',
					multi: «response.isMultivalued»,
					fields: [
						«FOR prop : response.enrichmentProperties SEPARATOR ","»
							'«prop.baseProperty.name»'
						«ENDFOR»
					],
					enrichments: «response.governingReferenceTreatments.enrichmentSpec»
				}
			'''
		}
	}

	def private getGoverningReferenceTreatments(TypedResponse response) {
		var treatments = response.referenceTreatments
		if (treatments.empty && response.resourceType !== null) {
			treatments = (response.resourceType as ServiceDataResource).referenceTreatments
		}
		return treatments
	}

	def private getEnrichmentProperties(TypedResponse response) {
		val resource = response.resourceType as ServiceDataResource
		if (response.resourceType !== null) {
			val orLink = resource.allReferenceTreatments.filter [
				it instanceof ReferenceLink && it.referenceElement.name == "ObjectResourceLink"
			].head
			if (orLink !== null) {
				// this is a special case of a collection resource with no inline realization, and for which a
				// default object resource exists. The realization is a collection of links.
				// Decorating properties, if any, will be included with the link in this case
				return #[]
			} else {
				return (response.resourceType as ServiceDataResource).properties.allIncludedProperties
			}
		} else {
			return response.properties.allIncludedProperties
		}
	}

	def private String getEnrichmentSpec(EList<ReferenceTreatment> treatments) {
		if (treatments.empty) {
			return "{}"
		}
		'''
			{
				«FOR treatment : treatments SEPARATOR ","»
					«treatment.enrichmentSpecItem»
				«ENDFOR»
			}
		'''
	}

	def private getEnrichmentSpecItem(ReferenceTreatment treatment) {
		if (treatment.
			visit) {
			try {
				'''
					«treatment.referenceElement.name»: {
						type: '«treatment.referenceElement.dataType.name»',
						multi: «treatment.isMultivalued»,
						fields: [
							«FOR feature : treatment.linkDescriptor?.allIncludedProperties?.map[baseProperty] ?: new BasicEList<Feature>() SEPARATOR ","»
								'«feature.name»'
							«ENDFOR»
						],
						«IF treatment instanceof ReferenceLink»
							link: {
								href: '«treatment.linkURI»',
								boundParams: «treatment.boundParams.trim»,
								rel: '«treatment.linkRelation?.name ?: "related"»'
							},
						«ENDIF»
						enrichments: «treatment.nestedTreatments»
					}
				'''
			} finally {
				treatment.leave
			}
		}
	}

	def private isMultivalued(TypedResponse response) {
		if(response.resourceType !== null) response.resourceType instanceof CollectionResource else false
	}

	def private isMultivalued(ReferenceTreatment treatment) {
		val propIsMulti = treatment.referenceElement.maxOccurs < 0;
		switch (treatment) {
			ReferenceLink: propIsMulti && treatment.targetResource instanceof ObjectResource
			ReferenceEmbed: propIsMulti
		}
	}

	def private String boundParams(
		ReferenceLink link) {
		'''
			{
				«FOR param : link.targetResource.URI.uriParameters.filter[it.sourceReference instanceof PropertyReference] SEPARATOR ","»
					«param.name»: '«(param.sourceReference as PropertyReference).conceptualFeature.name»'
				«ENDFOR»
			}
		'''
	}

	def private getNestedTreatments(ReferenceTreatment treatment) {
		switch (treatment) {
			ReferenceLink: "{}"
			ReferenceEmbed: treatment.nestedReferenceTreatments.enrichmentSpec
		}
	}

	def private getLinkURI(ReferenceTreatment treatment) {
		switch (treatment) {
			ReferenceLink: treatment.targetResource.URI.expressify
			ReferenceEmbed: null
		}
	}

	def private getStructType(TypedResponse response) {
		(response.resourceType as ServiceDataResource)?.dataType ?: response.dataType
	}

	def private String getFqName(ResourceDefinition resource) {
		val api = resource.eContainer as ResourceAPI
		val model = api.eContainer as ZenModel
		'''«model.name».«api.name».«resource.name»'''
	}

	def private String getFqName(DataType type) {
		val dataModel = type.eContainer as DataModel
		val model = dataModel.eContainer as ZenModel
		'''«model.name».«dataModel.name».«type.name»'''
	}

	def private expressify(URI uri) {
		uri.toString.replaceAll("/\\{([^/]+)\\}((?=/)|$)", "/:$1");
	}
}
