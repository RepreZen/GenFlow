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
import com.reprezen.genflow.api.template.IGenTemplate;

public class PrerequisiteBuilder extends NamedBuilderBase<PrerequisiteBuilder> {

	private String genTemplateId;
	private boolean required = true;

	public PrerequisiteBuilder on(String genTemplateId) {
		this.genTemplateId = genTemplateId;
		return this;
	}

	public PrerequisiteBuilder on(IGenTemplate genTemplate) {
		return on(genTemplate.getId());
	}

	public PrerequisiteBuilder on(Class<? extends IGenTemplate> genTemplateClass) throws GenerationException {
		try {
			return on(genTemplateClass.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			throw new GenerationException("Failed to instantiate prerequisite GenTemplate", e);
		}
	}

	public PrerequisiteBuilder required(boolean required) {
		this.required = required;
		return this;
	}

	public PrerequisiteBuilder required() {
		return required(true);
	}

	public PrerequisiteBuilder optional() {
		return required(false);
	}

	public PrerequisiteSpec build() throws GenerationException {
		return new PrerequisiteSpec(name, genTemplateId, required, descriptionLines);
	}

	public static class PrerequisiteSpec extends NamedSpecBase {
		private final String genTemplateId;
		private final boolean required;

		public PrerequisiteSpec(String name, String genTemplateId, boolean required, String[] descriptionLines)
				throws GenerationException {
			super(name, descriptionLines);
			this.genTemplateId = genTemplateId;
			this.required = required;
			validate();
		}

		public String getGenTemplateId() {
			return genTemplateId;
		}

		public boolean isRequired() {
			return required;
		}

		@Override
		public void validate() throws GenerationException {
			super.validate();
			if (genTemplateId == null || genTemplateId.isEmpty()) {
				throw new GenerationException("Prerequisiste must have a non-empty GenTemplate Id");
			}
		}

		@Override
		public String toString() {
			return String.format("%s[name: %s, genTemplateId: %s, required: %s, description: %s]",
					BuilderUtil.simpleName(this), name, genTemplateId, required, description);
		}
	}
}
