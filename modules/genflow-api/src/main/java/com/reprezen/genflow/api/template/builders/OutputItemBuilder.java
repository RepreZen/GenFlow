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
import com.reprezen.genflow.api.outputitem.IOutputItem;

public class OutputItemBuilder extends NamedBuilderBase<OutputItemBuilder> {

	private String outputItemClassName;
	private String outputFile;
	private String condition;

	public OutputItemBuilder using(IOutputItem<?, ?> outputItem) throws GenerationException {
		@SuppressWarnings("unchecked")
		Class<? extends IOutputItem<?, ?>> validClass = (Class<? extends IOutputItem<?, ?>>) outputItem.getClass();
		return using(validClass);
	}

	public OutputItemBuilder using(Class<? extends IOutputItem<?, ?>> outputItemClass) throws GenerationException {
		return using(outputItemClass.getName());
	}

	public OutputItemBuilder using(String className) throws GenerationException {
		Optional<Class<?>> outputItemClass = BuilderUtil.getClass(className);
		if (outputItemClass.isPresent() && !IOutputItem.class.isAssignableFrom(outputItemClass.get())) {
			throw new GenerationException("Output Item class does not implement IOutputItem<?,?>: " + className);
		}
		this.outputItemClassName = className;
		return this;
	}

	public OutputItemBuilder writing(String outputFile) {
		this.outputFile = outputFile;
		return this;
	}

	public OutputItemBuilder when(String condition) {
		this.condition = condition;
		return this;
	}

	public OutputItemSpec build() throws GenerationException {
		return new OutputItemSpec(name, outputItemClassName, outputFile, condition, descriptionLines);
	}

	public static class OutputItemSpec extends NamedSpecBase {
		private final String outputItemClassName;
		private final String outputFile;
		private final String condition;

		public OutputItemSpec(String name, String outputItemClassName, String outputFile, String condition,
				String[] descriptionLines) throws GenerationException {
			super(name, descriptionLines);
			this.outputItemClassName = outputItemClassName;
			this.outputFile = outputFile;
			this.condition = condition;
			validate();
		}

		public String getOutputItemClassName() {
			return outputItemClassName;
		}

		public Optional<IOutputItem<?, ?>> getOutputItemInstance(ClassLoader classLoader) {
			return BuilderUtil.getInstance(outputItemClassName, classLoader);
		}

		public String getOutputFile() {
			return outputFile;
		}

		public String getCondition() {
			return condition;
		}

		@Override
		protected boolean isNameOptional() {
			return true;
		}

		@Override
		public void validate() throws GenerationException {
			super.validate();
			if (outputItemClassName == null) {
				throw new GenerationException("Output item is missing");
			}
			if (outputFile != null && outputFile.isEmpty()) {
				throw new GenerationException("Output file, if specified, must not be empty");
			}
			if (condition != null && condition.isEmpty()) {
				throw new GenerationException("Condition, if specified must not be empty");
			}
		}

		@Override
		public String toString() {
			String primaryType = "?";
			String itemType = "?";
			Optional<Object> instance = BuilderUtil.getInstance(outputItemClassName, this.getClass().getClassLoader());
			if (instance.isPresent()) {
				try {
					primaryType = ((IOutputItem<?, ?>) instance.get()).getPrimaryType().getName();
					itemType = ((IOutputItem<?, ?>) instance.get()).getItemType().getName();
				} catch (GenerationException e) {
				}
			}
			return String.format("%s[name: %s, type: %s[%s,%s], outputFile: %s, condition: %s, description: %s]",
					BuilderUtil.simpleName(this), name, outputItemClassName, primaryType, itemType, outputFile,
					condition, description);
		}
	}
}
