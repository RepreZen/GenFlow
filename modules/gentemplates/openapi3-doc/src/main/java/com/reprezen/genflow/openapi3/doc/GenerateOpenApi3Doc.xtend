package com.reprezen.genflow.openapi3.doc

import com.reprezen.genflow.api.openapi3.OpenApi3OutputItem
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.util.ArrayList
import java.util.List
import java.util.regex.Pattern

class GenerateOpenApi3Doc extends OpenApi3OutputItem {

	public static val URL_PREFIX_PARAM = 'urlPrefix'
	public static val ANCHOR_NAME_PARAM = 'anchorName'
	public static val PREVIEW_PARAM = 'preview'

	extension OptionHelper optionHelper
	extension HtmlHelper htmlHelper
	extension MiscHelper miscHelper

	override generate(OpenApi3 model) {
		try {
			HelperHelper.open(model, context)
			optionHelper = HelperHelper.optionHelper
			htmlHelper = HelperHelper.htmlHelper
			miscHelper = HelperHelper.miscHelper
			doGenerate(model)
		} finally {
			HelperHelper.close()
		}
	}

	def doGenerate(OpenApi3 model) {
		val urlPrefix = getUrlPrefix
		val startTime = System.nanoTime
		val showPaths = !model.paths.empty
		val showDefs = (!preview || !showPaths) && !model.schemas.empty
		val showParams = (!preview || !showPaths) && !model.parameters.empty
		val showResponses = (!preview || !showPaths) && !model.responses.empty
		val includeTOC = !preview && isIncludeTOC

		val html = '''
			<!DOCTYPE html>
			<html lang="en">
			    <!-- Preview: «preview» -->
			    «htmlHeadSection(model, urlPrefix, preview)»
			    <body data-spy="scroll" data-target="#toc">            
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
			                   <a class="anchor" id="model-spec"></a>
			                   <div class="page-header">
			                       <h1>OpenApi3 Specification</h1>
			                   </div>
			    
			                    <a class="anchor" id="«model.htmlId»"></a>
			                    «new TopMatter().get(model)»
			                    «if(showPaths) model.pathsHtml»
			                    «if(showParams) model.parametersHtml»
			                    «if(showResponses) model.responsesHtml»
			                    «if(showDefs) model.definitionsHtml»
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
			    </body>
			    <!-- Generation time: «(System.nanoTime - startTime).elapsedTime» -->
			</html>
		'''
		html.removeUnwantedIndentation
	}

	def private elapsedTime(long nano) {
		val msec = nano / 1000000
		String.format("%d.%03d", msec / 1000, msec % 1000)
	}

	def private htmlHeadSection(OpenApi3 model, String urlPrefix, boolean preview) {
		'''
			<head>
			    <meta charset="utf-8">
			    <meta http-equiv="X-UA-Compatible" content="IE=edge">
			    <meta name="viewport" content="width=device-width, initial-scale=1.0">
			    <meta name="description" content="">
			    <meta name="author" content="">
			    <link rel="shortcut icon" href="«urlPrefix»docs-assets/ico/favicon.png">
			
			    <title>«model?.info?.title» Documentation</title>
			
			    <!-- Bootstrap core CSS -->
			    <link href="«urlPrefix»bootstrap/css/bootstrap.css" rel="stylesheet">
			    <link href="«urlPrefix»bootstrap/css/bootstrap-reprezen.css" rel="stylesheet">
			
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
			</head>
		'''
	}

	def private pathsHtml(OpenApi3 model) {
		'''
			<a class="anchor toc-entry" id="model-paths" data-toc-level="0" data-toc-text="Paths"></a>
			<div class="page-header">
			    <h1>Path Specifications</h1>
			</div>
			<div class="panel panel-primary">
			    <div class="panel-heading">
			        <h3 class="panel-title">Path Specifications</h3>
			    </div>
			    <div class="panel-body">
			        «FOR path : model.paths?.keySet.sortByPosition(model.paths) ?: #[]»
			        	«new PathDoc(path).html»
			        «ENDFOR»
			    </div>
			</div>
		'''
	}

	def private parametersHtml(OpenApi3 model) {
		'''
			<a class="anchor toc-entry" id="model-parameters" data-toc-level="0" data-toc-text="Parameters"></a>
			<div class="page-header">
			    <h1>Parameters</h1>
			</div>
			<div class="panel panel-primary">
			    <div class="panel-heading">
			        <h3 class="panel-title">Parameters</h3>
			    </div>
			    <div class="panel-body">
			        «FOR param : model.parameters.keySet.sortByPosition(model.parameters)»
			        	«new ParamDoc(model, param).html»
			        «ENDFOR»
			    </div>
			</div>
		'''
	}

	def private responsesHtml(OpenApi3 model) {
		'''
			<a class="anchor toc-entry" id="model-responses" data-toc-level="0" data-toc-text="Responses"></a>
			<div class="page-header">
			    <h1>Responses</h1>
			</div>
			<div class="panel panel-primary">
			    <div class="panel-heading">
			        <h3 class="panel-title">Responses</h3>
			    </div>
			    <div class="panel-body">			    
			        «FOR response : model.responses.keySet.sortByPosition(model.responses)»
			        	«new ResponseDoc(response).html»
			        «ENDFOR»
			    </div>
			</div>
		'''
	}

	def private definitionsHtml(OpenApi3 model) {
		'''
			<a class="anchor toc-entry" id="model-definitions" data-toc-level="0" data-toc-text="Schema Definitions"></a>
			<div class="page-header">
			    <h1>Schema Definitions</h1>
			</div>
			<div class="panel panel-primary">
			    <div class="panel-heading">
			        <h3 class="panel-title">Schema Definitions</h3>
			    </div>
			    <div class="panel-body">
			        «FOR definition : model.schemas.keySet.sortByPosition(model.schemas)»
			        	«new ModelDoc(definition).html»
			        «ENDFOR»
			    </div>
			</div>
		'''
	}

	val private indentedPreBlock = Pattern.compile("^(\\s*)<pre\\s+[^>]*class=\"remove-xtend-indent\"[^>]*>.*$",
		Pattern.CASE_INSENSITIVE + Pattern.DOTALL) // DOTALL so we match trailing \r chars

	def private removeUnwantedIndentation(String html) {
		return html.split("\n").removeUnwantedIndentation
	}

	def private removeUnwantedIndentation(List<String> lines) {
		var i = 0
		val unindentedLines = new ArrayList<String>
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
