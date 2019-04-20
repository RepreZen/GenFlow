/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.doc

import com.google.common.collect.Lists
import com.reprezen.genflow.api.swagger.SwaggerOutputItem
import com.reprezen.genflow.common.HtmlInjections
import io.swagger.models.Swagger
import java.util.List
import java.util.regex.Pattern

import static com.reprezen.genflow.common.HtmlInjections.*

class XGenerateSwaggerDoc extends SwaggerOutputItem {
	public static val URL_PREFIX_PARAM = 'urlPrefix'
	public static val ANCHOR_NAME_PARAM = 'anchorName'
	public static val PREVIEW_PARAM = 'preview'

	extension OptionHelper optionHelper
	extension HtmlHelper htmlHelper
	extension MiscHelper miscHelper
	extension HtmlInjections htmlInjections

	override generate(Swagger swagger) {
		try {
			HelperHelper.open(swagger, context)
			optionHelper = HelperHelper.optionHelper
			htmlHelper = HelperHelper.htmlHelper
			miscHelper = HelperHelper.miscHelper
			htmlInjections = context.genTargetParameters.get(HTML_INJECTIONS_PARAM) as HtmlInjections ?:
				new HtmlInjections
			doGenerate(swagger)
		} finally {
			HelperHelper.close()
		}
	}

	def doGenerate(Swagger swagger) {
		this.optionHelper = HelperHelper.optionHelper
		val urlPrefix = getUrlPrefix
		val preview = isPreview
		val showPaths = !swagger.paths.empty
		val showDefs = (!preview || !showPaths) && !swagger.definitions.empty
		val showParams = (!preview || !showPaths) && !swagger.parameters.empty
		val showResponses = (!preview || !showPaths) && !swagger.responses.empty
		val includeTOC = !preview && isIncludeTOC

		val html = '''
			<!DOCTYPE html>
			<html lang="en">
				<!-- Preview: «preview» -->
				«htmlHeadSection(swagger, urlPrefix, preview)»
				<body data-spy="scroll" data-target="#toc">            
					«BODY_TOP.inject»
					<!-- Fixed navbar -->
					<div class="navbar navbar-default navbar-fixed-top" role="navigation">
						<div class="container">
							<div class="navbar-collapse collapse">
								<ul class="nav navbar-nav navbar-right">
									<li><a href="http://reprezen.com" target="_blank">Created with <img class="logo" src="images/logo.png"></a></li>
								</ul>
							</div><!--/.nav-collapse -->
						</div>
					</div>
					<div class="container">
						<div class="row">
							«IF includeTOC»
								<div class="col-md-3">
									<div id="toc" class="reprezen-sidebar hidden-print affix">
									</div>
								</div>
							«ENDIF»
			
							<div class="col-md-9">
								<a class="anchor" id="swagger-spec"></a>
								<div class="page-header">
									<h1>Swagger Specification</h1>
								</div>
			
								<a class="anchor" id="«swagger.htmlId»"></a>
								«new TopMatter().get(swagger)»
								«if(showPaths) swagger.pathsHtml»
								«if(showParams) swagger.parametersHtml»
								«if(showResponses) swagger.responsesHtml»
								«if(showDefs) swagger.definitionsHtml»
							</div> <!-- /col-md-9 -->
						</div>  <!-- /row -->
					</div> <!-- /container -->
			
						<!-- Bootstrap core JavaScript
						================================================== -->
						<!-- Placed at the end of the document so the pages load faster -->
						<script src="«urlPrefix»jquery/jquery-1.10.2.min.js"></script>
						<script src="«urlPrefix»bootstrap/js/bootstrap.min.js"></script>
						<script>
						$(document).ready(function() {
							var visibleClass = "glyphicon-collapse-up";
							var hiddenClass = "glyphicon-collapse-down";
							var setTitle = function(visible) {
								if ($(this).attr("data-visible-title") != undefined) {
									$(this).attr("title", $(this).attr(visible ? "data-visible-title" : "data-hidden-title"));
									$(this).tooltip("destroy");
									$(this).tooltip();
								}
							};
							var toggleCollapseButton = function() {
								var button = $($(this).attr('data-controller'));
								button.toggleClass(visibleClass+" "+hiddenClass);
								setTitle.call(button,$(this).hasClass("in"));
							};
							var controls = $(".collapse[data-controller]")
							controls.on("shown.bs.collapse hidden.bs.collapse", toggleCollapseButton);
							controls.each(function(i,control) {
								setTitle.call($($(control).attr("data-controller")), $(control).hasClass("in"));
							});
							$('[data-toggle="tooltip"]').tooltip();
							$('div.markdown table').addClass("table").addClass("table-striped");
						});
						</script>
					«IF includeTOC»
						<script src="«urlPrefix»bootstrap/js/reprezenTOCBuilder.js"></script>
					«ENDIF»
				«BODY_BOTTOM.inject»
				</body>
			</html>
		'''
		html.removeUnwantedIndentation
	}

	def private htmlHeadSection(Swagger swagger, String urlPrefix, boolean preview) {
		'''
			<head>
			    «HEAD_TOP.inject»
			    <meta charset="utf-8">
			    <meta http-equiv="X-UA-Compatible" content="IE=edge">
			    <meta name="viewport" content="width=device-width, initial-scale=1.0">
			    <meta name="description" content="">
			    <meta name="author" content="">
			    <link rel="shortcut icon" href="«urlPrefix»docs-assets/ico/favicon.png">
			
			    <title>«swagger?.info?.title» Documentation</title>
			
			    <!-- Bootstrap core CSS -->
			    <link href="«urlPrefix»bootstrap/css/bootstrap.css" rel="stylesheet">
			    <link href="«urlPrefix»bootstrap/css/bootstrap-reprezen.css" rel="stylesheet">
			    «IF preview»
			    	<!-- Workaround for live preview problem with web-font loading -->
			    	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.0/css/bootstrap.min.css">
			    «ENDIF»
			
			    <!-- Custom styles for this template -->
			    <link href="«urlPrefix»bootstrap/css/navbar-fixed-top.css" rel="stylesheet">
			
			    <!-- Just for debugging purposes. Don't actually copy this line! -->
			    <!--[if lt IE 9]><script src="«urlPrefix»docs-assets/js/ie8-responsive-file-warning.js"></script><![endif]-->
			
			    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
			    <!--[if lt IE 9]>
			    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
			    <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
			    <![endif]-->
			    <style>
			    .tooltip-inner {
			        min-width: 100px;
			        max-width: 100%;
			    }
			    </style>
			    «HEAD_BOTTOM.inject»
			</head>
		'''
	}

	def private pathsHtml(Swagger swagger) {
		'''
			<a class="anchor toc-entry" id="swagger-paths" data-toc-level="0" data-toc-text="Paths"></a>
			<div class="page-header">
			    <h1>Path Specifications</h1>
			</div>
			<div class="panel panel-primary">
			    <div class="panel-heading">
			        <h3 class="panel-title">Path Specifications</h3>
			    </div>
			    <div class="panel-body">
			        «FOR path : swagger.paths?.keySet.sortByPosition(swagger.paths) ?: #[]»
			        	«new PathDoc(path).html»
			        «ENDFOR»
			    </div>
			</div>
		'''
	}

	def private parametersHtml(Swagger swagger) {
		'''
			<a class="anchor toc-entry" id="swagger-parameters" data-toc-level="0" data-toc-text="Parameters"></a>
			<div class="page-header">
			    <h1>Parameters</h1>
			</div>
			<div class="panel panel-primary">
			    <div class="panel-heading">
			        <h3 class="panel-title">Parameters</h3>
			    </div>
			    <div class="panel-body">
			        «FOR param : swagger.parameters.keySet.sortByPosition(swagger.parameters)»
			        	«new ParamDoc(swagger, param).html»
			        «ENDFOR»
			    </div>
			</div>
		'''
	}

	def private responsesHtml(Swagger swagger) {
		'''
			<a class="anchor toc-entry" id="swagger-responses" data-toc-level="0" data-toc-text="Responses"></a>
			<div class="page-header">
			    <h1>Responses</h1>
			</div>
			<div class="panel panel-primary">
			    <div class="panel-heading">
			        <h3 class="panel-title">Responses</h3>
			    </div>
			    <div class="panel-body">
			        «FOR response : swagger.responses.keySet.sortByPosition(swagger.responses)»
			        	«new ResponseDoc(response).html»
			        «ENDFOR»
			    </div>
			</div>
		'''
	}

	def private definitionsHtml(Swagger swagger) {
		'''
			<a class="anchor toc-entry" id="swagger-definitions" data-toc-level="0" data-toc-text="Schema Definitions"></a>
			<div class="page-header">
			    <h1>Schema Definitions</h1>
			</div>
			<div class="panel panel-primary">
			    <div class="panel-heading">
			        <h3 class="panel-title">Schema Definitions</h3>
			    </div>
			    <div class="panel-body">
			        «FOR definition : swagger.definitions.keySet.sortByPosition(swagger.definitions)»
			        	«new ModelDoc(definition).html»
			        «ENDFOR»
			    </div>
			</div>
		'''
	}

	val indentedPreBlock = Pattern.compile("^(\\s*)<pre(\\s+.*)?>.*$",
		Pattern.CASE_INSENSITIVE + Pattern.DOTALL) // DOTALL so we match trailing \r chars

	def private removeUnwantedIndentation(String html) {
		return html.split("\n").removeUnwantedIndentation
	}

	def private removeUnwantedIndentation(List<String> lines) {
		var i = 0
		val unindentedLines = Lists.<String>newArrayList
		while (i < lines.size) {
			var line = lines.get(i)
			var matcher = indentedPreBlock.matcher(line)
			if (matcher.matches) {
				var indentation = matcher.group(1)
				var indentLen = matcher.end(1)
				while (i < lines.size && line.startsWith(indentation)) {
					unindentedLines.add(line.substring(indentLen))
					i = i + 1
					line = if(i < lines.size) lines.get(i) else ""
				}
			} else {
				unindentedLines.add(lines.get(i))
				i = i + 1
			}
		}
		unindentedLines.join("\n")
	}
}
