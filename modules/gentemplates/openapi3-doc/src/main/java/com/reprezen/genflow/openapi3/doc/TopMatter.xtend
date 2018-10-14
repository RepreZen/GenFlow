/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi3.doc

import com.reprezen.kaizen.oasparser.model3.OpenApi3

class TopMatter {
	extension DocHelper = HelperHelper.docHelper

	def get(OpenApi3 model) {
		'''
			<div class="panel panel-primary">
			  <div class="panel-heading">
			    <h3 class="panel-title">«model.info?.title» «model.baseUri»</h3>
			  </div>
			  <div class="panel-body restful-interface">
			    «model.info?.description?.docHtml»
			    <table class="table">
			        <tr><th>Version</th><td>«model.info?.version»</td></tr>
			        <tr><th>Contact</th><td>«model.contactInfoHtml»</td></tr>
			        <tr><th>Terms of Service</th><td>«model.info?.termsOfService.docHtml»</td></tr>
			        <tr><th>License</th><td>«model.licenseInfoHtml»</td></tr>
			    </table>
			  </div>
			</div>
		'''
	}

// FIXME adapt to OAS3
//    def private getScheme(OpenApi3 model) {
//        val schemes = model.schemes.map[it.toString.toLowerCase] // OpenApi3 model api uses ugly upper-case scheme names
//        for (preferredScheme : #["https", "http"]) {
//            if (schemes.contains(preferredScheme)) {
//                return preferredScheme
//            }
//        }
//        if(schemes.size > 0) model.schemes.get(0) else "http"
//    }
	def private getBaseUri(OpenApi3 model) {
// FIXME adapt to OAS3
//        '''<small>(«model.getScheme»://«model.host»«model.basePath»)</small>'''
	}

	def private getContactInfoHtml(OpenApi3 model) {
		val contact = model.info?.contact
		if (contact !== null) {
			val primaryText = contact.name ?: contact.url
			val primary = if (contact.url !== null) '''<a href="«contact.url»">«primaryText»</a>''' else primaryText
			val email = if (contact.email !== null) '''<a href="mailto:«contact.email»">«contact.email»</a>'''
			'''«primary» «email»'''
		}
	}

	def private getLicenseInfoHtml(OpenApi3 model) {
		val license = model.info?.license
		if (license !== null) {
			val text = license.name ?: license.url
			if (license.url !== null) '''<a href="«license.url»">«text»</a>''' else text
		}
	}
}
