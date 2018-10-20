/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.swaggerui.v2

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Strings
import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.common.HtmlInjections
import io.swagger.models.Swagger
import java.net.URL

class XGenerateSwaggerUI {

	val static mapper = if (Boolean.valueOf(true)) { // trick to get static code block
		val m = new ObjectMapper
		m.setSerializationInclusion(Include.NON_NULL)
		m
	}

	def String generateForSwaggerSpec(Swagger swagger, String urlPrefix, URL resolutionBase, boolean isLiveView,
		SwaggerUiOptions options, IGenTemplateContext context) {
		val spec = mapper.writeValueAsString(swagger)
		generateForSwaggerSpec(spec, urlPrefix, resolutionBase, isLiveView, options, context)
	}

	extension HtmlInjections htmlInjections

	def String generateForSwaggerSpec(String spec, String urlPrefix, URL resolutionBase, boolean isLiveView,
		SwaggerUiOptions options, IGenTemplateContext context) {
		htmlInjections = context.genTargetParameters.get(HtmlInjections.HTML_INJECTIONS_PARAM) as HtmlInjections ?:
			new HtmlInjections
		'''
			<!DOCTYPE html>
			<html>
				<head>
					«HtmlInjections.HEAD_TOP.inject»
					<meta http-equiv="X-UA-Compatible" content="IE=9,10" />
					<title>Swagger UI</title>
					<link rel="icon" type="image/png" href="«urlPrefix»/images/favicon-32x32.png" sizes="32x32" />
					<link rel="icon" type="image/png" href="«urlPrefix»/images/favicon-16x16.png" sizes="16x16" />
					<link href='«urlPrefix»/css/typography.css' media='screen' rel='stylesheet' type='text/css'/>
					<link href='«urlPrefix»/css/reset.css' media='screen' rel='stylesheet' type='text/css'/>
					<link href='«urlPrefix»/css/screen.css' media='screen' rel='stylesheet' type='text/css'/>
					<link href='«urlPrefix»/css/reset.css' media='print' rel='stylesheet' type='text/css'/>
					<link href='«urlPrefix»/css/print.css' media='print' rel='stylesheet' type='text/css'/>
					<link href='«urlPrefix»/bootstrap/css/fake-bootstrap.css' rel='stylesheet' type='text/css'/>
					<link href='«urlPrefix»/css/settings.css' rel='stylesheet' type='text/css'/>
					«IF options.theme !== null»
						<link href='«urlPrefix»/css/theme-«options.theme.toString.toLowerCase».css' media='screen' rel='stylesheet' type='text/css'/>
					«ENDIF»
					<!-- for some reason this doesn't load the fonts! 
					<link href='«urlPrefix»/font-awesome-4.6.3/css/font-awesome.css' rel='stylesheet' type='text/css'/>
					-->
					<!-- this does, but it requires connectivity -->
					<script src="https://use.fontawesome.com/0ceea6e477.js"></script>
			
					<script src='«urlPrefix»/lib/js-yaml.min.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/object-assign-pollyfill.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/jquery-1.8.0.min.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/jquery.slideto.min.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/jquery.wiggle.min.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/jquery.ba-bbq.min.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/handlebars-4.0.5.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/lodash.min.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/backbone-min.js' type='text/javascript'></script>
					<script src='«urlPrefix»/swagger-ui.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/highlight.9.1.0.pack.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/highlight.9.1.0.pack_extended.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/jsoneditor.min.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/marked.js' type='text/javascript'></script>
					<script src='«urlPrefix»/lib/swagger-oauth.js' type='text/javascript'></script>
			
					<script type="text/javascript">
				hljs.configure({
					highlightSizeThreshold: 5000
				});
			
						$(document).ready(function () {
				var refreshSwaggerUi = function() {
					window.swaggerUi = new SwaggerUi({
						url: 'api.json',
						spec: «spec»,
						validatorUrl: null,
						jsonEditor: getCookie('jsonEditor') == 'true',
						dom_id: "swagger-ui-container",
						supportedSubmitMethods: «options.supportedSubmitMethodsAsString»,
						onComplete: function(swaggerApi, swaggerUi){
							if(typeof initOAuth == "function") {
								/*
									clientId: "your-client-id",
									clientSecret: "your-client-secret-if-required",
									realm: "your-realms",
									appName: "your-app-name",
									scopeSeparator: " ",
									additionalQueryStringParams: {}
								*/
							}
						},
						onFailure: function(data) {
							log("Unable to Load SwaggerUI");
						},
						docExpansion: «IF options.docExpansion!== null»"«options.docExpansionAsString»"«ELSE»null«ENDIF»,
						apisSorter: «IF options.apisSorter!== null»«options.apisSorterAsString.maybeFunction»«ELSE»null«ENDIF»,
						operationsSorter: «IF options.operationsSorter!== null»«options.operationsSorterAsString.maybeFunction»«ELSE»null«ENDIF»,
						defaultModelRendering: «IF options.defaultModelRendering!== null»"«options.defaultModelRenderingAsString»"«ELSE»null«ENDIF»,
						«IF !Strings.isNullOrEmpty(options.oauth2RedirectUrl)»oauth2RedirectUrl: "«options.oauth2RedirectUrl»",«ENDIF»
						showRequestHeaders: «options.showRequestHeaders»
					});
					window.swaggerUi.load();
				};
			
							function log() {
				if ('console' in window) {
					console.log.apply(console, arguments);
				}
							}
			
							// following makes it easier to test this in an external browser
							if (typeof getCookie === "undefined") {
				var cookies = {};
				getCookie = function(name) { return cookies[name]; };
				createCookie = function(name, value) { cookies[name] = value; };
							}
							$(".swagger-ui-options .iconbox.option").each(function() {
								$(this).prop('checked', getCookie($(this).attr('id')) == 'true');
								$(this).on('click', function() {
									createCookie($(this).attr('id'), $(this).prop('checked') ? 'true' : 'false');
									refreshSwaggerUi();
									return true;
								});
							});
							refreshSwaggerUi();
						});
					</script>
					«HtmlInjections.HEAD_BOTTOM.inject»
				</head>
			
				<body class="swagger-section">
				«HtmlInjections.BODY_TOP.inject»
				<div class="swagger-ui-wrap">
					<div class="swagger-ui-options">
						<input type="checkbox" class="iconbox" id="options-gear">
						<label for="options-gear" title="Swagger-UI Settings"><i class="fa fa-cog"></i></label>
						«IF isLiveView || options.showJsonEditorInSettings»
							<div>
								<input type="checkbox" class="iconbox option" id="jsonEditor"/>
								<label for="jsonEditor">
									<i class="checked fa fa-fw fa-check"></i>
									<i class="unchecked fa fa-fw"></i>
									Use forms for request input
								</label>
							</div>
						«ENDIF»
					</div>
				</div>
				<!--div id='header'>
					<div class="swagger-ui-wrap">
						<a id="logo" href="http://swagger.io"><img class="logo__img" alt="swagger" height="30" width="30" src="images/logo_small.png" /><span class="logo__title">swagger</span></a>
						<form id='api_selector'>
							<div class='input'><input placeholder="http://example.com/api" id="input_baseUrl" name="baseUrl" type="text"/></div>
							<div id='auth_container'></div>
							<div class='input'><a id="explore" class="header__btn" href="#" data-sw-translate>Explore</a></div>
						</form>
					</div>
				</div-->
				
				<div id="message-bar" class="swagger-ui-wrap">&nbsp;</div>
				<div id="swagger-ui-container" class="swagger-ui-wrap"></div>
				«HtmlInjections.BODY_BOTTOM.inject»
				</body>
			</html>
		     '''
	}

	def maybeFunction(String option) {
		// apisSorter and methodsSorter options take either fixed string values (e.g. "alpha") or a Javascript function object. 
		// The latter should not be quoted, while the former should. This method applies quoting unless the value begins with "function".
		if(option.startsWith("function")) option else '''"«option»"'''

	}
}
