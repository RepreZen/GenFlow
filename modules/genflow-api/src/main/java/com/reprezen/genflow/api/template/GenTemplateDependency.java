/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template;

public class GenTemplateDependency {

	public enum GenTemplateDependencyType {
		PRIMARY_SOURCE, NAMED_SOURCE, GENERATOR, PARAMETER
	}

	private final GenTemplateDependencyType type;
	private final String name;
	private final String info;
	private final boolean required;
	private final String description;

	public GenTemplateDependency(GenTemplateDependencyType type, String name, String info, boolean required,
			String description) {
		this.name = name;
		this.type = type;
		this.info = info;
		this.required = required;
		this.description = description;
	}

	public GenTemplateDependencyType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getInfo() {
		return info;
	}

	public boolean isRequired() {
		return required;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return String.format("%s[name: %s, type: %s, info: %s, required: %s, description: %s]",
				getClass().getSimpleName(), name, type, info, required, description);
	}
}
