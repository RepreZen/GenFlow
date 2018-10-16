/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.source.ISource;
import com.reprezen.genflow.api.target.GenTarget;
import com.reprezen.genflow.api.trace.GenTemplateTrace;
import com.reprezen.genflow.api.trace.GenTemplateTraces;

public interface IGenTemplate {

	String getName();

	String getId();

	default GenTemplateProperty getProperty(String propertyName) throws GenerationException {
		return null;
	};

	default Map<String, GenTemplateProperty> getProperties() throws GenerationException {
		return Maps.newHashMap();
	}

	ISource<?> getPrimarySource() throws GenerationException;

	List<GenTemplateDependency> getDependencies() throws GenerationException;

	IGenTemplate newInstance() throws GenerationException;

	Generator getGenerator();

	/**
	 * Prevent a discovered gentemplate from being offered by any GUI components
	 * 
	 * @return true to suppress this gentemplate
	 */
	public boolean isSuppressed();

	/**
	 * More targeted gentemplate suppression, for when a gentemplate that would
	 * normally be applicable to more than one model type should be suppressed for
	 * one or more but not all of them
	 * 
	 * @param modelType the type of model for which to test suppression
	 * @return true to suppress this gentemplate for this model type
	 */
	public boolean isSuppressed(Class<?> modelType);

	public interface Generator {

		GenTemplateTrace generate(GenTarget target, GenTemplateTraces traces) throws GenerationException;

		void attachLogger(Logger logger);
	}
}
