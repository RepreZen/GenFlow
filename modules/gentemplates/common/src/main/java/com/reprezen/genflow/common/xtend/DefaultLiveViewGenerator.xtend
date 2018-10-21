/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.xtend

class DefaultLiveViewGenerator {
    def String generateContent(String urlPrefix) {
    	generateContent(urlPrefix, '')
    }
    def String generateContent(String urlPrefix, String bodyText) {
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
			
			    <title>Documentation</title>
			
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
			    «XLiveViewHelpers::generateStylesForLiveView(urlPrefix + '../')»
			  </head>
			
			  <body data-spy="scroll" data-target="#nav-left">
			  «bodyText»
			
			    <!-- Bootstrap core JavaScript
			    ================================================== -->
			    <!-- Placed at the end of the document so the pages load faster -->
			    <script src="«urlPrefix»jquery/jquery-1.10.2.min.js"></script>
			    <script src="«urlPrefix»bootstrap/js/bootstrap.min.js"></script>
			    «XLiveViewHelpers::generateJavaScriptForLiveView('')»
			  </body>
			</html>
		'''
    }
}
