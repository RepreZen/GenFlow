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

public class NamedSourceBuilder extends NamedBuilderBase<NamedSourceBuilder> {
	private String sourceClassName;
	private String valueClassName;
	private boolean required;

	public NamedSourceBuilder ofType(ISource<?> source) {
		@SuppressWarnings("unchecked")
		Class<? extends ISource<?>> validClass = (Class<? extends ISource<?>>) source.getClass();
		return ofType(validClass);
	}

	public NamedSourceBuilder ofType(Class<? extends ISource<?>> sourceClass) {
		this.sourceClassName = sourceClass.getName();
		return this;
	}

	public NamedSourceBuilder ofType(String className) throws GenerationException {
		Optional<Class<?>> sourceClass = BuilderUtil.getClass(className);
		if (!sourceClass.isPresent() || ISource.class.isAssignableFrom(sourceClass.get())) {
			sourceClassName = className;
			return this;
		} else {
			throw new GenerationException("Class does not implement ISource<?>: " + className);
		}
	}

	public NamedSourceBuilder withValueType(Object value) {
		return withValueType(value.getClass());
	}

	public NamedSourceBuilder withValueType(Class<?> valueType) {
		return withValueType(valueType.getName());
	}

	public NamedSourceBuilder withValueType(String valueClassName) {
		this.valueClassName = valueClassName;
		return this;
	}

	public NamedSourceBuilder required(boolean required) {
		this.required = required;
		return this;
	}

	public NamedSourceBuilder required() {
		return required(true);
	}

	public NamedSourceBuilder optional() {
		return required(false);
	}

	public NamedSourceSpec build() throws GenerationException {
		return new NamedSourceSpec(name, sourceClassName, valueClassName, required, descriptionLines);
	}

	public static class NamedSourceSpec extends NamedSpecBase {
		private final String sourceClassName;
		private String valueClassName;
		private final boolean required;

		public NamedSourceSpec(String name, String sourceClassName, String valueClassName, boolean required,
				String[] descriptionLines) throws GenerationException {
			super(name, descriptionLines);
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

		@Override
		public void validate() throws GenerationException {
			super.validate();
			if (sourceClassName == null) {
				throw new GenerationException("Named source must specify a source type");
			}
		}

		@Override
		public String toString() {
			return String.format("%s[name: %s, source: %s[%s], required: %s, description: %s]",
					BuilderUtil.simpleName(this), name, sourceClassName, valueClassName, required, description);
		}
	}
}
