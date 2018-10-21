package com.reprezen.genflow.openapi3.doc

import com.reprezen.kaizen.oasparser.model3.Parameter
import com.reprezen.kaizen.oasparser.model3.Response
import com.reprezen.kaizen.oasparser.model3.Schema

class RefHelper implements Helper {

	override init() {
	}

	def dispatch Object resolve(Object obj) {
		switch obj {
// FIXME adapt to OAS3
//            RefParameter: obj.resolve
//            RefModel: obj.resolve
//            RefProperty: obj.resolve
//            RefResponse: obj.resolve
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

// FIXME adapt to OAS3
//    def dispatch resolve(RefParameter ref) {
//        resolve(ref.$ref, RefType::PARAMETER, ref.simpleRef, ref.refFormat)
//    }
//
//    def dispatch safeResolve(RefParameter ref) {
//        ref.safeResolve(ref.$ref, RefType::PARAMETER, ref.simpleRef, ref.refFormat)
//    }
//
	def dispatch resolve(Schema model) {
		model
	}

	def dispatch safeResolve(Schema model) {
		model
	}

// FIXME adapt to OAS3
//    def dispatch resolve(RefModel ref) {
//        resolve(ref.$ref, RefType::DEFINITION, ref.simpleRef, ref.refFormat)
//    }
//
//    def dispatch safeResolve(RefModel ref) {
//        ref.safeResolve(ref.$ref, RefType::DEFINITION, ref.simpleRef, ref.refFormat)
//    }
//
//    def dispatch resolve(Property prop) {
//        prop
//    }
//
//    def dispatch safeResolve(Property prop) {
//        prop
//    }
//
//    def dispatch resolve(RefProperty ref) {
//        resolve(ref.$ref, RefType::DEFINITION, ref.simpleRef, ref.refFormat)
//    }
//
//    def dispatch safeResolve(RefProperty ref) {
//        ref.safeResolve(ref.$ref, RefType::DEFINITION, ref.simpleRef, ref.refFormat)
//    }
	def dispatch resolve(Response response) {
		response
	}

	def dispatch safeResolve(Response response) {
		response
	}

// FIXME adapt to OAS3
//    def dispatch resolve(RefResponse ref) {
//        resolve(ref.$ref, RefType::RESPONSE, ref.simpleRef, ref.refFormat)
//    }
//
//    def dispatch safeResolve(RefResponse ref) {
//        ref.safeResolve(ref.$ref, RefType::RESPONSE, ref.simpleRef, ref.refFormat)
//    }
//
//    def private resolve(String refString, RefType refType, String simpleRef, RefFormat refFormat) {
//        val value = switch refFormat {
//            case RefFormat::INTERNAL:
//                switch refType {
//                    case RefType::PARAMETER:
//                        swagger.parameters.get(simpleRef)
//                    case RefType::DEFINITION:
//                        swagger.definitions.get(simpleRef)
//                    case RefType::RESPONSE:
//                        swagger.responses.get(simpleRef)
//                }
//        }
//        value ?: badRef(refString)
//    }
//
//    def private safeResolve(Object refObj, String refString, RefType refType, String simpleRef, RefFormat refFormat) {
//        try {
//            resolve(refString, refType, simpleRef, refFormat)
//        } catch (BadReferenceException e) {
//            refObj
//        }
//    }
//
//    def Object badRef(String refString) {
//        throw new BadReferenceException(refString)
//    }
}

class BadReferenceException extends Exception {
	val String refString

	new(String refString) {
		super("Invalid reference: " + refString)
		this.refString = refString
	}

	def getRefString() {
		refString
	}

}
