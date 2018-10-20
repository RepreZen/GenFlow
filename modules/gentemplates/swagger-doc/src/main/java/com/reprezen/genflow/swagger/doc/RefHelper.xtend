package com.reprezen.genflow.swagger.doc

import com.reprezen.genflow.swagger.doc.Helper
import com.reprezen.genflow.swagger.doc.HelperHelper
import io.swagger.models.Model
import io.swagger.models.RefModel
import io.swagger.models.RefResponse
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.RefParameter
import io.swagger.models.properties.Property
import io.swagger.models.properties.RefProperty
import io.swagger.models.refs.RefFormat
import io.swagger.models.refs.RefType

class RefHelper implements Helper {

	var private Swagger swagger

	override init() {
		swagger = HelperHelper.swagger
	}

	def dispatch Object resolve(Object obj) {
		switch obj {
			RefParameter: obj.resolve
			RefModel: obj.resolve
			RefProperty: obj.resolve
			RefResponse: obj.resolve
			default: obj
		}
	}

	def dispatch Object safeResolve(Object obj) {
		try {
			obj.resolve
		} catch (BadReferenceException e) {
			obj
		}
	}

	def dispatch resolve(Parameter param) {
		param
	}

	def dispatch safeResolve(Parameter param) {
		param
	}

	def dispatch resolve(RefParameter ref) {
		resolve(ref.$ref, RefType::PARAMETER, ref.simpleRef, ref.refFormat)
	}

	def dispatch safeResolve(RefParameter ref) {
		ref.safeResolve(ref.$ref, RefType::PARAMETER, ref.simpleRef, ref.refFormat)
	}

	def dispatch resolve(Model model) {
		model
	}

	def dispatch safeResolve(Model model) {
		model
	}

	def dispatch resolve(RefModel ref) {
		resolve(ref.$ref, RefType::DEFINITION, ref.simpleRef, ref.refFormat)
	}

	def dispatch safeResolve(RefModel ref) {
		ref.safeResolve(ref.$ref, RefType::DEFINITION, ref.simpleRef, ref.refFormat)
	}

	def dispatch resolve(Property prop) {
		prop
	}

	def dispatch safeResolve(Property prop) {
		prop
	}

	def dispatch resolve(RefProperty ref) {
		resolve(ref.$ref, RefType::DEFINITION, ref.simpleRef, ref.refFormat)
	}

	def dispatch safeResolve(RefProperty ref) {
		ref.safeResolve(ref.$ref, RefType::DEFINITION, ref.simpleRef, ref.refFormat)
	}

	def dispatch resolve(Response response) {
		response
	}

	def dispatch safeResolve(Response response) {
		response
	}

	def dispatch resolve(RefResponse ref) {
		resolve(ref.$ref, RefType::RESPONSE, ref.simpleRef, ref.refFormat)
	}

	def dispatch safeResolve(RefResponse ref) {
		ref.safeResolve(ref.$ref, RefType::RESPONSE, ref.simpleRef, ref.refFormat)
	}

	def private resolve(String refString, RefType refType, String simpleRef, RefFormat refFormat) {
		val value = switch refFormat {
			case RefFormat::INTERNAL:
				switch refType {
					case RefType::PARAMETER:
						swagger.parameters.get(simpleRef)
					case RefType::DEFINITION:
						swagger.definitions.get(simpleRef)
					case RefType::RESPONSE:
						swagger.responses.get(simpleRef)
					default:
						null
				}
			default:
				null
		}
		value ?: badRef(refString)
	}

	def private safeResolve(Object refObj, String refString, RefType refType, String simpleRef, RefFormat refFormat) {
		try {
			resolve(refString, refType, simpleRef, refFormat)
		} catch (BadReferenceException e) {
			refObj
		}
	}

	def Object badRef(String refString) {
		throw new BadReferenceException(refString)
	}
}

class BadReferenceException extends Exception {
	val private String refString

	new(String refString) {
		super("Invalid reference: " + refString)
		this.refString = refString
	}

	def getRefString() {
		refString
	}

}
