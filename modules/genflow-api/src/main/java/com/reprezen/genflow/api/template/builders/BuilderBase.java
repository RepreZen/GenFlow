/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template.builders;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Andy Lowry
 * 
 */
public class BuilderBase<T extends BuilderBase<?>> {
	protected String[] descriptionLines = new String[0];

	public T withDescription(String... descriptionLines) {
		this.descriptionLines = descriptionLines;
		@SuppressWarnings("unchecked")
		T builder = (T) this;
		return builder;
	}

	public static class SpecBase {
		protected String description;

		public SpecBase(String[] descriptionLines) {
			this.description = descriptionLines.length > 0
					? Arrays.asList(descriptionLines).stream().collect(Collectors.joining("\n"))
					: null;
		}

		public final String getDescription() {
			return description;
		}

		protected String specType() {
			String type = this.getClass().getSimpleName();
			if (type.endsWith("Spec")) {
				type = type.substring(0, type.length() - 4);
			}
			return type.replaceAll("([a-z])([A-Z])", "$1 $2");
		}
	}
}
