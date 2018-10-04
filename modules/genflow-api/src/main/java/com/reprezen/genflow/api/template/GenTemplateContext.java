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
import com.reprezen.genflow.api.template.builders.NamedSourceBuilder.NamedSourceSpec;
import com.reprezen.genflow.api.template.builders.ParameterBuilder.ParameterSpec;
import com.reprezen.genflow.api.template.builders.PrerequisiteBuilder.PrerequisiteSpec;
import com.reprezen.genflow.api.template.builders.PrimarySourceBuilder.PrimarySourceSpec;
import com.reprezen.genflow.api.trace.GenTemplateTrace;
import com.reprezen.genflow.api.trace.GenTemplateTraceBuilder;
import com.reprezen.genflow.api.trace.GenTemplateTraces;
import com.reprezen.genflow.api.trace.GenTemplateTraceBuilder.GenTemplateTraceItemBuilder;

public class GenTemplateContext implements IGenTemplateContext {

	private Logger logger;
	private GenTarget controllingGenTarget;
	private GenTemplateTraces traces;
	private GenTemplateTraceBuilder traceBuilder;
	private IGenTemplate executingGenTemplate;
	private GenTemplateDependencies dependencies;
	private ISource<?> primarySource;
	private File outputDirectory;
	private File currentOutputFile;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#getLogger(
	 * )
	 */
	@Override
	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * getControllingGenTarget()
	 */
	@Override
	public GenTarget getControllingGenTarget() {
		return controllingGenTarget;
	}

	public void setControllingGenTarget(GenTarget controllingGenTarget) {
		this.controllingGenTarget = controllingGenTarget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#getTraces(
	 * )
	 */
	@Override
	public GenTemplateTraces getTraces() {
		return traces;
	}

	public void setTraces(GenTemplateTraces traces) {
		this.traces = traces;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * getTraceBuilder()
	 */
	@Override
	public GenTemplateTraceBuilder getTraceBuilder() {
		return traceBuilder;
	}

	public void setTraceBuilder(GenTemplateTraceBuilder traceBuilder) {
		this.traceBuilder = traceBuilder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * getExecutingGenTemplate()
	 */
	@Override
	public IGenTemplate getExecutingGenTemplate() {
		return executingGenTemplate;
	}

	public void setExecutingGenTemplate(IGenTemplate executingGenTemplate) {
		this.executingGenTemplate = executingGenTemplate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * getPrimarySource()
	 */
	@Override
	public ISource<?> getPrimarySource() {
		return primarySource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * getDependencies()
	 */
	@Override
	public GenTemplateDependencies getDependencies() {
		return dependencies;
	}

	public void setDependencies(GenTemplateDependencies dependencies) {
		this.dependencies = dependencies;
	}

	public void setPrimarySource(ISource<?> primarySource) {
		this.primarySource = primarySource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * getOutputDirectory()
	 */
	@Override
	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * getCurrentOutputFile()
	 */
	@Override
	public File getCurrentOutputFile() {
		return currentOutputFile;
	}

	public void setCurrentOutputFile(File currentOutputFile) {
		this.currentOutputFile = currentOutputFile;
	}

	// Following are convenience methods that make use of context items

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * getGenTargetParameters()
	 */
	@Override
	public Map<String, Object> getGenTargetParameters() {
		return controllingGenTarget.getParameters();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * getPrerequisiteTrace(java.lang.String)
	 */
	@Override
	public GenTemplateTrace getPrerequisiteTrace(String prerequisiteName) throws GenerationException {
		File prerequisiteGenFile = controllingGenTarget.getPrerequisites().get(prerequisiteName).getGenFilePath();
		return traces.get(controllingGenTarget.resolvePath(prerequisiteGenFile));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * resolveOutputPath(java.io.File)
	 */
	@Override
	public File resolveOutputPath(File path) throws GenerationException {
		return controllingGenTarget.resolveOutputPath(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * addTraceItem(java.lang.String)
	 */
	@Override
	public GenTemplateTraceItemBuilder addTraceItem(String type) {
		return traceBuilder.newItem(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.modelsolv.reprezen.generators.api.template.IGenTemplateContext#
	 * addPrimaryTraceItem(java.io.File, java.lang.String)
	 */
	@Override
	public GenTemplateTraceBuilder addPrimaryTraceItem(File outputFile, String locator) {
		return traceBuilder.addPrimaryItem(outputFile, locator);
	}

	public void addPrimarySouceDependency(PrimarySourceSpec spec) {
		dependencies.addPrimarySouceDependency(spec);
	}

	public void addNamedSourceDependency(NamedSourceSpec spec) {
		dependencies.addNamedSourceDependency(spec);
	}

	public void addGeneratorDependency(PrerequisiteSpec spec) {
		dependencies.addGeneratorDependency(spec);
	}

	public void addRequiredGeneratorDependency(String name, String genTemplateId, String description) {
		dependencies.addRequiredGeneratorDependency(name, genTemplateId, description);
	}

	public void addParameterDependency(ParameterSpec spec) {
		dependencies.addParameterDependency(spec);
	}
}
