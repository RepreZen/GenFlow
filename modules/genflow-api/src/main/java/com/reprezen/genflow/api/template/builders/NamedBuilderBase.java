/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template.builders;

import com.reprezen.genflow.api.GenerationException;

/**
 * @author Andy Lowry
 * 
 */
public class NamedBuilderBase<T extends NamedBuilderBase<?>> extends BuilderBase<T> {

	protected String name;

	public T named(String name) {
		this.name = name;
		@SuppressWarnings("unchecked")
		T t = (T) this;
		T builder = t;
		return builder;
	}

	public static class NamedSpecBase extends SpecBase {

		protected final String name;

		public NamedSpecBase(String name, String[] descriptionLines) {
			super(descriptionLines);
			this.name = name;
		}

		public final String getName() {
			return name;
		}

		protected boolean isNameOptional() {
			return false;
		}

		public void validate() throws GenerationException {
			if (!isNameOptional() && (name == null || name.isEmpty())) {
				throw new GenerationException(specType() + " must have a non-empty name");
			}
		}

	}
}
