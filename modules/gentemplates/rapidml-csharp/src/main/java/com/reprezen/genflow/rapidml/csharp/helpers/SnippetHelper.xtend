package com.reprezen.genflow.rapidml.csharp.helpers

import com.reprezen.genflow.rapidml.csharp.Config
import com.reprezen.genflow.rapidml.csharp.Config.Framework

class SnippetHelper {

	val Framework framework

	new(Config config) {
		this.framework = config.framework
	}

	def routePrefix() {
		switch (framework) {
			case ASP_DOTNET_CORE_2_0_MVC: "Route"
			case ASP_DOTNET_WEBAPI_2: "RoutePrefix"
		}
	}

	def apiController() {
		switch (framework) {
			case ASP_DOTNET_CORE_2_0_MVC: "Controller"
			case ASP_DOTNET_WEBAPI_2: "ApiController"
		}
	}

	def delegateVar() {
		switch (framework) {
			case ASP_DOTNET_CORE_2_0_MVC: "responder"
			case ASP_DOTNET_WEBAPI_2: "responsePayload"
		}
	}

	def delegateVarDecl(String valueType) {
		switch (framework) {
			case ASP_DOTNET_CORE_2_0_MVC: {
				val responderType = if (valueType !== null) '''Responder<«valueType»>''' else "VoidResponder"
				'''«responderType» responder;'''
			}
			case ASP_DOTNET_WEBAPI_2: {
				if (valueType !== null) '''«valueType» responsePayload;'''
			}
		}
	}

	def postProcessExtraCond() {
		switch (framework) {
			case ASP_DOTNET_CORE_2_0_MVC: " && responder.IsNormal()"
			case ASP_DOTNET_WEBAPI_2: ""
		}
	}

	def delegateReturn(boolean notVoid) {
		if (notVoid) {
			switch (framework) {
				case ASP_DOTNET_CORE_2_0_MVC: "return responder.GetResult();"
				case ASP_DOTNET_WEBAPI_2: "return responsePayload;"
			}
		} else {
			switch (framework) {
				case ASP_DOTNET_CORE_2_0_MVC: "return responder.getResult();"
				case ASP_DOTNET_WEBAPI_2: ""
			}
		}
	}

	def methodResponseType(String valueType) {
		switch (framework) {
			case ASP_DOTNET_CORE_2_0_MVC: '''Result<«valueType»>'''
			case ASP_DOTNET_WEBAPI_2:
				valueType
		}
	}

	def delegateType(String valueType) {
		switch (framework) {
			case ASP_DOTNET_CORE_2_0_MVC: '''Responder<«valueType»>'''
			case ASP_DOTNET_WEBAPI_2:
				valueType
		}
	}

	def voidMethodResponseType() {
		switch (framework) {
			case ASP_DOTNET_CORE_2_0_MVC: "VoidResult"
			case ASP_DOTNET_WEBAPI_2: "void"
		}
	}

	def voidDelegateType() {
		switch (framework) {
			case ASP_DOTNET_CORE_2_0_MVC: "VoidResponder"
			case ASP_DOTNET_WEBAPI_2: "void"
		}
	}

	def responseValue() {
		switch (framework) {
			case ASP_DOTNET_CORE_2_0_MVC: "responder.GetResult().GetValue()"
			case ASP_DOTNET_WEBAPI_2: "responsePayload"
		}
	}
}
