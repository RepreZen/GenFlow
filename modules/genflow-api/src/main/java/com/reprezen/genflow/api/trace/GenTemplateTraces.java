/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.trace;

import java.io.File;
import java.util.Map;

import com.google.common.collect.Maps;
import com.reprezen.genflow.api.target.GenTarget;
import com.reprezen.genflow.api.target.GenTargetUtils;

public class GenTemplateTraces {

	Map<File, GenTemplateTrace> traces = Maps.newHashMap();

	public void addTrace(File genTargetFile, GenTemplateTrace trace) {
		traces.put(genTargetFile, trace);
	}

	public void addTrace(GenTarget target, GenTemplateTrace trace) {
		addTrace(GenTargetUtils.getGenTargetFile(target), trace);
	}

	public GenTemplateTrace get(File genTargetFile) {
		GenTemplateTrace trace = traces.get(genTargetFile);
		return trace;
	}

	public GenTemplateTrace get(GenTarget target) {
		return get(GenTargetUtils.getGenTargetFile(target));
	}
}
