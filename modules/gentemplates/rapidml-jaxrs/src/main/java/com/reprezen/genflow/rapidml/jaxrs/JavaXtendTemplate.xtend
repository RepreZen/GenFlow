/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jaxrs

import com.reprezen.genflow.api.util.TypeUtils
import com.reprezen.genflow.api.zenmodel.ZenModelExtractOutputItem
import java.util.Set
import org.eclipse.emf.ecore.EObject

abstract class JavaXtendTemplate<T extends EObject> extends ZenModelExtractOutputItem<T> {
	val static Set<String> JAVA_KEYWORDS = #{"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"}
	val Set<String> imports = newHashSet()

	override getItemType() {
		return TypeUtils::getTypeParamClass(getClass(), JavaXtendTemplate, 0);
	}

	protected def String escapeJavaKeywords(String name) {
		if (JAVA_KEYWORDS.contains(name)) {
			return name.concat("_arg");
		} else {
			return name.replaceAll("[:\\.\\-]", "_");
		}
	}

	protected def void addImport(Class<?> clazz) {
		if (!clazz.primitive) {
			addImport(clazz.canonicalName)
		}
	}

	protected def void addImport(String fqClassName) {
		imports.add(fqClassName)
	}

	protected def generateImports() {
		imports.sort.map["import " + it + ";\n"].join("")		
	}

	protected def String getTypeName(Class<?> value) {
		if (value === null) {
			return null;
		}
		addImport(value)
		return value.simpleName
	}

}
