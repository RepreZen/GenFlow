/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.source.ISource;
import com.reprezen.genflow.api.target.GenTarget;
import com.reprezen.genflow.api.trace.GenTemplateTrace;
import com.reprezen.genflow.api.trace.GenTemplateTraceBuilder;
import com.reprezen.genflow.api.trace.GenTemplateTraces;
import com.reprezen.genflow.api.trace.GenTemplateTraceBuilder.GenTemplateTraceItemBuilder;

public interface IGenTemplateContext {

	public abstract Logger getLogger();

	public abstract GenTarget getControllingGenTarget();

	public abstract GenTemplateTraces getTraces();

	public abstract GenTemplateTraceBuilder getTraceBuilder();

	public abstract IGenTemplate getExecutingGenTemplate();

	public abstract ISource<?> getPrimarySource();

	public abstract GenTemplateDependencies getDependencies();

	public abstract File getOutputDirectory();

	public abstract File getCurrentOutputFile();

	public abstract Map<String, Object> getGenTargetParameters();

	public abstract GenTemplateTrace getPrerequisiteTrace(String prerequisiteName) throws GenerationException;

	public abstract File resolveOutputPath(File path) throws GenerationException;

	public abstract GenTemplateTraceItemBuilder addTraceItem(String type);

	public abstract GenTemplateTraceBuilder addPrimaryTraceItem(File outputFile, String locator);

}