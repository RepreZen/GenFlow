/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc.xtend

import com.reprezen.genflow.api.zenmodel.ZenModelOutputItem
import com.reprezen.genflow.common.doc.XDocHelper
import com.reprezen.genflow.common.xtend.XImportHelper
import com.reprezen.genflow.common.xtend.XLiveViewHelpers
import com.reprezen.genflow.common.xtend.XSecuritySchemeImportHelper
import com.reprezen.rapidml.ZenModel

class XGenerateDoc extends ZenModelOutputItem {
	public static val URL_PREFIX_PARAM = 'urlPrefix'
	public static val ANCHOR_NAME_PARAM = 'anchorName'
	public static val PREVIEW_PARAM = 'preview'

	val importHelper = new XImportHelper
	val XSecuritySchemeImportHelper securitySchemeImportHelper = new XSecuritySchemeImportHelper()
	extension XDocHelper docHelper
	var XGenerateInterfaces genInterfaces = null
	var XGenerateInterfaceDataModels genDataModels = null
	var XGenerateSecuritySchemes genSecuritySchemes = null

	override generate(ZenModel model) {
		docHelper = new XDocHelper(context.primarySource?.inputFile?.toURI)
		genInterfaces = new XGenerateInterfaces(importHelper, docHelper)
		genDataModels = new XGenerateInterfaceDataModels(importHelper, docHelper)
		genSecuritySchemes = new XGenerateSecuritySchemes(importHelper, securitySchemeImportHelper, docHelper)
		val templateParam = context.getGenTargetParameters();
		val urlPrefix = templateParam.get(URL_PREFIX_PARAM) as String
		val anchorName = templateParam.get(ANCHOR_NAME_PARAM) as String
		val boolean preview = true == templateParam.get(PREVIEW_PARAM)
		importHelper.init(model)
		securitySchemeImportHelper.init(model)
		genInterfaces.isLiveView(preview)
		'''
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="shortcut icon" href="«urlPrefix»docs-assets/ico/favicon.png">

    <title>«model.nameOrTitle» Documentation</title>

    <!-- Bootstrap core CSS -->
    <link href="«urlPrefix»bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="«urlPrefix»bootstrap/css/bootstrap-reprezen.css" rel="stylesheet">
    «IF preview»
        <!-- Workaround for live preview problem with web-font loading -->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.0/css/bootstrap.min.css">
        «XLiveViewHelpers::generateErrorHandlerForLiveView»
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
    «IF preview»
        «XLiveViewHelpers::generateStylesForLiveView(urlPrefix + '../')»
    «ENDIF»
  </head>

  <body data-spy="scroll" data-target="#nav-left">
    «IF !preview»
    <!-- Fixed navbar -->
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li><a href="#resourceAPIs">Resource APIs</a></li>
            <li><a href="#data-models">Data Models</a></li>
          </ul>
          <ul class="nav navbar-nav navbar-right">
            <li><a href="http://reprezen.com" target="_blank">Created with <img class="logo" src="images/logo.png"></a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </div>
    «ENDIF»

    <div class="container">
      <div class="row" id="«model.htmlLink»">

        «IF !preview»
        <!-- Dynamic Sidebar -->
        <div class="col-md-3">
          <div id="nav-left" class="reprezen-sidebar hidden-print affix">
            <ul class="nav bs-sidenav">
              «FOR resourceAPI : model.resourceAPIs»
                <li>
                  <a href="#«resourceAPI.htmlLink»">«resourceAPI.nameOrTitle»</a>
                  <ul class="nav">
                    «FOR resource : resourceAPI.ownedResourceDefinitions»
                      <li>
                        <a href="#«resource.htmlLink»">«resource.name»</a>
                      </li>
                    «ENDFOR»
                  </ul>
                </li>
              «ENDFOR»
              «FOR dataModel : model.dataModels»
                <li>
                  <a href="#«dataModel.htmlLink»">«dataModel.nameOrTitle»</a>
                  <ul class="nav">
                    «FOR dataType : dataModel.ownedDataTypes»
                      <li>
                        <a href="#«dataType.htmlLink»">«dataType.name»</a>
                      </li>
                    «ENDFOR»
                  </ul>
                </li>
              «ENDFOR»
              «FOR dataModel : importHelper.importedTypes.keySet»
                <li>
                  <a href="#«dataModel.htmlLink»">«dataModel.nameOrTitle»</a>
                  <ul class="nav">
                    «FOR dataType : importHelper.importedTypes.get(dataModel)»
                      <li>
                        <a href="#«dataType.htmlLink»">«dataType.name»</a>
                      </li>
                    «ENDFOR»
                  </ul>
                </li>
              «ENDFOR»
            </ul>
          </div>
        </div>
        «ENDIF»

        «IF model.documentation !== null»
        <div class="col-md-9">
          <a id="modelDoc"></a>
          <div class="page-header">
            <h1>Model Documentation</h1>
          </div>
          «model.generateDocItem»
        </div>
        «ENDIF»
                  
        <div class="col-md-9">
          <a id="resourceAPIs"></a>
          <div class="page-header">
            <h1>Resource APIs</h1>
          </div>
          «genInterfaces.generateInterfaces(model)»
          
          <a id="data-models"></a>
          <div class="page-header">
            <h1>Data Models</h1>
          </div>
          «genDataModels.generateInterfaceDataModels(model)»

          «IF genSecuritySchemes.hasSecuritySchemes(model)»
          <a id="security-schemes"></a>
          <div class="page-header">
            <h1>Security Schemes</h1>
          </div>
          «genSecuritySchemes.generateSecuritySchemes(model)»
          «ENDIF»

        </div> <!-- /container -->
      </div>
    </div>

    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="«urlPrefix»jquery/jquery-1.10.2.min.js"></script>
    <script src="«urlPrefix»bootstrap/js/bootstrap.min.js"></script>
    «IF preview»
        «XLiveViewHelpers::generateJavaScriptForLiveView(anchorName)»
    «ENDIF»
  </body>
</html>
'''
	}
}
