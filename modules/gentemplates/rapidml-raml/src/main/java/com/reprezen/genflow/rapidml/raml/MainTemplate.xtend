/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.raml

import com.reprezen.genflow.api.GenerationException
import com.reprezen.genflow.api.zenmodel.ZenModelExtractOutputItem
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ZenModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.List
import java.util.logging.Level
import org.raml.emitter.RamlEmitter
import org.raml.parser.rule.ValidationResult
import org.raml.parser.visitor.RamlDocumentBuilder
import org.raml.parser.visitor.RamlValidationService

class MainTemplate extends ZenModelExtractOutputItem<ResourceAPI> { // Xtend language is used for generator template here
// TODO: consider making this a templateParameter.
	boolean augmentGeneratedRAML = false;

	override generate(ZenModel zenModel, ResourceAPI resourceAPI) {
		context.logger.info("Generating RAML for " + resourceAPI.name);
		val ramlTargetModel = new RamlTargetModel(zenModel, resourceAPI)
		ramlTargetModel.attachLogger(context.logger)
		val myDate = new Date();
		val sdf = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss");
		val dateStamp = sdf.format(myDate);

		// RAML uses spaces instead of tabs for indentation - 2 spaces in fact.
		val generatedRAML = '''#%RAML 0.8
#===============================================================
#  RAML code generated from RAPID-ML in RepreZen API Studio
#  Generated on «dateStamp»
#  References:
#    - RAML Specification - http://raml.org/spec.html
#    - RAML Projects      - http://raml.org/projects.html
#    - RAML Tools         - http://www.apihub.com/raml-tools
#    - RAML Java Parser   - https://github.com/raml-org/raml-java-parser
#===============================================================
title: «ramlTargetModel.title»
version: «ramlTargetModel.version»
baseUri: «ramlTargetModel.baseUri»
#===============================================================
# API resource definitions: «ramlTargetModel.ramlResources.size»
#===============================================================  
«var resourceCounter = 0»
«FOR ramlResource : ramlTargetModel.ramlResources»
#---------------------------------------------------------------
# Resource «resourceCounter = resourceCounter + 1»: «ramlResource.displayName»
#---------------------------------------------------------------
# Relative URI:
/«ramlResource.getURI»:
  displayName: «ramlResource.displayName»
  description: |
    «ramlResource.description»
  «generateParameterList("uriParameters", ramlResource.uriParameters)»
  «FOR ramlMethod : ramlResource.methods»
    «ramlMethod.verb»:
      description: |
        «ramlMethod.description»
      # Request properties:
      «val ramlRequest = ramlMethod.request»
      «IF !ramlRequest.headerParameters.empty»
        «generateParameterList("headers", ramlRequest.headerParameters)»
      «ENDIF»
      «IF !ramlRequest.queryParameters.empty»
        «generateParameterList("queryParameters", ramlRequest.queryParameters)»
      «ENDIF»
      body:
        «FOR requestMediaType : ramlRequest.mediaTypes»
          «requestMediaType»:
            «var requestMessageSchemas = ramlRequest.schemas»
            «IF !requestMessageSchemas.empty && requestMessageSchemas.get(requestMediaType) !== null»
              schema: | 
                «formatSchema(requestMediaType, requestMessageSchemas.get(requestMediaType))»
            «ENDIF»
            «IF ramlRequest.hasExample»
              example: |
                «ramlRequest.example»
            «ENDIF»
        «ENDFOR»
      responses:
        «FOR ramlResponse : ramlMethod.responses»
          «ramlResponse.statusCode»:
            «IF !ramlResponse.headerParameters.empty»
              «generateParameterList("headers", ramlResponse.headerParameters)»
            «ENDIF»
            body:
              «FOR responseMediaType : ramlResponse.mediaTypes»
                «responseMediaType»:
                  «var responseMessageSchemas = ramlResponse.schemas»
                  «IF !responseMessageSchemas.empty && responseMessageSchemas.get(responseMediaType) !== null»
                    schema: | 
                      «formatSchema(responseMediaType, responseMessageSchemas.get(responseMediaType))»
                  «ENDIF»
                  «IF ramlResponse.hasExample»
                    example: |
                      «ramlResponse.example»
                  «ENDIF»
              «ENDFOR»
        «ENDFOR»
  «ENDFOR»
«ENDFOR»
'''

		// Validate and possibly augment the generated RAML using the RAML Java Parser:
		// https://github.com/raml-org/raml-java-parser
		validate(generatedRAML);
		if (augmentGeneratedRAML) {
			return augment(generatedRAML);
		}
		context.logger.info("RAPID>>RAML Code Generation - DONE!")
		return generatedRAML;
	}

	def formatSchema(String mediaType, String schema) {
		return '''
			«IF RamlMessage.mediaTypeIsJSON(mediaType)»
				{
				  "$schema": "http://json-schema.org/draft-03/schema",
				  «schema»
				}
			«ELSE»
				«schema»
			«ENDIF»
		'''
	}

	/** 
	 * As per the RAML specification (http://raml.org/spec.html#named-parameters), in RAML
	 * parameters in the following locations have the same properties:
	 * - URI parameters
	 * - Request query string parameters
	 * - Request bodies (?)
	 * - Request and response headers.
	 * So we can use the same method to generate parameter lists in all of these places.
	 */
	def generateParameterList(String listName, List<RamlParameter> ramlParameters) {
		return '''
			«IF !ramlParameters.empty»
				«listName»:
				  «FOR ramlParameter : ramlParameters»
				  	«ramlParameter.name»:
				  	  displayName: «ramlParameter.displayName»
				  	  description: |
				  	    «ramlParameter.description»
				  	  type: «ramlParameter.type»
				  	  «IF ramlParameter.stringEnumeration»
				  	  	enum: «ramlParameter.enumerationConstants.toString»
				  	  «ENDIF»
				  	  «IF ramlParameter.hasConstraint»
				  	  	«val ramlConstraint = ramlParameter.constraint»
				  	  	«IF ramlConstraint.stringRegExConstraint»
				  	  		pattern: («ramlConstraint.pattern»)
				  	  	«ENDIF»
				  	  	«IF ramlConstraint.stringLengthConstraint»
				  	  		minLength: «ramlConstraint.minLength»
				  	  		maxLength: «ramlConstraint.maxLength»
				  	  	«ENDIF»
				  	  	«IF ramlConstraint.intValueRangeConstraint»
				  	  		minimum: «ramlConstraint.minimum»
				  	  		maximum: «ramlConstraint.maximum»
				  	  	«ENDIF»
				  	  «ENDIF»
				  	  #example: RAPID does not support example parameters!
				  	  #repeat: # true/false. Does the param occur multiple times? Will the post-processor handle this? TODO: test
				  	  required: «ramlParameter.required»
				  	  «IF ramlParameter.hasDefaultValue»
				  	  	default: «ramlParameter.defaultValue»
				  	  «ENDIF»
				  «ENDFOR»
			«ENDIF»
		'''
	}

	def validate(String generatedRAML) {
		val results = RamlValidationService.createDefault().validate(generatedRAML,
			"IN-MEMORY") as List<ValidationResult>
		if (!results.empty) {
			var message = "Generated RAML is invalid: " + results.toString;
			context.logger.log(Level::SEVERE, message, new GenerationException(message))
		}
	}

	def augment(String generatedRAML) {
		val ramlObject = new RamlDocumentBuilder().build(generatedRAML, "IN-MEMORY");
		val emitter = new RamlEmitter();
		return emitter.dump(ramlObject);
	}

}
