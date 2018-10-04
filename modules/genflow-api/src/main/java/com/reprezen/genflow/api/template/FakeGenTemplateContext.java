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

import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.outputitem.AbstractOutputItem;
import com.reprezen.genflow.api.source.ISource;
import com.reprezen.genflow.api.trace.GenTemplateTrace;
import com.reprezen.genflow.api.trace.GenTemplateTraceBuilder;
import com.reprezen.genflow.api.trace.GenTemplateTraces;

// TODO Come up with a better solution than this hack
/**
 * Support for tests and live generation, for output items that normally expect
 * to be running under the control of an {@link IGenTemplate} and to be supplied
 * via {@link AbstractOutputItem#init(GenTemplateContext)} with a context
 * values.
 * <p>
 * In many such scenarios, limited context information is actually required, and
 * this class provides convenience constructors that install only selected
 * context information for use in such situations.
 * 
 * @author Andy
 * 
 */
public class FakeGenTemplateContext extends GenTemplateContext {

	private final Map<String, Object> parameters;

	public FakeGenTemplateContext() {
		this(null, null);
	}

	public FakeGenTemplateContext(Map<String, Object> parameters) {
		this(parameters, null);
	}

	public FakeGenTemplateContext(ISource<?> primarySource) {
		this(null, primarySource);
	}

	public FakeGenTemplateContext(Map<String, Object> parameters, ISource<?> primarySource) {
		setLogger(Logger.getLogger(FakeGenTemplateContext.class.getName()));
		this.parameters = parameters != null ? parameters : Maps.newHashMap();
		this.setPrimarySource(primarySource);
	}

	@Override
	public Map<String, Object> getGenTargetParameters() {
		return parameters;
	}

	public void setupTraces() {
		setTraces(new GenTemplateTraces());
		setTraceBuilder(new GenTemplateTraceBuilder(null));
	}

	@Override
	public GenTemplateTrace getPrerequisiteTrace(String prerequisiteName) throws GenerationException {
		return getTraces().get(new File(prerequisiteName));
	}

	@Override
	public File getOutputDirectory() {
		return new File("");
	}
}
