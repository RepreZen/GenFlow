/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.lang.reflect.InvocationTargetException;
import java.util.Map.Entry;

import io.swagger.models.parameters.AbstractParameter;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;

/**
 * This class is not currently used in the normalizer, but it is temporarily
 * retained as model for code that may be needed for an upcoming improvement.
 */
public class SwaggerCloneUtils {

	public static Parameter cloneParameter(Parameter from) {
		try {
			Parameter to = from.getClass().getConstructor().newInstance();
			copyFields(from, to);
			return to;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Failed to create parameter copy of type " + from.getClass().getName());
		}
	}

	private static void copyFields(Parameter from, Parameter to) {
		if (from instanceof AbstractSerializableParameter<?>) {
			copyFields((AbstractSerializableParameter<?>) from, (AbstractSerializableParameter<?>) to);
		} else if (from instanceof BodyParameter) {
			copyFields((BodyParameter) from, (BodyParameter) to);
		} else {
			throw new RuntimeException("Unable to copy fields from parameter of type " + from.getClass().getName());
		}
	}

	private static void copyFields(AbstractSerializableParameter<?> from, AbstractSerializableParameter<?> to) {
		copyFields((AbstractParameter) from, (AbstractParameter) to);
		to.setCollectionFormat(from.getCollectionFormat());
		to.setDefault(from.getDefault());
		to.setEnum(from.getEnum());
		if (from.getExample() != null) {
			to.setExample(from.getExample().toString());
		}
		to.setExclusiveMaximum(to.isExclusiveMaximum());
		to.setExclusiveMinimum(to.isExclusiveMinimum());
		to.setFormat(from.getFormat());
		to.setItems(from.getItems());
		to.setMaximum(from.getMaximum());
		to.setMaxItems(from.getMaxItems());
		to.setMaxLength(from.getMaxLength());
		to.setMinLength(to.getMinLength());
		to.setMinimum(from.getMinimum());
		to.setMaxItems(from.getMaxItems());
		to.setMinLength(from.getMinLength());
		to.setMultipleOf(from.getMultipleOf());
		to.setType(from.getType());
		to.setUniqueItems(from.isUniqueItems());
	}

	private static void copyFields(BodyParameter from, BodyParameter to) {
		copyFields((AbstractParameter) from, (AbstractParameter) to);
		to.setExamples(from.getExamples());
		to.setSchema(from.getSchema());
	}

	private static void copyFields(AbstractParameter from, AbstractParameter to) {
		to.setAccess(from.getAccess());
		to.setDescription(from.getDescription());
		to.setIn(from.getIn());
		to.setName(from.getName());
		to.setPattern(from.getPattern());
		to.setRequired(from.getRequired());
		copyVendorExtensions(from, to);
	}

	private static void copyVendorExtensions(AbstractParameter from, AbstractParameter to) {
		for (Entry<String, Object> entry : from.getVendorExtensions().entrySet()) {
			to.setVendorExtension(entry.getKey(), entry.getValue());
		}
	}
}
