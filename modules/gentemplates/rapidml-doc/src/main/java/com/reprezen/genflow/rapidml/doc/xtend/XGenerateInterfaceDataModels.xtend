/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc.xtend

import com.reprezen.genflow.api.zenmodel.util.CommonServices
import com.reprezen.genflow.common.xtend.XImportHelper
import com.reprezen.genflow.common.xtend.XParameterHelper
import com.reprezen.restapi.DataModel
import com.reprezen.restapi.Enumeration
import com.reprezen.restapi.Feature
import com.reprezen.restapi.PrimitiveProperty
import com.reprezen.restapi.ReferenceProperty
import com.reprezen.restapi.Structure
import com.reprezen.restapi.UserDefinedType
import com.reprezen.restapi.ZenModel

class XGenerateInterfaceDataModels {
	extension XDocHelper docHelper
	val commonServices = new CommonServices
	val paramHelper = new XParameterHelper
	val XImportHelper importHelper

	new(XImportHelper importHelper, XDocHelper docHelper) {
		this.importHelper = importHelper
		this.docHelper = docHelper
	}

	def String generateInterfaceDataModels(ZenModel model) {
		'''
			«FOR interfaceDataModel : model.dataModels»
				«generateInterfaceDataModels(interfaceDataModel, false)»
			«ENDFOR»
			«FOR interfaceDataModel : importHelper.importedTypes.keySet»
				«generateInterfaceDataModels(interfaceDataModel, true)»
			«ENDFOR»
		'''
	}

	def private generateInterfaceDataModels(DataModel dataModel, boolean imported) {
		val alias = importHelper.getAlias(dataModel)
		val aliasNote = if (alias !== null) ''' [alias&nbsp;<em>«alias»</em>]'''
		'''
			<a class="anchor" id="«dataModel.htmlLink»"></a>
			<div class="panel panel-primary">
			  <div class="panel-heading">
			    <h3 class="panel-title"><strong>«dataModel.nameOrTitle»</strong></h3>
			    <p>«importHelper.getModelFullQualifiedName(dataModel)»«aliasNote»</p>
			  </div>
			  <div class="panel-body">
			    «dataModel.generateDocItem»
			    «FOR dataType : if (imported) importHelper.importedTypes.get(dataModel) else dataModel.ownedDataTypes»
			    	«generateType(dataType)»
			    «ENDFOR»
			  </div>
			</div>
		'''
	}

	/**
     * Generate complex type
     */
	def dispatch private String generateType(Structure dataType) {
		'''
			<a class="anchor" id="«dataType.htmlLink»" data-zenname="«dataType.name»"></a>
			<div class="panel panel-default">
			  <div class="panel-heading">
			    <h3 class="panel-title">
			      <span class="glyphicon glyphicon-tasks"></span>
			      <code>«dataType.name»</code> 
			    </h3>
			  </div>
			  <div class="panel-body">
			    «dataType.generateDocItem»
			    «IF !dataType.ownedFeatures.empty»
			    	<h4>Properties</h4>
			    	<table class="table table-condensed">
			    	    <tr>
			    	        <th>Name</th>
			    	        <th>Type</th>
			    	        <th>Documentation</th>
			    	    </tr>
			    	«FOR feature : dataType.ownedFeatures»
			    		<tr>
			    		    <td>«feature.name»</td>
			    		    <td>«generatePropertyType(feature)»</td>
			    		    <td>«feature.generateDoc»</td>
			    		</tr>
			    	«ENDFOR»
			    	</table>
			    «ENDIF»
			    «IF !dataType.ownedOperations.empty»
			    	<h4>Operations</h4>
			    	<table class="table table-condensed">
			    	  <tr>
			    	    <th>Name</th>
			    	    <th>Documentation</th>
			    	  </tr>
			    	  «FOR operation : dataType.ownedOperations»
			    	  	<tr>
			    	  	  <td>«operation.name»</td>
			    	  	  <td>«operation.generateDoc»</td>
			    	  	<tr>
			    	  «ENDFOR»
			    	</table>
			    «ENDIF»
			    «IF !dataType.ownedElements.empty»
			    	<h4>Owned Elements</h4>
			    	«FOR ownedDataType : dataType.ownedElements»
			    		«generateType(ownedDataType)»
			    	«ENDFOR»
			    «ENDIF»
			  </div>
			</div>
		'''
	}

	/**
     * Generate enumeration.
     */
	def dispatch private String generateType(Enumeration dataType) {
		'''
			<a class="anchor" id="«dataType.htmlLink»" data-zenname="«dataType.name»"></a>
			<div class="panel panel-default">
			  <div class="panel-heading">
			    <h3 class="panel-title">
			      <span class="glyphicon glyphicon-tasks"></span> «dataType.baseType.name» Enumeration<code>«dataType.
				name»</code> 
			    </h3>
			  </div>
			  <div class="panel-body">
			    «dataType.generateDocItem»
			    «IF !dataType.enumConstants.empty»
			    	<h4>Enumeration Constants</h4>
			    	    <table class="table table-condensed">
			    	        <tr>
			    	            <th>Name</th>
			    	            <th>Value</th>
			    	            <th>Documentation</th>
			    	        </tr>
			    	          «FOR enumConstant : dataType.enumConstants»
			    	          	<tr>
			    	          	  <td>«enumConstant.name»</td>
			    	          	  <td>«IF enumConstant.literalValue !== null»«enumConstant.literalValue»«ELSE»«enumConstant.
				integerValue»«ENDIF»</td>
			    	          	  <td>«enumConstant.generateDoc»</td>
			    	          	</tr>
			    	          «ENDFOR»
			    	    </table>
			    «ENDIF»
			  </div>
			</div>
		'''
	}

	def dispatch private String generateType(UserDefinedType dataType) {
		'''
			<a class="anchor" id="«dataType.htmlLink»" data-zenname="«dataType.name»"></a>
			<div class="panel panel-default">
			<div class="panel-heading">
			          <h3 class="panel-title">
			            <span class="glyphicon glyphicon-tasks"></span><code>«dataType.name»</code>
				  <span class="glyphicon glyphicon-chevron-right"></span> <a href="#«dataType.baseType.htmlLink»">«dataType.
				baseType.name»</a> 
				      </h3>
				       </div>
				       <div class="panel-body">
			  «dataType.generateDocItem»
				«dataType.allConstraints.generateConstraints»
				      </div>
				     </div>
			    '''
	}

	/**
     * Generates property type for ReferenceProperty and PrimitiveProperty
     */
	def dispatch private generatePropertyType(ReferenceProperty property) {
		'''
			«IF property.containment»
				containing
			«ENDIF»
			«IF property.container»
				container
			«ENDIF»
			<a href="#«property.type.htmlLink»">«generateFeatureType(property)»</a>
			«IF property.inverse !== null»
				<br> inverse of <a href="#«property.type.htmlLink»">«property.inverse.name»</a>
			«ENDIF»
		'''
	}

	def dispatch private generatePropertyType(PrimitiveProperty property) {
		'''
			«IF property.type instanceof Enumeration || property.type instanceof UserDefinedType»
				<a href="#«property.type.htmlLink»">«generateFeatureType(property)»</a>
			«ELSE»
				«generateFeatureType(property)»
			«ENDIF»
			«property.allConstraints.generateInlineConstraints»
		'''
	}

	def private String generateFeatureType(Feature feature) {
		'''
			«paramHelper.featureType(feature, importHelper)»
			«IF feature.cardinality !== null»«commonServices.getPrettyPrintedMultiplicity(feature)»«ENDIF»
		'''
	}
}
