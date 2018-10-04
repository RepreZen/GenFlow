/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template;

import static com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType.GENERATOR;
import static com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType.NAMED_SOURCE;
import static com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType.PARAMETER;
import static com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType.PRIMARY_SOURCE;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType;
import com.reprezen.genflow.api.template.builders.NamedSourceBuilder.NamedSourceSpec;
import com.reprezen.genflow.api.template.builders.ParameterBuilder.ParameterSpec;
import com.reprezen.genflow.api.template.builders.PrerequisiteBuilder.PrerequisiteSpec;
import com.reprezen.genflow.api.template.builders.PrimarySourceBuilder.PrimarySourceSpec;

public class GenTemplateDependencies {

	private final List<GenTemplateDependency> dependencies = Lists.newArrayList();

	public List<GenTemplateDependency> get() {
		return ImmutableList.copyOf(dependencies);
	}

	public void addDependency(GenTemplateDependencyType type, String name, String info, boolean required,
			String description) {
		dependencies.add(new GenTemplateDependency(type, name, info, required, description));
	}

	public void addPrimarySouceDependency(PrimarySourceSpec spec) {
		addDependency(PRIMARY_SOURCE, null, spec.getValueClassName(), spec.isRequired(), spec.getDescription());
	}

	public void addNamedSourceDependency(NamedSourceSpec spec) {
		addDependency(NAMED_SOURCE, spec.getName(), spec.getValueClassName(), spec.isRequired(), spec.getDescription());
	}

	public void addParameterDependency(ParameterSpec spec) {
		addDependency(PARAMETER, spec.getName(), toJson(spec.getDefaultValue()), spec.isRequired(),
				spec.getDescription());
	}

	public void addGeneratorDependency(PrerequisiteSpec spec) {
		addDependency(GENERATOR, spec.getName(), spec.getGenTemplateId(), spec.isRequired(), spec.getDescription());
	}

	public void addRequiredGeneratorDependency(String name, String genTemplateId, String description) {
		addDependency(GENERATOR, name, genTemplateId, true, description);
	}

	private static ObjectMapper mapper = new ObjectMapper();

	private String toJson(Object value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			return "null";
		}
	}
}
