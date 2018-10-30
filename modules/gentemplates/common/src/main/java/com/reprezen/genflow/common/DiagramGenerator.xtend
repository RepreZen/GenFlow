/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common

import com.google.common.base.Strings
import com.reprezen.genflow.api.outputitem.AbstractOutputItem
import com.reprezen.genflow.api.template.GenTemplate
import java.util.Map

import static com.reprezen.genflow.common.HtmlInjections.*

abstract class DiagramGenerator<T> extends AbstractOutputItem<T, T> {

	static public val JSON_OUTPUT_ITEM_NAME = "JSON"

	static val URL_PREFIX_PARAM = 'urlPrefix'
	static val PREVIEW_PARAM = 'preview'
	static val FIRST_RUN = 'firstRun'
	extension HtmlInjections htmlInjections

	static val int DEFAULT_TRANSITION_DURATION = 300

	def String generate(CharSequence data, String modelName) {
		htmlInjections = HtmlInjections::fromContext(context)
		val parameters = context.getGenTargetParameters();
		val urlPrefix = getStringParameter(URL_PREFIX_PARAM, null)

		var pathToJson = '''«urlPrefix»/data/«modelName».js''';
		val pathToJsonParamName = GenTemplate::getParamNameFor(JSON_OUTPUT_ITEM_NAME)
		if (parameters.containsKey(GenTemplate::OUTPUT_FILES_PARAM) &&
			parameters.get(GenTemplate::OUTPUT_FILES_PARAM) instanceof Map<?, ?>) {
			val Map<?, ?> outputParams = parameters.get(GenTemplate::OUTPUT_FILES_PARAM) as Map<?, ?>;
			if (outputParams.containsKey(pathToJsonParamName) &&
				outputParams.get(pathToJsonParamName) instanceof String) {
				pathToJson = outputParams.get(pathToJsonParamName) as String
			}
		}

		val int transitionDuration = if('false' == parameters.get(FIRST_RUN)) 0 else DEFAULT_TRANSITION_DURATION

		'''
			<!DOCTYPE html>
			<html lang="en">
			<head>
				«HEAD_TOP.inject»
				<meta charset="utf-8">
				<meta http-equiv="X-UA-Compatible" content="IE=9" />
			
				<title>«modelName»</title>
				<script src="«urlPrefix»lib/d3.v3.min.js" type="text/javascript"></script>
				<script src="«urlPrefix»script/core.js"></script>
			
				<script type="text/javascript" src="«urlPrefix»lib/jquery-1.8.1.js" charset="utf-8"></script>
				<link rel="stylesheet" type="text/css" media="all" href="«urlPrefix»lib/bootstrap/css/bootstrap.css" />
				<script type="text/javascript" src="«urlPrefix»lib/bootstrap/bootstrap.js" charset="utf-8"></script>
			
				<script src="«urlPrefix»lib/bootstrap-checkbox.js" type="text/javascript" charset="utf-8"></script>
				<link  href="«urlPrefix»lib/bootstrap-checkbox.css"  rel="stylesheet" type="text/css" media="all"/>
			
				<link  href="«urlPrefix»css/main.css"  rel="stylesheet" type="text/css" media="all"/>
			
				«IF data === null»
					<script src="«pathToJson»"></script>
				«ENDIF»
				«IF !preview»
					<script src="«urlPrefix»script/cookies.js"></script>
				«ENDIF»
			
				<script src="«urlPrefix»script/viewmap-utils.js"></script>
				<script src="«urlPrefix»script/draw2d.js"></script>
				<script src="«urlPrefix»script/diagram-specifics.js"></script>
				<script src="«urlPrefix»script/layout-impl.js"></script>
				<script src="«urlPrefix»script/viewmaps.js"></script>
			
				<script src="«urlPrefix»script/chart.js"></script>
				  <script src="«urlPrefix»script/chart-flow-jquery.js"></script>
				<script src="«urlPrefix»script/layout.js"></script>
				<script src="«urlPrefix»script/layout-flow.js"></script>
				«HEAD_BOTTOM.inject»
			</head>
			
			<body>
			«BODY_TOP.inject»
			<div id="container">
				<div id="flowtree" class="flowtree"></div>
			</div>
			
			<div style="margin-left: 40px; position: absolute; top: 10px;">
				<div class="btn-group">
					<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
						Show Details
						<span class="caret"></span>
					</button>
					<ul class="dropdown-menu">
						<li><input id="R" class="box" type="checkbox" value="R"> Resources
							<ul>
								<li><input id="DT" class="box" type="checkbox" value="DT"> Data Types
								<ul>
									<li><input id="RL" class="box" type="checkbox" value="RL"> Reference Links </li>
								</ul>
								</li>
			
								<li><input id="MTD" class="box" type="checkbox" value="MTD" checked> Methods</li>
							</ul>
						</li>
					</ul>
				</div>
			</div>
			«IF !preview»
				<div style="position: absolute; top: 20px; right: 15px;">
					<a href="http://reprezen.com" target="_blank">Created with <img style="margin-top: -8px;" src="«urlPrefix»images/logo.png"></a>
				</div>
			«ENDIF»
			<!-- Setup visibility control -->
			<script src="«urlPrefix»script/dropdownSetting.js"></script>
			
			
			<script type="text/javascript">
				«IF !preview»
					var url_prefix = '';
					var data = «IF data !== null»«data»«ELSE»getJSON()«ENDIF»;
					function chartChangeSelection(id, type) {
						console.log(id + ' : ' + type);
					};
				«ELSE»
					function chartChangeSelection(id, type) {
						reprezen_changeSelection(id, type);
					};
					var url_prefix = '«urlPrefix»';
					var data = «data»;
				«ENDIF»
				(function($) {
					$(document).ready(function() { 
						flowTree = d3.custom.chart.flow(«transitionDuration»);
						d3.select('#flowtree')
							.datum(data.ResourceAPI)
							.call(flowTree);
					})
						})(jQuery);
					</script>
					«IF preview»
						<script>
							function scrollToAnchor(anchorName) {
								try {
									d3.select('svg').selectAll('.selected').classed('selected', false);
									d3.select('[anchorId="' + anchorName.substr(1) + '"]').classed('selected', true);
							} catch (e) {}
						}
						</script>
					«ENDIF»
					«BODY_BOTTOM.inject»
					</body>
					</html>
				   '''
	}

	def boolean isPreview() {
		true == context.getGenTargetParameters().get(PREVIEW_PARAM)
	}

	def protected String getStringParameter(String paramName, String defaultValue) {
		val value = context.getGenTargetParameters().get(paramName)
		if (value instanceof String && !Strings::isNullOrEmpty(value as String)) {
			return value as String
		}
		return defaultValue;
	}

}
