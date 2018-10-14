/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template.builders;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.genflow.api.GenerationException;

public class ParameterBuilder extends NamedBuilderBase<ParameterBuilder> {

	private boolean required;
	private Object defaultValue;

	public ParameterBuilder required(boolean required) {
		this.required = required;
		return this;
	}

	public ParameterBuilder required() {
		return required(true);
	}

	public ParameterBuilder optional() {
		return required(false);
	}

	public ParameterBuilder withDefault(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public ParameterBuilder withJsonDefault(String defaultJson) throws GenerationException {
		try {
			this.defaultValue = new ObjectMapper().readTree(defaultJson);
		} catch (IOException e) {
			throw new GenerationException("Invalid JSON string", e);
		}
		return this;
	}

	public ParameterSpec build() throws GenerationException {
		return new ParameterSpec(name, required, defaultValue, descriptionLines);
	}

	public static class ParameterSpec extends NamedSpecBase {
		private final boolean required;
		private final Object defaultValue;

		public ParameterSpec(String name, boolean required, Object defaultValue, String[] descriptionLines)
				throws GenerationException {
			super(name, descriptionLines);
			this.required = required;
			this.defaultValue = defaultValue;
			validate();
		}

		public boolean isRequired() {
			return required;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		// Removed this override because we don't really have a reasonable UI experience
		// when required parameters are
		// missing. This change means we can mark a parameter as required and ALSO
		// provide a default value, which is
		// temporary measure until we do something better in the UI, like bringing the
		// problem to the user's attention
		// prior to attempting execution.
		//
		// @Override
		// public void validate() throws GenerationException {
		// super.validate();
		// if (required && defaultValue != null) {
		// throw new GenerationException("Default value will never be used because
		// parameter " + name
		// + " is required");
		// }
		// }

		@Override
		public String toString() {
			return String.format("%s[name: %s, required: %s, defaultValue: %s, description: %s]",
					BuilderUtil.simpleName(this), name, required, defaultValue, description);
		}
	}
}
