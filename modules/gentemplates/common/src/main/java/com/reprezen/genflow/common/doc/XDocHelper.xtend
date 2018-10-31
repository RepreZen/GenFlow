/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.doc

import com.reprezen.rapidml.Constraint
import com.reprezen.rapidml.DataModel
import com.reprezen.rapidml.Documentable
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.LengthConstraint
import com.reprezen.rapidml.RegExConstraint
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ValueRangeConstraint
import com.reprezen.rapidml.ZenModel
import java.io.File
import java.net.URI
import java.util.List
import java.util.regex.Pattern
import org.apache.commons.text.StringEscapeUtils
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil

class XDocHelper {

	static val extensions = #[TablesExtension.create, AutolinkExtension.create]
	static val parser = Parser.builder.extensions(extensions).build
	static val htmlRenderer = HtmlRenderer.builder.extensions(extensions).build

	val URI baseUri

	new(URI baseUri) {
		this.baseUri = baseUri
	}

	def generateDoc(Documentable documentable) {
		documentable?.documentation?.text?.generateDoc
	}

	def generateDoc(String text) {
		text?.markdown
	}

	def private markdown(String text) {
		val html = htmlRenderer.render(parser.parse(text.preprocess)).toString
		html.replaceAll("<table>", "<table class=\"table\">")
	}

	val static NON_DOT_LINE = Pattern.compile("(?m)^[^.]")

	def private preprocess(String text) {
		if (NON_DOT_LINE.matcher(text).find()) {
			text
		} else {
			text.replaceAll("(?m)^[.]", "");
		}
	}

	def String htmlEscape(String value) {
		StringEscapeUtils::escapeHtml4(value)
	}

	def generateDocItem(String text) {
		'''<div class="text-success">«text.generateDoc»</div>'''
	}

	def generateDocItem(Documentable documentable) {
		documentable?.documentation?.text?.generateDocItem
	}

	def String htmlLink(EObject obj) {
		val objUri = EcoreUtil::getURI(obj)
		val fileString = objUri.toFileString
		var fixedUri = if (fileString !== null) {
				var rel = baseUri.relativize(new File(fileString).toURI)
				new URI(rel.scheme, rel.schemeSpecificPart, objUri.fragment)
			} else {
				objUri
			}
		return "anchor:" + fixedUri.toString.sanitizeLink
	}

	def private sanitizeLink(String link) {
		link.replaceAll("[^a-zA-Z0-9_]", "_")
	}

	def nameOrTitle(ZenModel element) {
		element.title ?: element.name
	}

	def nameOrTitle(ResourceAPI element) {
		element.title ?: element.name
	}

	def nameOrTitle(DataModel element) {
		element.title ?: element.name
	}

	/**
	 * Data type properties are shown as rows in a table, elements that don't have their own anchor. Therefore, reusing the link of the containing data type
	 */
	def String htmlLinkReferenceProp(Feature feature) {
		htmlLink(feature.containingDataType)
	}

	def generateInlineConstraints(List<Constraint> constraints) {
		'''
			«IF !constraints.empty»
				<table class="table table-condensed table-constraints">
					<tr>
						<th colspan="2">Constraints</th>
					</tr>
				«FOR constraint : constraints»
					<tr>
						<td>«constraint.constraintType»</td>
						<td>«constraint.constraintValue»</td>
					</tr>
				«ENDFOR»
				</table>
			«ENDIF»
		'''
	}

	def generateConstraints(List<Constraint> constraints) {
		'''
			«IF !constraints.empty»
				<h4>Constraints</h4>
				<table class="table table-condensed">
				«FOR constraint : constraints»
					<tr>
						<td>«constraint.constraintType»</td>
						<td>«constraint.constraintValue»</td>
					</tr>
				«ENDFOR»
				</table>
			«ENDIF»
		'''
	}

	def getConstraintType(Constraint constraint) {
		switch (constraint) {
			LengthConstraint: 'String Length'
			RegExConstraint: 'Regular Expression'
			ValueRangeConstraint: 'Value Range'
			: ''
		}
	}

	def getConstraintValue(Constraint constraint) {
		switch (constraint) {
			LengthConstraint:
				if (constraint.setMinLength)
					'from ' + (if(!constraint.setMaxLength) 'minimum ' else '') + constraint.minLength +
						(if(constraint.setMaxLength) ' to ' + constraint.maxLength else '')
				else
					'up to ' + constraint.maxLength
			RegExConstraint: '''matching regex "«constraint.pattern»"'''
			ValueRangeConstraint: {
				val maxExcStr = if (constraint.minValueExclusive || constraint.maxValueExclusive)
						if(constraint.maxValueExclusive) ' exclusive' else ' inclusive'
					else
						''
				val minExcStr = if (constraint.minValueExclusive || constraint.maxValueExclusive)
						if(constraint.minValueExclusive) ' exclusive' else ' inclusive'
					else
						''

				if (constraint.minValue !== null)
					'from ' + (if(constraint.maxValue === null) 'minimum ' else '') + constraint.minValue + minExcStr +
						(if(constraint.maxValue !== null) ' to ' + constraint.maxValue + maxExcStr else '')
				else
					'up to' + constraint.maxValue + maxExcStr
			}
			:
				''
		}
	}

	static def tableWithHeader(String tableName, String... columns) {
		'''
			<h4>«tableName»</h4>
			<table class="table table-condensed">
			    <tr>
			    «FOR column : columns»
			    	<th>«column»</th>
			  «ENDFOR»
			   </tr>
		'''
	}
}
