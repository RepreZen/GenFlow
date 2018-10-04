/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template.builders;

import java.util.Optional;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.source.ISource;

public class PrimarySourceBuilder extends BuilderBase<PrimarySourceBuilder> {

	private String sourceClassName;
	private String valueClassName;
	private boolean required;

	public PrimarySourceBuilder ofType(ISource<?> source) {
		@SuppressWarnings("unchecked")
		Class<? extends ISource<?>> validClass = (Class<? extends ISource<?>>) source.getClass();
		return ofType(validClass);
	}

	public PrimarySourceBuilder ofType(Class<? extends ISource<?>> sourceClass) {
		this.sourceClassName = sourceClass.getName();
		return this;
	}

	public PrimarySourceBuilder ofType(String className) throws GenerationException {
		Optional<Class<?>> sourceClass = BuilderUtil.getClass(className);
		if (!sourceClass.isPresent() || ISource.class.isAssignableFrom(sourceClass.get())) {
			this.sourceClassName = className;
			return this;
		} else {
			throw new GenerationException("Class does not implement ISource<?>: " + className);
		}
	}

	public PrimarySourceBuilder withValueType(Object value) {
		return withValueType(value.getClass());
	}

	public PrimarySourceBuilder withValueType(Class<?> valueType) {
		return withValueType(valueType.getName());
	}

	public PrimarySourceBuilder withValueType(String valueClassName) {
		this.valueClassName = valueClassName;
		return this;
	}

	public PrimarySourceBuilder required(boolean required) {
		this.required = required;
		return this;
	}

	public PrimarySourceBuilder required() {
		return required(true);
	}

	public PrimarySourceBuilder optional() {
		return required(false);
	}

	public PrimarySourceSpec build() throws GenerationException {
		return new PrimarySourceSpec(sourceClassName, valueClassName, required, descriptionLines);
	}

	public static class PrimarySourceSpec extends SpecBase {

		private final String sourceClassName;
		private String valueClassName;
		private final boolean required;

		public PrimarySourceSpec(String sourceClassName, String valueClassName, boolean required,
				String[] descriptionLines) throws GenerationException {
			super(descriptionLines);
			this.sourceClassName = sourceClassName;
			this.valueClassName = valueClassName;
			this.required = required;
			this.validate();
		}

		public String getSourceClassName() {
			return sourceClassName;
		}

		public Optional<ISource<?>> getInstance() {
			Optional<Object> instance = BuilderUtil.getInstance(sourceClassName);
			return instance.isPresent() ? Optional.<ISource<?>>of((ISource<?>) instance.get())
					: Optional.<ISource<?>>empty();
		}

		public String getValueClassName() {
			if (valueClassName == null) {
				Optional<ISource<?>> instance = getInstance();
				if (instance.isPresent()) {
					try {
						valueClassName = instance.get().getValueType().getName();
					} catch (GenerationException e) {
					}
				}
			}
			return valueClassName;
		}

		public boolean isRequired() {
			return required;
		}

		public void validate() throws GenerationException {
			if (sourceClassName == null) {
				throw new GenerationException("Primary source must specify a source type");
			}
		}

		@Override
		public String toString() {
			return String.format("%s[source: %s[%s], required: %s, description: %s]", BuilderUtil.simpleName(this),
					sourceClassName, valueClassName, required, description);
		}
	}
}
