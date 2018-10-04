/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template.config;

public class PrimarySourceConfig {
	private String type;
	private String valueType;
	private boolean required;
	private String description;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public final String getValueType() {
		return valueType;
	}

	public final void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
