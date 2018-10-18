/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.doc

import io.swagger.models.Swagger

class TopMatter {
    extension DocHelper = HelperHelper.docHelper

    def get(Swagger swagger) {
        '''
            <div class="panel panel-primary">
              <div class="panel-heading">
                <h3 class="panel-title">«swagger.info?.title» «swagger.baseUri»</h3>
              </div>
              <div class="panel-body restful-interface">
                «swagger.info?.description?.docHtml»
                <table class="table">
                    <tr><th>Version</th><td>«swagger.info?.version»</td></tr>
                    <tr><th>Contact</th><td>«swagger.contactInfoHtml»</td></tr>
                    <tr><th>Terms of Service</th><td>«swagger.info?.termsOfService.docHtml»</td></tr>
                    <tr><th>License</th><td>«swagger.licenseInfoHtml»</td></tr>
                </table>
              </div>
            </div>
        '''
    }

    def private getScheme(Swagger swagger) {
        val schemes = swagger.schemes.map[it.toString.toLowerCase] // Swagger model api uses ugly upper-case scheme names
        for (preferredScheme : #["https", "http"]) {
            if (schemes.contains(preferredScheme)) {
                return preferredScheme
            }
        }
        if(schemes.size > 0) swagger.schemes.get(0) else "http"
    }

    def private getBaseUri(Swagger swagger) {
        '''<small>(«swagger.getScheme»://«swagger.host»«swagger.basePath»)</small>'''
    }

    def private getContactInfoHtml(Swagger swagger) {
        val contact = swagger.info?.contact
        if (contact != null) {
            val primaryText = contact.name ?: contact.url
            val primary = if (contact.url != null) '''<a href="«contact.url»">«primaryText»</a>''' else primaryText
            val email = if (contact.email != null) '''<a href="mailto:«contact.email»">«contact.email»</a>'''
            '''«primary» «email»'''
        }
    }

    def private getLicenseInfoHtml(Swagger swagger) {
        val license = swagger.info?.license
        if (license != null) {
            val text = license.name ?: license.url
            if (license.url != null) '''<a href="«license.url»">«text»</a>''' else text
        }
    }
}
