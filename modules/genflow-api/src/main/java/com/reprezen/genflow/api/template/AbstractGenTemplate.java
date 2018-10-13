/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.source.ISource;

public abstract class AbstractGenTemplate implements IGenTemplate {

	protected GenTemplateContext context = new GenTemplateContext();
	protected List<String> alsoKnownAsIds = Lists.newArrayList();

	public AbstractGenTemplate() {
		context.setExecutingGenTemplate(this);
		context.setDependencies(new GenTemplateDependencies());
	}

	@Override
	public String getId() {
		return this.getClass().getCanonicalName();
	}

	protected void alsoKnownAs(String... akaIds) {
		alsoKnownAsIds.addAll(Arrays.asList(akaIds));
	}

	protected List<String> getAlsoKnownAsIds() {
		return Collections.unmodifiableList(alsoKnownAsIds);
	}

	@Override
	public ISource<?> getPrimarySource() throws GenerationException {
		return null;
	}

	@Override
	public List<GenTemplateDependency> getDependencies() throws GenerationException {
		return context.getDependencies().get();
	}

	@Override
	public boolean isSuppressed() {
		return false;
	}

	@Override
	public boolean isSuppressed(Class<?> modelType) {
		return isSuppressed();
	}

	public abstract class Generator implements IGenTemplate.Generator {
		@Override
		public void attachLogger(Logger logger) {
			context.setLogger(logger);
		}
	}

	public static abstract class StaticGenerator implements IGenTemplate.Generator {

		protected AbstractGenTemplate genTemplate;
		protected GenTemplateContext context;

		public StaticGenerator(AbstractGenTemplate genTemplate, GenTemplateContext context) {
			this.genTemplate = genTemplate;
			this.context = context;
		}

		@Override
		public void attachLogger(Logger logger) {
			context.setLogger(logger);
		}
	}
}
