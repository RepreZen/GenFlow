package com.reprezen.genflow.openapi3.doc

import java.util.IdentityHashMap
import java.util.Map

class RecursionHelper implements Helper {

	override init() {}

	// would really like an IdentityHashSet here, but Java.util doesn't have one.
	// identity semantics required so we distinguish between different objects even if they're equal
	val Map<Object, Void> activeObjects = new IdentityHashMap<Object, Void>()

	def use(Object obj) {
		if (activeObjects.containsKey(obj)) {
			throw new RecursiveRenderException(obj)
		} else {
			new Activation(obj, this)
		}
	}

	def activate(Object obj) {
		activeObjects.put(obj, null)
	}

	def deactivate(Object obj) {
		activeObjects.remove(obj)
	}
}

class Activation implements AutoCloseable {
	val RecursionHelper helper
	val Object obj

	new(Object obj, RecursionHelper helper) {
		this.helper = helper
		this.obj = obj
		helper.activate(obj)
	}

	override close() {
		helper.deactivate(obj)
	}
}

class RecursiveRenderException extends Exception {
	val Object obj

	new(Object obj) {
		this.obj = obj
	}

	def getObject() {
		return obj
	}
}
