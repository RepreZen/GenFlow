/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.swaggerui.v3

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.base.Strings
import com.reprezen.genflow.api.normal.openapi.OpenApiNormalizer
import com.reprezen.genflow.api.normal.openapi.Option
import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.common.HtmlInjections
import java.io.File

import static com.reprezen.genflow.common.HtmlInjections.*

class XGenerateSwaggerUIv3 {

	extension HtmlInjections htmlInjections
	
	val static mapper = if (Boolean.valueOf(true)) { // trick to get static code block 
		val m = new ObjectMapper();
		m.setSerializationInclusion(Include.NON_NULL);
		m;
	}

	def generate(String json, String uriPrefix, String version, IGenTemplateContext context) {
		generate(json, uriPrefix, false, SwaggerUi3Options.DEFAULT, context);
	}

	def String generateNormalized(String modelText, File inputFile, Integer modelVersion, String uriPrefix,
		boolean isLiveView, IGenTemplateContext context) {
		val normalized = new OpenApiNormalizer(modelVersion, Option.MINIMAL_OPTIONS).of(modelText).normalizeToJson(
			inputFile.toURI().toURL());
		return generate(mapper.writeValueAsString(normalized), uriPrefix, isLiveView, SwaggerUi3Options.DEFAULT, context);
	}

	def String generate(String modelText, String uriPrefix, boolean isLiveView, SwaggerUi3Options options, IGenTemplateContext context) {
		htmlInjections = context.genTargetParameters.get(HTML_INJECTIONS_PARAM) as HtmlInjections ?: new HtmlInjections
		val json = modelText.toJson
		'''
			<!-- HTML for static distribution bundle build -->
			<!DOCTYPE html>
			<html lang="en">
				<head>
			  		«HEAD_TOP.inject»
			
					<meta charset="UTF-8">
			  		<title>Swagger UI</title>
			  		<link href="https://fonts.googleapis.com/css?family=Open+Sans:400,700|Source+Code+Pro:300,600|Titillium+Web:400,600,700" rel="stylesheet">
			  		<link rel="stylesheet" type="text/css" href="«uriPrefix»/assets/swagger-ui.css" >
				  	<link rel="stylesheet" type="text/css" href="«uriPrefix»/reprezen/css/mini-bootstrap.css" >
			  		<link rel="icon" type="image/png" href="«uriPrefix»/assets/favicon-32x32.png" sizes="32x32" />
			  		<link rel="icon" type="image/png" href="«uriPrefix»/assets/favicon-16x16.png" sizes="16x16" />
			  		<style>
				    	html
			    		{
					        box-sizing: border-box;
			        		overflow: -moz-scrollbars-vertical;
				        	overflow-y: scroll;
			    		}
			    		*,
				    	*:before,
			    		*:after
			    		{
					        box-sizing: inherit;
			    		}
			
			    		body {
					    	margin:0;
			      			background: #fafafa;
			    		}
			    
			    		/* RepreZen styles*/
			    		.zennav {
			        		overflow: hidden;
			    		}
			    
			    		.zennav a {
				        	float: right;
			        		display: block;
			        		color: #808080;
				        	text-align: center;
				        	padding: 14px 16px;
					        text-decoration: none;
			        		font-size: 17px;
			        		font-family: Arial;
			    		}

			    		.zennav a img{
					        vertical-align: middle;
			    		}
					</style>
			
			  		«HEAD_BOTTOM.inject»
				</head>
			
				<body>
					«BODY_TOP.inject»
			
					«IF !isLiveView»
						<div class="zennav">
						<a href="http://reprezen.com" target="_blank">Created with <img class="logo" src="«uriPrefix»/reprezen/images/logo.png" ></a>
						</div>
					«ENDIF»	
			
					<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" style="position:absolute;width:0;height:0">
			  			<defs>
				    		<symbol viewBox="0 0 20 20" id="unlocked">
				          		<path d="M15.8 8H14V5.6C14 2.703 12.665 1 10 1 7.334 1 6 2.703 6 5.6V6h2v-.801C8 3.754 8.797 3 10 3c1.203 0 2 .754 2 2.199V8H4c-.553 0-1 .646-1 1.199V17c0 .549.428 1.139.951 1.307l1.197.387C5.672 18.861 6.55 19 7.1 19h5.8c.549 0 1.428-.139 1.951-.307l1.196-.387c.524-.167.953-.757.953-1.306V9.199C17 8.646 16.352 8 15.8 8z"></path>
			    			</symbol>
			
			    			<symbol viewBox="0 0 20 20" id="locked">
				      			<path d="M15.8 8H14V5.6C14 2.703 12.665 1 10 1 7.334 1 6 2.703 6 5.6V8H4c-.553 0-1 .646-1 1.199V17c0 .549.428 1.139.951 1.307l1.197.387C5.672 18.861 6.55 19 7.1 19h5.8c.549 0 1.428-.139 1.951-.307l1.196-.387c.524-.167.953-.757.953-1.306V9.199C17 8.646 16.352 8 15.8 8zM12 8H8V5.199C8 3.754 8.797 3 10 3c1.203 0 2 .754 2 2.199V8z"/>
			    			</symbol>
			
			    			<symbol viewBox="0 0 20 20" id="close">
				      			<path d="M14.348 14.849c-.469.469-1.229.469-1.697 0L10 11.819l-2.651 3.029c-.469.469-1.229.469-1.697 0-.469-.469-.469-1.229 0-1.697l2.758-3.15-2.759-3.152c-.469-.469-.469-1.228 0-1.697.469-.469 1.228-.469 1.697 0L10 8.183l2.651-3.031c.469-.469 1.228-.469 1.697 0 .469.469.469 1.229 0 1.697l-2.758 3.152 2.758 3.15c.469.469.469 1.229 0 1.698z"/>
			    			</symbol>
			
					    	<symbol viewBox="0 0 20 20" id="large-arrow">
				      			<path d="M13.25 10L6.109 2.58c-.268-.27-.268-.707 0-.979.268-.27.701-.27.969 0l7.83 7.908c.268.271.268.709 0 .979l-7.83 7.908c-.268.271-.701.27-.969 0-.268-.269-.268-.707 0-.979L13.25 10z"/>
					    	</symbol>
			
					    	<symbol viewBox="0 0 20 20" id="large-arrow-down">
				      			<path d="M17.418 6.109c.272-.268.709-.268.979 0s.271.701 0 .969l-7.908 7.83c-.27.268-.707.268-.979 0l-7.908-7.83c-.27-.268-.27-.701 0-.969.271-.268.709-.268.979 0L10 13.25l7.418-7.141z"/>
			    			</symbol>
			
					    	<symbol viewBox="0 0 24 24" id="jump-to">
				    		  	<path d="M19 7v4H5.83l3.58-3.59L8 6l-6 6 6 6 1.41-1.41L5.83 13H21V7z"/>
			    			</symbol>
			
			    			<symbol viewBox="0 0 24 24" id="expand">
				      			<path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z"/>
			    			</symbol>
			
			  			</defs>
					</svg>

					<div id="swagger-ui"></div>
			
					<script src="«uriPrefix»/assets/swagger-ui-bundle.js"> </script>
					<script src="«uriPrefix»/assets/swagger-ui-standalone-preset.js"> </script>
					<script>
						window.onload = function() {
			  				// Build a system
			  				 const ui = SwaggerUIBundle(«generateOptions(json, options)»)
			
			  				window.ui = ui;
			  				// RepreZen customization Explore input form to download an external Swagger
			  				// Can't use JQuery as it's not provided as a dependency .. $(".download-url-wrapper").remove();
			  				var exploreElement = document.getElementsByClassName("topbar")[0];
			  				exploreElement.parentNode.removeChild(exploreElement); 
						};
					</script>
					«IF isLiveView»
						<script src='«uriPrefix»/reprezen/js/jquery-3.2.1.min.js' type='text/javascript'></script>
						<script src='«uriPrefix»/reprezen/js/bootstrap-3.3.7.min.js' type='text/javascript'></script>
					«ENDIF»
					«BODY_BOTTOM.inject»
				</body>
			</html>
		'''
	}

	val static ObjectMapper jsonMapper = new ObjectMapper()
	val static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())

	def private toJson(String modelText) {
		if (modelText.trim().startsWith("{")) {
			return modelText;
		}
		val tree = yamlMapper.readTree(modelText);
		return jsonMapper.writeValueAsString(tree);
	}

	private def orNull(Object value) '''«IF value !== null»"«value»"«ELSE»null«ENDIF»'''

	private def maybeFunction(String option) {
		if (option === null || option.isEmpty) return '''null'''

		// apisSorter and methodsSorter options take either fixed string values (e.g. "alpha") or a Javascript function object. 
		// The latter should not be quoted, while the former should. This method applies quoting unless the value begins with "function".
		if(option.startsWith("function")) option else '''"«option»"'''
	}

	def String generateOptions(String spec, SwaggerUi3Options options) '''
	  {
	    url: "",
	    spec: «spec»,
	    dom_id: '#swagger-ui',
	    presets: [
	        SwaggerUIBundle.presets.apis,
	        SwaggerUIStandalonePreset
	    ],
	    plugins: [
	      SwaggerUIBundle.plugins.DownloadUrl
	    ],
	    layout: «orNull(options.layout)»,
	    deepLinking: «options.deepLinking»,
	    displayOperationId: «options.displayOperationId»,
	    defaultModelsExpandDepth: «options.defaultModelsExpandDepth»,
	    defaultModelExpandDepth: «options.defaultModelExpandDepth»,
	    defaultModelRendering: «orNull(options.defaultModelRenderingAsString)»,
	    displayRequestDuration: «options.displayRequestDuration»,
	    docExpansion: «orNull(options.docExpansionAsString)»,
	    filter: «options.filter»,
	    maxDisplayedTags: «options.maxDisplayedTags»,
	    operationsSorter: «maybeFunction(options.operationsSorter)»,
	    showExtensions: «options.showExtensions»,
	    showCommonExtensions: «options.showCommonExtensions»,
	    tagsSorter: «maybeFunction(options.tagsSorter)»,
	    onComplete: «maybeFunction(options.onComplete)»,
	    «IF !Strings.isNullOrEmpty(options.oauth2RedirectUrl)»
	    oauth2RedirectUrl: "«options.oauth2RedirectUrl»",
	    «ENDIF»
	    requestInterceptor: «maybeFunction(options.requestInterceptor)»,
	    responseInterceptor: «maybeFunction(options.responseInterceptor)»,
	    showMutatedRequest: «options.showMutatedRequest»,
	    supportedSubmitMethods: «options.supportedSubmitMethodsAsString»,
	    validatorUrl: «orNull(options.validatorUrl)»,
	    modelPropertyMacro: «maybeFunction(options.modelPropertyMacro)»,
	    parameterMacro: «maybeFunction(options.parameterMacro)»,
	    initOAuth: «maybeFunction(options.initOAuth)»,
	    preauthorizeBasic: «maybeFunction(options.preauthorizeBasic)»,
	    preauthorizeApiKey: «maybeFunction(options.preauthorizeApiKey)»
	  }
	'''
}
