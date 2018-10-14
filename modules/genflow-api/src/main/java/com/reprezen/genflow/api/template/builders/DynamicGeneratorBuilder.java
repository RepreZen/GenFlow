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
import com.reprezen.genflow.api.template.IDynamicGenerator;

public class DynamicGeneratorBuilder extends NamedBuilderBase<DynamicGeneratorBuilder> {

	private String generatorClassName;

	public DynamicGeneratorBuilder using(IDynamicGenerator<?> generator) throws GenerationException {
		@SuppressWarnings("unchecked")
		Class<? extends IDynamicGenerator<?>> validClass = (Class<? extends IDynamicGenerator<?>>) generator.getClass();
		return using(validClass);
	}

	public DynamicGeneratorBuilder using(Class<? extends IDynamicGenerator<?>> generatorClass)
			throws GenerationException {
		return using(generatorClass.getName());
	}

	public DynamicGeneratorBuilder using(String className) throws GenerationException {
		Optional<Class<?>> generatorClass = BuilderUtil.getClass(className);
		if (generatorClass.isPresent() && !IDynamicGenerator.class.isAssignableFrom(generatorClass.get())) {
			throw new GenerationException("Output Item class does not implement IDynamicGenerator<?>: " + className);
		}
		this.generatorClassName = className;
		return this;
	}

	public DynamicGeneratorSpec build() throws GenerationException {
		return new DynamicGeneratorSpec(name, descriptionLines, generatorClassName);
	}

	public static class DynamicGeneratorSpec extends NamedSpecBase {

		private String generatorClassName;

		public DynamicGeneratorSpec(String name, String[] descriptionLines, String generatorClassName)
				throws GenerationException {
			super(name, descriptionLines);
			this.generatorClassName = generatorClassName;
			validate();
		}

		public Optional<IDynamicGenerator<?>> getDynamicGeneratorInstance(ClassLoader classLoader) {
			return BuilderUtil.getInstance(generatorClassName, classLoader);
		}

		@Override
		public void validate() throws GenerationException {
			if (generatorClassName == null) {
				throw new GenerationException("Dynamic Genenerator must specify a generator class");
			}
		}

		@Override
		public String toString() {
			return String.format("%s[name: %s; generatorClass: %s; description: %s]", BuilderUtil.simpleName(this),
					name, generatorClassName, description);
		}
	}
}
