/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc.xtend

import com.reprezen.genflow.common.services.MethodServices
import com.reprezen.genflow.common.xtend.XImportHelper
import com.reprezen.genflow.common.xtend.XParameterHelper
import com.reprezen.genflow.common.xtend.XSecuritySchemeImportHelper
import com.reprezen.rapidml.SecurityScheme
import com.reprezen.rapidml.SecuritySchemeLibrary
import com.reprezen.rapidml.TypedResponse
import com.reprezen.rapidml.ZenModel

class XGenerateSecuritySchemes {
    extension XDocHelper docHelper
    val paramHelper = new XParameterHelper
    val XImportHelper importHelper
    val XSecuritySchemeImportHelper securitySchemeImportHelper
    
    new(XImportHelper importHelper, XSecuritySchemeImportHelper securitySchemeImportHelper, XDocHelper docHelper) {
        this.importHelper = importHelper
        this.securitySchemeImportHelper = securitySchemeImportHelper
        this.docHelper = docHelper
    }
    
    def String generateSecuritySchemes(ZenModel model) {
        '''
			«IF model.securitySchemesLibrary !== null»
				«««             <a class="anchor" id="«	securityScheme.htmlLink»"></a>
			v class="panel panel-primary">
	  <div class="panel-heading">
	    <h3 class="panel-title">«model.securitySchemesLibrary.name»</h3>
				se</div>
				se<div class="panel-body">
				se  «FOR securityScheme : model.securitySchemesLibrary.securitySchemes»
				se  	«generateSecurityScheme(securityScheme)»
				se  «ENDFOR»
				se</div>
				</div>
			«ENDIF»
			«FOR schemeModel : securitySchemeImportHelper.importedTypes.keySet»
				«generateImportedSecuritySchemes(schemeModel)»
			«ENDFOR»
		            '''
    }

    def protected generateSecurityScheme(SecurityScheme securityScheme) {
        '''
			 <a class="anchor" id="«securityScheme.htmlLink»"></a>
			 <div class="panel panel-default">
			  <div class="panel-heading">
			    <h3 class="panel-title">
			      <span class="glyphicon glyphicon-lock"></span>
			      <code>«securityScheme.name»</code> 
			      <small>(«securityScheme.type»)</small>
			    </h3>
			  </div>
			  <div class="panel-body">
			    «securityScheme.generateDocItem»
			    
			 <code>Method Invocation</code>
			 <ul class="list-group">
			 «IF !securityScheme.parameters.empty»
			 	<li class="list-group-item">
			 	    «XDocHelper.tableWithHeader("Authorization Parameters", "Name", "Location", "Type", "Documentation")»
			 	    «FOR param : securityScheme.parameters»
			 	    	<tr>
			 	    	    <td>«param.name»</td>
			 	    	    <td>«param.type.toString»</td>
			 	    	    <td>«paramHelper.paramType(param.sourceReference, importHelper)»</td>
			 	    	    <td>«param?.generateDoc»</td>
			 	    	</tr>
			 	    «ENDFOR»
			 	</table>
			 	</li>
			 «ENDIF»
			«IF !securityScheme.errorResponses.empty»
				<li class="list-group-item">
				    «XDocHelper.tableWithHeader("Error Responses", "Code", "Description", "Documentation")»
				    «FOR response : securityScheme.errorResponses»
				    	«generateErrorResponse(response)»
				    «ENDFOR»
				    </table>
				</li>
			«ENDIF» 
			 </ul>
			 
			 «IF !securityScheme.scopes.empty»
				«XDocHelper.tableWithHeader("Defined Scopes", "Name", "Documentation")»
				«FOR scope : securityScheme.scopes»
					<tr>
					    <td>«scope.name»</td>
					    <td>«scope.documentation?.text»</td>
					</tr>
				«ENDFOR»
				</table>
			 «ENDIF»
			 «IF !securityScheme.settings.empty»
				«XDocHelper.tableWithHeader("Settings", "Name", "Documentation", "Value")»
				«FOR setting : securityScheme.settings»
					<tr>
					    <td>«setting.name»</td>
					    <td>«setting.documentation?.text»</td>
					    <td>«setting.value»</td>
					</tr>
				«ENDFOR»
				</table>
			 «ENDIF»
			     
			     </div>
			 </div>
		'''
    }
    
   def private generateErrorResponse(TypedResponse response) {
        '''
			<tr>
			 Error Response
			 <td><span class="label label-«XGenerateInterfaces.statusColorCode(response.statusCode)»">«response.statusCode»</span></td>
			 <td>«MethodServices.getResponseStatusCodeDescription(response.statusCode)»</td>
			 <td>«response.generateDoc»</td>
			 </tr>
		     '''
    }
				
    def private generateImportedSecuritySchemes(SecuritySchemeLibrary model) {
	'''<a class="anchor" id="«model.htmlLink»"></a>
        <div class="panel panel-primary">
          <div class="panel-heading">
            <h3 class="panel-title">«securitySchemeImportHelper.getModelQualifiedName(model)»</h3>
          </div>
          <div class="panel-body">
            «FOR dataType : securitySchemeImportHelper.importedTypes.get(model)»
                «generateSecurityScheme(dataType)»
            «ENDFOR»
          </div>
        </div>'''
    }

	def boolean hasSecuritySchemes(ZenModel model) {
		val library = model.securitySchemesLibrary

		if (library !== null) !library.securitySchemes.isEmpty else false
	}
 
}
