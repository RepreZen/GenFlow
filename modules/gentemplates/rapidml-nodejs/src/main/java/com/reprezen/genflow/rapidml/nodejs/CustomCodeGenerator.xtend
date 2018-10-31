package com.reprezen.genflow.rapidml.nodejs

import com.reprezen.genflow.api.GenerationException
import com.reprezen.genflow.rapidml.nodejs.NodejsGenerator.Generator
import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedResponse
import com.reprezen.rapidml.URI

class CustomCodeGenerator implements Generator {

	val ServiceDataResource resource

	new(ServiceDataResource resource) {
		this.resource = resource
	}

	override generate() throws GenerationException {
		'''
			const CustomImpl = require('../../../lib/CustomImpl');
			
			class «resource.name»Impl extends CustomImpl {
				«FOR method : resource.methods SEPARATOR "\n"»
					// «method.httpMethod» «resource.URI.expressify»
					«FOR response: method.responses»
						// «response.status» => «response.type» 
					«ENDFOR»
					«method.name»(dao, enrichments, req, res, next) {
						res.status(500).send("Method not implemented");
					}
				«ENDFOR»
			}
			
			module.exports = «resource.name»Impl; 
		'''
	}

	def private getStatus(TypedResponse response) {
		String.format("%03d", response.statusCode)
	}

	def private getType(TypedResponse response) {
		val struct = (response.resourceType as ServiceDataResource)?.dataType ?: response.dataType
		if (struct !== null) {
			val card = if(response.resourceType !== null &&
					response.resourceType instanceof CollectionResource) "*" else ""
			val res = if (response.resourceType !== null) ''' as «response.resourceType.name»''' else ""
			'''«struct.name»«card»«res»'''
		} else {
			"no response data"
		}
	}

	def private expressify(URI uri) {
		uri.toString.replaceAll("/\\{([^/]+)\\}((?=/)|$)", "/:$1");
	}
}
