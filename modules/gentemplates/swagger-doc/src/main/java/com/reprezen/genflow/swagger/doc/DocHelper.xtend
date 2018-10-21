/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.doc

import org.pegdown.Extensions
import org.pegdown.PegDownProcessor

class DocHelper implements Helper {

	extension HtmlHelper htmlHelper

	override init() {
		htmlHelper = HelperHelper.htmlHelper
	}

	var static extensions = Extensions.SMARTS + Extensions.QUOTES + Extensions.AUTOLINKS + Extensions.TABLES +
		Extensions.FENCED_CODE_BLOCKS + Extensions.STRIKETHROUGH + Extensions.ATXHEADERSPACE
	extension PegDownProcessor = new PegDownProcessor(extensions)

	def String getDocHtml(String doc) {
		doc?.nonEmpty?.toString?.processMarkdown.wrap
	}

	def getDocHtml(String summary, String doc) {
		#[summary, doc].filterNull.join("\n\n")?.processMarkdown.wrap
	}

	def nonEmpty(String text) {
		if(text.trim.isEmpty) null else text
	}

	def private String wrap(String doc) {
		'''<div class="markdown">«doc»</div>'''
	}

	def private String processMarkdown(String md) {
		for (i : 1 .. 3) {
			try {
				return md.markdownToHtml
			} catch (Exception e) {
			}
		}
		return '''Markdown processing error; raw documentation:<div class="well">«md.htmlEscape»</div>'''
	}
}
